/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.squin.common.Priority;
import org.squin.common.Statistics;
import org.squin.common.TaskListener;
import org.squin.common.impl.PrioritizedQueueImpl;
import org.squin.common.impl.StatisticsImpl;
import org.squin.dataset.QueriedDataset;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.Triple;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.DataRetrievedListener;
import org.squin.lookup.DataImporter;
import org.squin.lookup.FinishedURILookUp;
import org.squin.lookup.RelookupDecisionMaker;
import org.squin.lookup.URILookUpManager;
import org.squin.lookup.URILookUpResult;
import org.squin.lookup.impl.RelookupDecisionMakerImpl;


/**
 * An implementation of an active cache of Linked Data that
 * makes use of a {@link org.squin.lookup.URILookUpManager}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkedDataCacheImpl extends LinkedDataCacheBase
                                 implements TaskListener<URILookUpResult>
{
	final private Logger log = LoggerFactory.getLogger( LinkedDataCacheImpl.class );


	// members

	final protected URILookUpManager lookupMgr;
	final protected DataImporter dataImporter;
	final protected RelookupDecisionMaker relookupDecisionMaker;
	final protected Map<Integer,PendingLookUpRequest> pendingLookUpRequests = new HashMap<Integer,PendingLookUpRequest> ();

	private boolean shuttingdown = false;
	private boolean shutdown = false;


	// initialization

	public LinkedDataCacheImpl ( QueriedDataset dataset, URILookUpManager lookupMgr )
	{
		super( dataset );

		assert lookupMgr != null;
		this.lookupMgr = lookupMgr;

		dataImporter = new DataImporter() {
			public void importData ( Iterator<Triple> data, RDFGraphProvenance prv ) { getQueriedDataset().putRDFGraph(data,prv); }
		};

		relookupDecisionMaker = RelookupDecisionMakerImpl.get();
	}


	// implementation of the LinkedDataCacheBase abstract worker methods

	protected boolean ensureAvailability ( int uriID, Priority priority, DataRetrievedListener listener )
	{
		log.debug( "ensureAvailability for URI {} requested {}.", uriID, (listener==null) ? "(no listener)" : "by listener "+listener.toString() );

		Integer id = Integer.valueOf( uriID );

		synchronized ( this ) {
			PendingLookUpRequest pendReq = pendingLookUpRequests.get( id );
			if ( pendReq == null ) {
				log.debug( "No URI look-up request pending for URI {}. Requesting a new look-up ...", uriID );

				boolean initiated;
				try {
					initiated = lookupMgr.requestLookUp( uriID,
					                                     getURILookUpPriority(priority),
					                                     relookupDecisionMaker,
					                                     dataImporter,
					                                     this ); // URILookUpListener
				} catch ( Exception e ) {
					log.warn( "Requesting a look-up of URI {} caused a {}: {}", new Object[] {uriID,e.getClass().getName(),e.getMessage()} );
					log.warn( "stack trace:", e );
					initiated = false;
				}

				if ( initiated ) {
					log.debug( "... look-up for URI {} initiated.", uriID );
					pendReq = new PendingLookUpRequest();
					pendingLookUpRequests.put( id, pendReq );
				}
				else  {
					log.debug( "... look-up for URI {} rejected.", uriID );
				}
			}
			else if ( log.isDebugEnabled() ) {
				log.debug( "A pending URI look-up request exists for URI {}.", uriID );
			}

			if ( pendReq != null && listener != null ) {
				pendReq.queue( listener, priority );
			}
			return ( pendReq == null );
		}
	}

	public void shutdownNow ( long timeoutInMilliSeconds ) throws ExecutionException, TimeoutException
	{
		if ( shutdown ) {
			return;
		}

		if ( shuttingdown ) {
			throw new IllegalStateException( "This cache is already being shut down at the moment." );
		} else {
			shuttingdown = true;
		}

		log.debug( "Shutting down {} ...", this.toString() );

		try {
			lookupMgr.shutdownNow( timeoutInMilliSeconds );
		}
		catch ( Exception e ) {
			String msg = "Shutting down the URI look-up manager of this cache caused a " + e.getClass().getName() + " with message: " + e.getMessage();
			log.warn( msg );
			shuttingdown = false;
			throw new ExecutionException( msg, e );
		}

		log.debug( "... shut down of {} finished.", this.toString() );
		shuttingdown = false;
		shutdown = true;
	}

	public boolean isShutdown ()
	{
		return shutdown;
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "dataset", dataset.getStatistics() );
		statAttrs.add( "lookupMgr", lookupMgr.getStatistics() );
		return new StatisticsImpl( statAttrs );
	}

	// implementation of the TaskListener<URILookUpResult> interface

	public void handleCompletedTask ( URILookUpResult result )
	{
		PendingLookUpRequest request;
		synchronized ( this ) {
			request = pendingLookUpRequests.remove( Integer.valueOf(result.getURIID()) );
		}

		log.debug( "Look-up for URI {} finished. Notifying listeners ...", result.getURIID() );


		DataRetrievedListener l;
		while ( (l=request.poll()) != null ) {
			log.debug( "Notifying listener {} about URI {} ...", l, result.getURIID() );
			l.ensureAvailabilityFinished( result.getURIID() );
		}

		log.debug( "Finished notifying listeners about the finished look-up of URI {}.", result.getURIID() );
	}

	public void handleFailedTask ( URILookUpResult result )
	{
		throw new UnsupportedOperationException( "TODO" );
	}


	// helpers

	final protected QueriedDataset getQueriedDataset ()
	{
		return dataset;
	}

	/**
	 * Returns a priority for URI look-ups that is equally important as the
	 * given priority for data retrieval.
	 */
	static public Priority getURILookUpPriority ( Priority p )
	{
		return p;
// 		if ( p.equals(Priority.HIGH) ) {
// 			return Priority.HIGH;
// 		} else if ( p.equals(Priority.MEDIUM) ) {
// 			return Priority.MEDIUM;
// 		} else if ( p.equals(Priority.LOW) ) {
// 			return Priority.LOW;
// 		} else {
// 			throw new IllegalArgumentException();
// 		}
	}


	static class PendingLookUpRequest extends PrioritizedQueueImpl<DataRetrievedListener>
	{}

}
