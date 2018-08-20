/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;

import org.squin.common.Priority;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.query.BindingProvenance;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.TriplePattern;
import org.squin.dataset.query.impl.FixedSizeSolutionMappingImpl;


/**
 * A prefetching iterator used for the iterator-based implementation of link
 * traversal based query execution.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class PrefetchingTriplePatternQueryIter extends NaiveTriplePatternQueryIter
{
	// initialization

	public PrefetchingTriplePatternQueryIter ( TriplePattern tp, Iterator<SolutionMapping> input, LinkTraversalBasedExecutionContext execCxt )
	{
		super( tp, input, execCxt );
	}


	// operations

	@Override
	protected SolutionMapping constructResult ( Triple currentMatch )
	{
		SolutionMapping result = new FixedSizeSolutionMappingImpl( currentInputMapping );

		BindingProvenance currentMatchProvenance = execCxt.recordProvenance ? new BindingProvenanceImpl( (TraceableTriple) currentMatch, tp ) : null;

		if ( currentQueryPattern.sIsVar ) {
			result.set( currentQueryPattern.s, currentMatch.s, currentMatchProvenance );
			requestAvailability( currentMatch.s, Priority.LOW ); // prefetch
		}

		if ( currentQueryPattern.pIsVar ) {
			result.set( currentQueryPattern.p, currentMatch.p, currentMatchProvenance );
			if ( ltbExecCxt.predicateLookUpEnabled ) {
				requestAvailability( currentMatch.p, Priority.LOW ); // prefetch
			}
		}

		if ( currentQueryPattern.oIsVar ) {
			result.set( currentQueryPattern.o, currentMatch.o, currentMatchProvenance );
			requestAvailability( currentMatch.o, Priority.LOW ); // prefetch
		}

		return result;
	}


	// helper methods

	/**
	 * This method initiates that the Linked Data cache ensures all data for the
	 * URI identified by the given ID is available.
	 *
	 * @param nodeID the identifier for the URI in question
	 * @return true, if the data is already available and, thus, it was
	 *         not necessary to queue asynchronous data retrieval tasks
	 */
	protected boolean requestAvailability ( int nodeID, Priority prio )
	{
		Node node = ltbExecCxt.nodeDict.getNode( nodeID );
		if ( node == null || ! node.isURI() ) {
			return true;
		}

		return ltbExecCxt.ldcache.ensureAvailability( ltbExecCxt.accessContext, nodeID, prio );
	}

}
