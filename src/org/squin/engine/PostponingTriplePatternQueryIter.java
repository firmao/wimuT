/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.squin.common.Priority;
import org.squin.dataset.Triple;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.TriplePattern;


/**
 * A postponing iterator used for the iterator-based implementation of link
 * traversal based query execution.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class PostponingTriplePatternQueryIter extends PrefetchingTriplePatternQueryIter
{
	// members

	static Random random = new Random( System.currentTimeMillis() );

	final private Logger log = LoggerFactory.getLogger( PostponingTriplePatternQueryIter.class );

	// Attention: we manage the postponed solutions in the postponing iterator
	//            (instead of explicitly introducing a postpone function and
	//             having the predecessor iterator manage them)
	final protected Queue<SolutionMapping> postponedSolutions = new LinkedList<SolutionMapping> ();

	// counters for statistics
	protected long tryCounter = 0;
	protected long retryCounter = 0;
	protected long newtryCounter = 0;
	protected long solCounter = 0;


	// initialization

	public PostponingTriplePatternQueryIter ( TriplePattern tp, Iterator<SolutionMapping> input, LinkTraversalBasedExecutionContext execCxt )
	{
		super( tp, input, execCxt );
	}


	// implementation of the Iterator interface

	public boolean hasNext ()
	{
		if ( isClosed() ) {
			return false;
		}

		while ( currentMatches == null || ! currentMatches.hasNext() )
		{
			if ( ! input.hasNext() && postponedSolutions.isEmpty() ) {
				return false;
			}

			boolean retry;
			int postponeSleepTime;
			if ( postponedSolutions.isEmpty() ) {
				retry = false;
				postponeSleepTime = 1;
			} else if ( ! input.hasNext() ) {
				retry = true;
				postponeSleepTime = 100; // avoid busy waiting
			} else {
				retry = random.nextBoolean();
				postponeSleepTime = 1;
			}

			tryCounter++;
			if ( retry ) {
				retryCounter++;
			} else {
				newtryCounter++;
			}

			currentInputMapping = ( retry ) ? postponedSolutions.remove() : input.next();
			currentQueryPattern = substitute( tp, currentInputMapping );

			if ( requestAvailability(currentQueryPattern) ) {
				currentMatches = ltbExecCxt.ldcache.find( ltbExecCxt.accessContext,
				                                          (currentQueryPattern.sIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.s,
				                                          (currentQueryPattern.pIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.p,
				                                          (currentQueryPattern.oIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.o );
			}
			else {
				postponedSolutions.offer( currentInputMapping ); // POSTPONE

				try {
// 					Thread.sleep( 0, 1 ); // 0.000001 ms
					Thread.sleep( postponeSleepTime ); // 1 ms
				} catch ( Exception e ) {
					log.debug( "Unexpected exception (type: {}) caught: {}", e.getClass().getName(), e.getMessage() );
				}
			}
		}

		solCounter++;
		return true;
	}


	// operations

	/**
	 * This method initiates that the Linked Data cache ensures all data for the
	 * URIs in the given triple pattern is available.
	 *
	 * @return true, if the data is already available and, thus, it was
	 *         not necessary to queue asynchronous data retrieval tasks
	 */
	protected boolean requestAvailability ( TriplePattern p )
	{
		boolean nonBlocking = true;
		if ( ! p.sIsVar && ! requestAvailability(p.s,Priority.MEDIUM) ) {
			nonBlocking = false;
		}
		if ( ltbExecCxt.predicateLookUpEnabled && ! p.pIsVar && ! requestAvailability(p.p,Priority.MEDIUM) ) {
			nonBlocking = false;
		}
		if ( ! p.oIsVar && ! requestAvailability(p.o,Priority.MEDIUM) ) {
			nonBlocking = false;
		}

		return nonBlocking;
	}

}
