/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Node;

import org.squin.common.Priority;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.query.BindingProvenance;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.TriplePattern;
import org.squin.dataset.query.impl.FixedSizeSolutionMappingImpl;
import org.squin.dataset.query.arq.iterators.TriplePatternQueryIter;
import org.squin.ldcache.DataRetrievedListener;


/**
 * Naive implementation of the iterator used for the iterator-based
 * implementation of link traversal based query execution.
 *
 * {@link #tp} corresponds to tp_i in the ISWC'09 paper
 * {@link #currentInputMapping} corresponds to \mu_{cur} in the paper
 * {@link #currentQueryPattern} corresponds to \mu_{cur}[{tp_i}] in the paper
 * {@link #currentMatches} corresponds to I_{find} in the paper
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class NaiveTriplePatternQueryIter extends TriplePatternQueryIter
                                         implements DataRetrievedListener
{
	final private Logger log = LoggerFactory.getLogger( NaiveTriplePatternQueryIter.class );

	// members

	// attention: access to this member must be thread-safe
	final private Collection<Integer> pendingDataRetrievals = new java.util.HashSet<Integer> ();

	final protected LinkTraversalBasedExecutionContext ltbExecCxt;

	private Boolean closed = false;


	// initialization

	public NaiveTriplePatternQueryIter ( TriplePattern tp, Iterator<SolutionMapping> input, LinkTraversalBasedExecutionContext execCxt )
	{
		super( tp, input, execCxt );
		ltbExecCxt = execCxt;
	}


	// accessors

	public boolean isClosed ()
	{
		synchronized ( closed ) {
			return closed;
		}
	}


	// implementation of the Iterator interface

	public boolean hasNext ()
	{
		if ( isClosed() ) {
			return false;
		}

		while ( currentMatches == null || ! currentMatches.hasNext() )
		{
			if ( ! input.hasNext() ) {
				return false;
			}

			currentInputMapping = input.next();
			currentQueryPattern = substitute( tp, currentInputMapping );

			ensureRequirement( currentQueryPattern ); // this may take some time

			if ( execCxt.recordProvenance ) {
				currentMatches = ltbExecCxt.ldcache.findWithProvenance( ltbExecCxt.accessContext,
				                                                        (currentQueryPattern.sIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.s,
				                                                        (currentQueryPattern.pIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.p,
				                                                        (currentQueryPattern.oIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.o );
			} else {
				currentMatches = ltbExecCxt.ldcache.find( ltbExecCxt.accessContext,
				                                          (currentQueryPattern.sIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.s,
				                                          (currentQueryPattern.pIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.p,
				                                          (currentQueryPattern.oIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.o );
			}
		}

		return true;
	}

	final public SolutionMapping next ()
	{
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		// Create the next solution mapping by i) copying the mapping currently
		// consumed from the input iterator and ii) by binding the variables in
		// the copy corresponding to the currently matching triple (currentMatch).
		return constructResult( currentMatches.next() );
	}


	// implementation of the Closable interface

	@Override
	public void close ()
	{
		synchronized ( closed ) {
			closed = true;
		}

		super.close();

		synchronized ( this ) {
			pendingDataRetrievals.clear();
		}
	}


	// operations

	protected void ensureRequirement ( TriplePattern p )
	{
		log.debug( "Ensuring look-up requirement for triple pattern {} ...", p.toString() );

		boolean blocking = false;
		if ( ! p.sIsVar && ! ensureAvailability(p.s) ) {
			blocking = true;
		}
		if ( ltbExecCxt.predicateLookUpEnabled && ! p.pIsVar && ! ensureAvailability(p.p) ) {
			blocking = true;
		}
		if ( ! p.oIsVar && ! ensureAvailability(p.o) ) {
			blocking = true;
		}

		if ( ! blocking ) {
			log.debug( "... look-up requirement for triple pattern {} ensured (no blocking).", p.toString() );
			return;
		}

		log.debug( "... look-up requirement for triple pattern {} blocks query execution ...", p.toString() );

		synchronized( this ) {
			if ( pendingDataRetrievals.isEmpty() ) {
				log.debug( "... look-up requirement for triple pattern {} ensured.", p.toString() );
				return;
			}

			try {
				wait();
			}
			catch ( InterruptedException e ) {
				throw new RuntimeException( "Waiting for the retrieval of required data interrupted by " + e.getClass().getName() + ": " + e.getMessage(), e );
			}

			log.debug( "... look-up requirement for triple pattern {} ensured (after blocking).", p.toString() );
		}
	}

	protected SolutionMapping constructResult ( Triple currentMatch )
	{
		SolutionMapping result = new FixedSizeSolutionMappingImpl( currentInputMapping );

		BindingProvenance currentMatchProvenance = execCxt.recordProvenance ? new BindingProvenanceImpl( (TraceableTriple) currentMatch, tp ) : null;

		if ( currentQueryPattern.sIsVar ) {
			result.set( currentQueryPattern.s, currentMatch.s, currentMatchProvenance );
		}

		if ( currentQueryPattern.pIsVar ) {
			result.set( currentQueryPattern.p, currentMatch.p, currentMatchProvenance );
		}

		if ( currentQueryPattern.oIsVar ) {
			result.set( currentQueryPattern.o, currentMatch.o, currentMatchProvenance );
		}

		return result;
	}


	// helper methods

	/**
	 * This method ensures that the Linked Data cache ensures all data for the
	 * URI identified by the given ID is available.
	 * Since the Linked Data cache executes such requests in an asynchronous
	 * manner, this method may add the given ID to the list of
	 * {@link #pendingDataRetrievals}.
	 *
	 * @param nodeID the identifier for the URI in question
	 * @return true, if everything is done already (i.e. the data is available
	 *         and, thus, it was not necessary to add something to the list of
	 *         {@link #pendingDataRetrievals})
	 */
	protected boolean ensureAvailability ( int nodeID )
	{
		Node node = ltbExecCxt.nodeDict.getNode( nodeID );
		if ( node == null || ! node.isURI() ) {
			return true;
		}

		synchronized ( this ) {
			if ( pendingDataRetrievals.contains(Integer.valueOf(nodeID)) ) {
				log.debug( "ensureAvailability for URI <{}> with ID {}: already pending", node.getURI(), nodeID );
				return true;
			}

			if ( ltbExecCxt.ldcache.ensureAvailability(ltbExecCxt.accessContext,nodeID,Priority.HIGH,this) ) {
				log.debug( "ensureAvailability for URI <{}> with ID {}: finished immediately", node.getURI(), nodeID );
				return true;
			}
			else {
				log.debug( "ensureAvailability for URI <{}> with ID {}: logged as pending", node.getURI(), nodeID );
				pendingDataRetrievals.add( Integer.valueOf(nodeID) );
				return false;
			}
		}
	}


	// implementation of the DataRetrievedListener interface

	public void ensureAvailabilityFinished ( int uriID )
	{
		synchronized ( this ) {
			log.debug( "Pending data retrieval for URI {} finished (pending data retrievals: {}).", uriID, pendingDataRetrievals.size()-1 );
			if ( log.isWarnEnabled() && ! pendingDataRetrievals.contains(Integer.valueOf(uriID)) ) {
				log.warn( "No pending data retrieval logged for URI {}. Ignoring.", uriID );
			}

			pendingDataRetrievals.remove( Integer.valueOf(uriID) );
			if ( pendingDataRetrievals.isEmpty() ) {
				notify();
			}
		}
	}

}
