/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.squin.common.Priority;
import org.squin.common.Statistics;
import org.squin.common.TaskListener;
import org.squin.common.impl.LockableTaskStatus;
import org.squin.common.impl.LockableTaskStatusBase;
import org.squin.common.impl.StatisticsImpl;
import org.squin.common.impl.TaskStatusIndexBase;
import org.squin.lookup.DataImporter;
import org.squin.lookup.deref.DataAnalyzer;
import org.squin.lookup.deref.DereferencingResult;
import org.squin.lookup.deref.DereferencingStatus;
import org.squin.lookup.deref.FinishedDereferencing;
import org.squin.lookup.deref.RederefDecisionMaker;
import org.squin.lookup.deref.URIDerefManager;


/**
 * Base class for implementations of a {@link URIDerefManager}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public abstract class URIDerefManagerBase implements URIDerefManager, TaskListener<DereferencingResult>
{
	static final private Logger log = LoggerFactory.getLogger( URIDerefManagerBase.class );

	// configuration parameters for the thread pool
	static final private int executorDefaultCorePoolSize = 10;
	static final private int executorDefaultMaximumPoolSize = 20;
	static final private long executorDefaultKeepAliveTime = 600; // 10 min
	static final private TimeUnit executorDefaultTimeUnit = TimeUnit.SECONDS;

	// members

	/** the thread pool used by this deref. manager */
	final private ThreadPoolExecutor executor;

	/** the index of dereferencing statuses used by this deref. manager */
	// Never access this member without synchronization!
	final private DerefStatusIndex statuses = new DerefStatusIndex ();

	/** denotes whether this deref. manager is currently shutting down */
	// Never access this member without synchronization!
	protected AtomicBoolean shuttingdown = new AtomicBoolean( false );

	/** denotes whether a previous shut down attempt for this deref. manager failed */
	protected boolean shutdownFailed = false;

	private long finishedTaskCount = 0;
	private long failedTaskCount = 0;
	private long redirectionCount = 0;
	private long overallQueueTime = 0;
	private long overallExecTime = 0;


	// initialization

	public URIDerefManagerBase ()
	{
		executor = new ThreadPoolExecutor( executorDefaultCorePoolSize,
		                                   executorDefaultMaximumPoolSize,
		                                   executorDefaultKeepAliveTime,
		                                   executorDefaultTimeUnit,
		                                   new PriorityBlockingQueue<Runnable> () );
	}

	public void finalize ()
	{
		if ( ! executor.isTerminated() )
		{
			log.warn( "Finalizing a deref. manager (type: {}) that still seems to be running. Let's try to shut it down.", getClass().getName() );

			try {
				shutdownNow( 3000 );
			}
			catch ( Exception e ) {
				log.error( "Shutting down the deref. manager (type: {}) caused a {}: {}", new Object[] {getClass().getName(),e.getClass().getName(),e.getMessage()} );
			}
		}
	}


	// implementation of the URIDerefManager interface

	public DereferencingStatus getDereferencingStatus ( int uriID )
	{
		synchronized ( statuses ) {
			return statuses.getStatus( uriID );
		}
	}

	public DereferencingResult requestDereferencing ( int uriID,
	                                                  Priority priority,
	                                                  RederefDecisionMaker rederef,
	                                                  DataImporter importer,
	                                                  DataAnalyzer analyzer,
	                                                  TaskListener<DereferencingResult> listener ) throws IllegalStateException
	{
		assert priority != null;
		assert rederef != null;
		assert importer != null;

		checkDereferenceability( uriID );

		log.debug( "Dereferencing of URI with identifier {} requested.", uriID );

		synchronized ( shuttingdown )
		{
			// check if dereferencing is (still) possible at all
			if ( executor.isShutdown() ) { throw new IllegalStateException( "Accepting dereferencing requests impossible: This " + this + " has already been shut down." ); }
			if ( shutdownFailed ) { throw new IllegalStateException( "Accepting dereferencing requests impossible: We already tried to shut down this " + this + "." ); }
			if ( shuttingdown.get() == true ) { throw new IllegalStateException( "Accepting dereferencing requests impossible: We are already in the process of shutting this " + this + " down." ); }

			// get the (previous) dereferencing status of the given URI
			DereferencingStatus s;
			synchronized ( statuses ) {
				s = statuses.getLockedStatus( uriID );
			}

			// decide on how to handle the dereferencing request
			// depending on the (previous) dereferencing status
			DereferencingResult result;
			try {
				if ( s.isUnknown() )
				{
					initiateDereferencing( uriID, priority, importer, analyzer, listener );
					result = null;
				}
				else if ( s.isFinished() )
				{
					if ( rederef.decideAboutRedereferencing(uriID,s.asFinishedDereferencing()) )
					{
						initiateDereferencing( uriID, priority, importer, analyzer, listener );
						result = null;
					}
					else {
						result = s.asFinishedDereferencing().getResult();
						synchronized ( statuses ) {
							statuses.unlockStatus( uriID );
						}
					}
				}
				else if ( s.isPending() )
				{
					dealWithPendingTask( uriID, (PendingDereferencing) s, importer, analyzer, priority, listener );
					result = null;
				}
				else
				{
					String msg = "Unknown dereferencing status (" + s.toString() + ") for URI with identifier " + uriID + ".";
					log.error( msg );

					synchronized ( statuses ) {
						statuses.unlockStatus( uriID );
					}

					throw new IllegalArgumentException( msg );
				}
			}
			catch ( Exception e ) {
				String msg = "Unexpected " + e.getClass().getName() + " caught: " + e.getMessage();
				log.warn( "{}  -- Trying to unlock the current deref status for URI {} at least, before throwing an exception.", msg, uriID );
				synchronized ( statuses ) {
					statuses.unlockStatus( uriID );
				}
				throw new IllegalStateException( msg, e );
			}

			return result;
		}
	}

	public void shutdownNow ( long timeoutInMilliSeconds ) throws ExecutionException, TimeoutException
	{
		// check whether shut down already completed
		synchronized ( shuttingdown ) {
			if (    executor.isTerminated()
			     || ( ! shutdownFailed && shuttingdown.get() == true ) ) {
				return;
			}
		}

		log.debug( "Shutting down {} ...", this.toString() );

		// initiate shut down of the thread pool and wait for its termination
		executor.shutdownNow();
		boolean terminated;
		try {
			terminated = executor.awaitTermination( timeoutInMilliSeconds, TimeUnit.MILLISECONDS );
		}
		catch ( InterruptedException e ) {
			String msg = "Unexpected interruption (class:" + e.getClass().getName() + " message: " + e.getMessage() + ") of executor.awaitTermination during shut down of " + this.toString() + " (getActiveCount: " + executor.getActiveCount() + ", getTaskCount: " + executor.getTaskCount() + ", isShutdown: " + executor.isShutdown() + ", isTerminated: " + executor.isTerminated() + ", isTerminating: " + executor.isTerminating() + ").";
			log.error( msg );

			synchronized ( shuttingdown ) {
				shuttingdown.set( false );
				shutdownFailed = true;
			}

			throw new ExecutionException( msg, e );
		}

		if ( ! terminated ) {
			String msg = "Termination of the thread pool timed out during shut down of " + this.toString() + " (getActiveCount: " + executor.getActiveCount() + ", getTaskCount: " + executor.getTaskCount() + ", isShutdown: " + executor.isShutdown() + ", isTerminated: " + executor.isTerminated() + ", isTerminating: " + executor.isTerminating() + ").";
			log.warn( msg );

			synchronized ( shuttingdown ) {
				shuttingdown.set( false );
				shutdownFailed = true;
			}

			throw new TimeoutException( msg );
		}

		log.debug( "... shut down of {} completed successfully.", this.toString() );

		synchronized ( shuttingdown ) {
			shuttingdown.set( false );
		}
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "thread pool - largestPoolSize", executor.getLargestPoolSize() );
		statAttrs.add( "thread pool - completedTaskCount", executor.getCompletedTaskCount() );
		statAttrs.add( "thread pool - taskCount", executor.getTaskCount() );
		statAttrs.add( "finishedTaskCount", finishedTaskCount );
		statAttrs.add( "redirectionCount", redirectionCount );
		statAttrs.add( "failedTaskCount", failedTaskCount );
		statAttrs.add( "overall queue time", overallQueueTime );
		statAttrs.add( "avg queue time", (finishedTaskCount != 0 ) ? overallQueueTime / finishedTaskCount : 0 );
		statAttrs.add( "overall exec. time", overallExecTime );
		statAttrs.add( "avg exec. time", (finishedTaskCount != 0 ) ? overallExecTime / finishedTaskCount : 0 );
		statAttrs.add( "statuses index", statuses.getStatistics() );
		return new StatisticsImpl( statAttrs );
	}


	// worker methods

	/**
	 * Creates a new dereferencing task, queues this task for asynchronous
	 * execution, and updates the dereferencing status for the given URI
	 * accordingly.
	 */
	protected void initiateDereferencing ( int uriID,
	                                       Priority priority,
	                                       DataImporter importer,
	                                       DataAnalyzer analyzer,
	                                       TaskListener<DereferencingResult> listener )
	{
		log.debug( "Initiate dereferencing of the URI with identifier {}.", uriID );

		DerefTask task = createDerefTask( uriID, priority, importer, analyzer );
		task.registerListener( this, Priority.HIGH );
		if ( listener != null ) {
			task.registerListener( listener, priority );
		}

		LockableDereferencingStatus newStatus = new PendingDereferencing( task );
		synchronized ( statuses ) {
			executor.execute( task );
			statuses.updateStatus( uriID, newStatus );
		}
	}

	/**
	 * Tries to deal with a dereferencing request for which a previously
	 * initiated dereferencing task is currently pending.
	 * Unlocks the current status.
	 */
	protected void dealWithPendingTask ( int uriID,
	                                     PendingDereferencing currentStatus,
	                                     DataImporter importer,
	                                     DataAnalyzer analyzer,
	                                     Priority priority,
	                                     TaskListener<DereferencingResult> listener )
	{
		log.debug( "Trying to deal with pending dereferencing task for the URI with identifier {}.", uriID );

		DerefTaskBase task = (DerefTaskBase) currentStatus.task;
		synchronized ( task )
		{
			// check the given data importer
			if ( importer != null && ! task.isRegisteredDataImporter(importer) )
			{
				synchronized ( statuses ) {
					statuses.unlockStatus( uriID );
				}
// TODO:
				throw new UnsupportedOperationException( "We cannot attach a second data importer to an already running deref task. Not sure what to do in this case :-(" );
			}

   		// (try to) register the given data analyzer (if any)
			if ( analyzer != null ) {
				task.registerDataAnalyzer( analyzer );
			}

			// (try to) register the given listener (if any)
			if ( listener != null )
			{
				try {
					task.registerListener( listener, priority );
				}
				catch ( IllegalStateException e ) {
					synchronized ( statuses ) {
						statuses.unlockStatus( uriID );
					}
// TODO:
					throw new UnsupportedOperationException( "We cannot attach a listener to a deref task that is already notifying its listeners. Not sure what to do in this case :-(" );
				}
			}

			// adjust the priority of the (pending) task if it is still awaiting execution
			if (    ! task.isRunning()
			     && task.getPriority().compareTo(priority) > 0
			     && executor.remove(task) )
			{
				task.upgradePriority( priority );
				executor.execute( task );
			}

			// unlock the current status
			synchronized ( statuses ) {
				statuses.unlockStatus( uriID );
			}
		}
	}


	// implementation of the TaskListener<DereferencingResult> interface

	public void handleCompletedTask ( DereferencingResult result )
	{
		DereferencingStatus s;
		synchronized ( statuses ) {
			s = statuses.getLockedStatus( result.getURIID() );

			if ( ! s.isPending() ) {
				log.warn( "Completion of dereferencing for URI {} reported (reported dereferencing result: {}) but the current dereferencing status (current dereferencing status: {}) for this URI is not 'pending'. Ignoring it.", new Object[] {result.getURIID(),result.toString(),s.toString()} );
				statuses.unlockStatus( result.getURIID() );
				return;
			}
		}

		// update statistics
		finishedTaskCount++;
		if ( result.hasBeenRedirected() ) { redirectionCount++; }
		if ( result.isFailure() ) { failedTaskCount++; }
		overallQueueTime += result.getQueueTime();
		overallExecTime += result.getExecutionTime();

		LockableDereferencingStatus newStatus = new FinishedDereferencingImpl( result );
		synchronized ( statuses ) {
			statuses.updateStatus( result.getURIID(), newStatus );
		}
	}

	public void handleFailedTask ( DereferencingResult result )
	{
		handleCompletedTask( result );
	}


	// abstract worker methods

	/**
	 * An implementation of this method must return a new {@link DerefTask}
	 * object constructed using the given parameters.
	 *
	 * @param uriID identifier of the URI that has to be dereferenced
	 *              (mandatory, i.e. must not be null)
	 * @param priority priority of the task
	 *                 (mandatory, i.e. must not be null)
	 * @param importer data importer for the task
	 *                 (mandatory, i.e. must not be null)
	 * @param analyzer a data analyzer to be registered with the task
	 *                 (optional, i.e. may be null)
	 */
	abstract protected DerefTask createDerefTask ( int uriID, Priority priority, DataImporter importer, DataAnalyzer analyzer );

	/**
	 * Checks that the URI identified by the given ID is actually
	 * dereferenceable.
	 *
	 * @param uriID identifier of the URI to be checked
	 * @throws IllegalArgumentException if the URI identified by the
	 *                               given ID is not dereferenceable
	 */
	abstract protected void checkDereferenceability ( int uriID ) throws IllegalArgumentException;


	// helpers

	static class DerefStatusIndex extends TaskStatusIndexBase<DereferencingStatus,LockableDereferencingStatus>
	{
		final static protected UnknownDereferencingStatus unknown = new UnknownDereferencingStatus ();
		protected DereferencingStatus getUnknownSingleton () { return unknown; }
		protected LockableDereferencingStatus getNewUnknownStatus () { return new UnknownDereferencingStatus(); }
	}

	static abstract class LockableDereferencingStatus extends LockableTaskStatusBase
	                                                  implements DereferencingStatus, LockableTaskStatus
	{}

	static class UnknownDereferencingStatus extends LockableDereferencingStatus
	{
		public boolean isUnknown () { return true; }
		public boolean isPending () { return false; }
		public boolean isFinished () { return false; }
		public FinishedDereferencing asFinishedDereferencing () { throw new UnsupportedOperationException(); }
	}

	static class PendingDereferencing extends LockableDereferencingStatus
	{
		final public DerefTask task;
		public PendingDereferencing ( DerefTask task ) { this.task = task; }
		public boolean isUnknown () { return false; }
		public boolean isPending () { return true; }
		public boolean isFinished () { return false; }
		public FinishedDereferencing asFinishedDereferencing () { throw new UnsupportedOperationException(); }
	}

	static class FinishedDereferencingImpl extends LockableDereferencingStatus implements FinishedDereferencing
	{
		final public DereferencingResult result;
		final public long finishTimeMillis = System.currentTimeMillis();
		public FinishedDereferencingImpl ( DereferencingResult result ) { this.result = result; }
		public boolean isUnknown () { return false; }
		public boolean isPending () { return false; }
		public boolean isFinished () { return true; }
		public FinishedDereferencing asFinishedDereferencing () { return this; }

		public long getFinishTimeMillis () { return finishTimeMillis; }
		public DereferencingResult getResult () { return result; }
	}

}
