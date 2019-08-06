/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.squin.dataset.QueriedDataset;


/**
 * An implementation of the Jena Graph interface that wraps a queried dataset.
 * Notice, this class is only a fake implementation of the Graph interface.
 * This class allows us to use the ARQ query execution machinery to execute
 * queries over the queried dataset.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueriedDatasetWrappingJenaGraph extends GraphBase
                                             implements Graph
{
	// members

	final public JenaIOBasedQueriedDataset queriedDataset;


	// initialization

	public QueriedDatasetWrappingJenaGraph ( JenaIOBasedQueriedDataset queriedDataset )
	{
		this.queriedDataset = queriedDataset;
	}


	// implementation of the GraphBase abstract methods

	@Override
	protected ExtendedIterator<Triple> graphBaseFind ( TripleMatch m )
	{
		int s = ( m.getMatchSubject() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchSubject() );
		int p = ( m.getMatchPredicate() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchPredicate() );
		int o = ( m.getMatchObject() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchObject() );

		return new DecodingTriplesIterator( queriedDataset.nodeDict, queriedDataset.find(s,p,o) );
	}

	@Override
	public void performAdd ( Triple t )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void performDelete ( Triple t )
	{
		throw new UnsupportedOperationException();
	}

}
