/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterAssign;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;

import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.dataset.jenacommon.QueriedDatasetWrappingJenaGraph;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.TriplePattern;
import org.squin.dataset.query.arq.iterators.DecodeBindingsIterator;
import org.squin.dataset.query.arq.iterators.EncodeBindingsIterator;
import org.squin.dataset.query.arq.iterators.TriplePatternQueryIter;
import org.squin.dataset.query.arq.iterators.QueryIterAssignWrapper;


/**
 * A {@link com.hp.hpl.jena.sparql.engine.main.OpExecutor} implementation
 * for {@link org.squin.dataset.QueriedDataset} implementations wrapped as
 * {@link org.squin.dataset.jenacommon.QueriedDatasetWrappingJenaGraph} object.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class OpExecutor extends com.hp.hpl.jena.sparql.engine.main.OpExecutor
{
	/**
	 * The factory object that creates this OpExecutor implementation.
	 */
	static final public OpExecutorFactory factory = new OpExecutorFactory()
	{
		public com.hp.hpl.jena.sparql.engine.main.OpExecutor create( ExecutionContext execCxt )
		{
			return new OpExecutor( (IdBasedExecutionContext) execCxt );
		}
	};


	/**
	 * Creates an operator compiler.
	 */
	public OpExecutor ( IdBasedExecutionContext execCxt )
	{
		super( execCxt );
	}


	// operations

	@Override
	public QueryIterator execute ( OpBGP opBGP, QueryIterator input )
	{
		if (    opBGP.getPattern().isEmpty()
		     || ! (execCxt.getDataset().getDefaultGraph() instanceof QueriedDatasetWrappingJenaGraph) )
		{
			return super.execute( opBGP, input );
		}

		IdBasedExecutionContext ourExecCxt = (IdBasedExecutionContext) execCxt;
		VarDictionary varDict = ourExecCxt.varDict;
		NodeDictionary nodeDict = ourExecCxt.nodeDict;

		Iterator<SolutionMapping> qIt = new EncodeBindingsIterator( input, ourExecCxt );
		for ( com.hp.hpl.jena.graph.Triple t : opBGP.getPattern().getList() ) {
			qIt = new TriplePatternQueryIter( encode(t,varDict,nodeDict), qIt, ourExecCxt );
		}

		return new DecodeBindingsIterator( qIt, ourExecCxt );
	}

	@Override
	protected QueryIterator execute ( OpAssign opAssign, QueryIterator input )
	{
		QueryIterAssign in = (QueryIterAssign) super.execute( opAssign, input );
		return new QueryIterAssignWrapper( in, (IdBasedExecutionContext) execCxt );
	}

	// helper methods

	final protected TriplePattern encode ( com.hp.hpl.jena.graph.Triple tp, VarDictionary varDict, NodeDictionary nodeDict )
	{
		boolean sIsVar = Var.isVar( tp.getSubject() );
		boolean pIsVar = Var.isVar( tp.getPredicate() );
		boolean oIsVar = Var.isVar( tp.getObject() );

		return new TriplePattern( sIsVar, (sIsVar) ? varDict.getId((Var)tp.getSubject()) : nodeDict.createId(tp.getSubject()),
		                          pIsVar, (pIsVar) ? varDict.getId((Var)tp.getPredicate()) : nodeDict.createId(tp.getPredicate()),
		                          oIsVar, (oIsVar) ? varDict.getId((Var)tp.getObject()) : nodeDict.createId(tp.getObject()) );
	}

}
