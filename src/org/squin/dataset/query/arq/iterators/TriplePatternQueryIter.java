/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openjena.atlas.lib.Closeable;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.jenacommon.QueriedDatasetWrappingJenaGraph;
import org.squin.dataset.query.BindingProvenance;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.TriplePattern;
import org.squin.dataset.query.impl.FixedSizeSolutionMappingImpl;
import org.squin.dataset.query.arq.IdBasedExecutionContext;


/**
 * A query iterator that provides ID-based solution mappings for an ID-based
 * triple pattern which are compatible with the solutions mappings provided
 * by an input operator.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class TriplePatternQueryIter implements Iterator<SolutionMapping>, Closeable
{
	// members

	final protected IdBasedExecutionContext execCxt;

	/** the input iterator consumed by this one */
	final protected Iterator<SolutionMapping> input;

	/** the triple pattern matched by this iterator */
	final protected TriplePattern tp;

	/** the solution mapping currently consumed from the input iterator */
	protected SolutionMapping currentInputMapping = null;

	/**
	 * The current query pattern is the triple pattern of this iterator
	 * (see {@link #tp} substituted with the bindings provided by the
	 * current solution mapping consumed from the input iterator (ie by
	 * {@link #currentInputMapping}).
	 */
	protected TriplePattern currentQueryPattern = null;

	/**
	 * an iterator over all triples that match the current query pattern
	 * (see {@link #currentQueryPattern}) in the queried dataset
	 */
	protected Iterator<? extends Triple> currentMatches = null;


	// initialization

	public TriplePatternQueryIter ( TriplePattern tp, Iterator<SolutionMapping> input, IdBasedExecutionContext execCxt )
	{
		this.tp = tp;
		this.input = input;
		this.execCxt = execCxt;
	}


	// implementation of the Iterator interface

	public boolean hasNext ()
	{
		while ( currentMatches == null || ! currentMatches.hasNext() )
		{
			if ( ! input.hasNext() ) {
				return false;
			}

			QueriedDataset queriedDataset = ( (QueriedDatasetWrappingJenaGraph) execCxt.getActiveGraph() ).queriedDataset;

			currentInputMapping = input.next();
			currentQueryPattern = substitute( tp, currentInputMapping );

			if ( execCxt.recordProvenance ) {
				currentMatches = queriedDataset.findWithProvenance( (currentQueryPattern.sIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.s,
				                                                    (currentQueryPattern.pIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.p,
				                                                    (currentQueryPattern.oIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.o );
			} else {
				currentMatches = queriedDataset.find( (currentQueryPattern.sIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.s,
				                                      (currentQueryPattern.pIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.p,
				                                      (currentQueryPattern.oIsVar) ? Triple.UNKNOWN_IDENTIFIER : currentQueryPattern.o );
			}
		}

		return true;
	}

	public SolutionMapping next ()
	{
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		// Create the next solution mapping by i) copying the mapping currently
		// consumed from the input iterator and ii) by binding the variables in
		// the copy corresponding to the currently matching triple (currentMatch).
		Triple currentMatch = currentMatches.next();
		BindingProvenance currentMatchProvenance = execCxt.recordProvenance ? new BindingProvenanceImpl( (TraceableTriple) currentMatch, tp ) : null;
		SolutionMapping result = new FixedSizeSolutionMappingImpl( currentInputMapping );

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

	public void remove ()
	{
		throw new UnsupportedOperationException();
	}


	// implementation of the Closable interface

	public void close ()
	{
		if ( input instanceof Closeable ) {
			( (Closeable) input ).close();
		}
	}


	// helper methods

	/**
	 * Replaces each query variable in the given triple pattern that is bound to
	 * a value in the given solution mapping by this value.
	 */
	static public TriplePattern substitute ( TriplePattern tp, SolutionMapping m )
	{
		int sNew, pNew, oNew;
		boolean sIsVarNew, pIsVarNew, oIsVarNew;
		boolean isBound;
		if ( tp.sIsVar )
		{
			isBound = m.contains( tp.s );
			sNew = ( isBound ) ? m.get(tp.s) : tp.s;
			sIsVarNew = ( ! isBound );
		}
		else
		{
			sNew = tp.s;
			sIsVarNew = false;
		}

		if ( tp.pIsVar )
		{
			isBound = m.contains( tp.p );
			pNew = ( isBound ) ? m.get(tp.p) : tp.p;
			pIsVarNew = ( ! isBound );
		}
		else
		{
			pNew = tp.p;
			pIsVarNew = false;
		}

		if ( tp.oIsVar )
		{
			isBound = m.contains( tp.o );
			oNew = ( isBound ) ? m.get(tp.o) : tp.o;
			oIsVarNew = ( ! isBound );
		}
		else
		{
			oNew = tp.o;
			oIsVarNew = false;
		}

		return new TriplePattern( sIsVarNew, sNew, pIsVarNew, pNew, oIsVarNew, oNew );
	}


	static protected class BindingProvenanceImpl implements BindingProvenance
	{
		final public TraceableTriple matchingTriple;
		final public TriplePattern matchedTriplePattern;

		public BindingProvenanceImpl ( TraceableTriple matchingTriple, TriplePattern matchedTriplePattern )
		{
			this.matchingTriple = matchingTriple;
			this.matchedTriplePattern = matchedTriplePattern;
		}

		public TraceableTriple getMatchingTriple () { return matchingTriple; }
		public TriplePattern getMatchedTriplePattern () { return matchedTriplePattern; }
	}

}
