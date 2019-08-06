/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.squin.common.PrioritizedQueue;
import org.squin.common.Priority;
import org.squin.common.Task;
import org.squin.common.TaskListener;


/**
 * Base class for any kind of tasks that produce a result of type R.
 * {@link org.squin.common.Task} implementations that are based
 * on this abstract class, only have to implement the methods
 * {@link java.util.concurrent.Callable#call} and
 * {@link #createFailureResult}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public abstract class TaskBase<R> extends ComparableTemporallyPrioritizedObjectBase<Task>
                                  implements Task<R>, Callable<R>
{
	// members

	final private Logger log = LoggerFactory.getLogger( TaskBase.class );

	private boolean running = false;
	private boolean notifying = false;
	private boolean done = false;
	private R result = null;
	final private PrioritizedQueue<TaskListener<R>> listenerQueue = new PrioritizedQueueImpl<TaskListener<R>> ();

	private long execStartTimestamp;


	// initialization

	/**
	 * Creates a task with the given priority.
	 */
	protected TaskBase ( Priority priority )
	{
		super( priority );
	}


	// implementation of the Task interface

	/**
	 * Implementation of {@link Task#registerListener}.
	 * Notice, this implementation accepts listeners only when i) the task is
	 * not yet running or when ii) the task is already running but it did not
	 * yet started to notify its listeners (i.e. as long as {@link #isNotifying}
	 * returns false).
	 */
	synchronized public void registerListener ( TaskListener<R> listener, Priority priority ) throws IllegalStateException
	{
		if ( notifying ) { throw new IllegalStateException( "Registering the listener (type: " + listener.getClass().getName() + ") is impossible because this task (type: " + getClass().getName() + ") is already notifying its listeners." ); }
		if ( done ) { throw new IllegalStateException( "Registering the listener (type: " + listener.getClass().getName() + ") is impossible because this task (type: " + getClass().getName() + ") has already been completed." ); }

		assert listener != null;

		if ( listenerQueue.contains(listener) ) {
			log.warn( "Listener already registered (task type: {}, task: {}, listener type: {}, listener: {})", new Object[] {getClass().getName(),toString(),listener.getClass().getName(),listener.toString()} );
			return;
		}

		log.trace( "Listener registered (task type: {}, task: {}, listener type: {}, listener: {})", new Object[] {getClass().getName(),toString(),listener.getClass().getName(),listener.toString()} );
		listenerQueue.queue( listener, priority );
		return;
	}

	synchronized final public boolean isRunning ()
	{
		return running;
	}

	synchronized final public boolean isNotifying ()
	{
		return notifying;
	}

	synchronized final public boolean isDone ()
	{
		return done;
	}

	final public R getResult ()
	{
		return result;
	}

	final public long getExecutionStartTimestamp ()
	{
		return execStartTimestamp;
	}


	// implementation of the Runnable interface

	final public void run ()
	{
		synchronized ( this ) {
			if ( running ) {
				String errMsg = "This task is already running (task type: " + getClass().getName() + ", task: " + toString() + ").";
				log.error( errMsg );
				throw new IllegalStateException( errMsg );
			}

			if ( done ) {
				String errMsg = "This task has already been executed (task type: " + getClass().getName() + ", task: " + toString() + ").";
				log.error( errMsg );
				throw new IllegalStateException( errMsg );
			}

			running = true;
			notifying = false;
			done = false;
		}

		log.debug( "Execution of task started (task type: {}, task: {})", getClass().getName(), toString() );

		execStartTimestamp = System.currentTimeMillis();
		boolean success;
		try {
			result = call();
			success = true;
		}
		catch ( Exception e ) {
			log.warn( "Execution of a task caused a {}: {}  (task type: {}, task: {})", new Object[]{e.getClass().getName(),e.getMessage(),getClass().getName(),toString()} );
			log.warn( "stack trace:", e );
			result = createFailureResult( e );
			success = false;
		}

		log.debug( "Execution of task finished. Notifying listeners ...  (task type: {}, task: {})", getClass().getName(), toString() );

		synchronized ( this ) { notifying = true; }
		notifyListeners( result, success );

		log.debug( "Notification of listeners finished (task type: {}, task: {})", getClass().getName(), toString() );

		synchronized ( this ) {
			running = false;
			notifying = false;
			done = true;
		}
	}


	// abstract worker methods methods

	/**
	 * Creates a result that represents a failure caused by throwing the given
	 * exception.
	 * This method will be called when the implementation of the method
	 * {@link java.util.concurrent.Callable#call} throws an exception.
	 *
	 * @param e the exception thrown by the {@link java.util.concurrent.Callable#call} implementation
	 */
	abstract protected R createFailureResult ( Exception e );


	// helper methods

	protected void notifyListeners ( R result, boolean success )
	{
		TaskListener<R> listener;
		while ( (listener=listenerQueue.poll()) != null ) {
			log.trace( "Notifying listener (task type: {}, task: {}, listener type: {}, listener: {})", new Object[] {getClass().getName(),toString(),listener.getClass().getName(),listener.toString()} );
			if ( success ) {
				listener.handleCompletedTask( result );
			} else {
				listener.handleFailedTask( result );
			}
		}
	}

}
