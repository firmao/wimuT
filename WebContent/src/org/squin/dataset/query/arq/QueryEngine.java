/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Context;

import org.squin.dataset.jenacommon.QueriedDatasetWrappingJenaGraph;


/**
 * A SPARQL query engine for {@link org.squin.dataset.QueriedDataset} implementations
 * wrapped as {@link org.squin.dataset.jenacommon.QueriedDatasetWrappingJenaGraph}
 * object.
 * To use this engine you simply have to register it by calling its
 * {@link #register} method.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueryEngine extends QueryEngineMain
{
	final public boolean RECORD_PROVENANCE = false;

	/**
	 * The factory object that creates an {@link QueryEngine}.
	 */
	static final private QueryEngineFactory factory = new QueryEngineFactory()
	{
		public boolean accept ( Query query, DatasetGraph ds, Context cxt ) { return isIdBased( ds ); }

		public boolean accept ( Op op, DatasetGraph ds, Context cxt ) { return isIdBased( ds ); }

		public Plan create ( Query query, DatasetGraph dataset, Binding initialBinding, Context context ) {
			QueryEngine engine = new QueryEngine( query, dataset, initialBinding, context );
			return engine.getPlan();
		}

		public Plan create ( Op op, DatasetGraph dataset, Binding initialBinding, Context context ) {
			QueryEngine engine = new QueryEngine( op, dataset, initialBinding, context );
			return engine.getPlan();
		}

		private boolean isIdBased ( DatasetGraph ds ) { return ( ds.getDefaultGraph() instanceof QueriedDatasetWrappingJenaGraph ); }
	};

	/**
	 * Returns a factory that creates an {@link QueryEngine}.
	 */
	static public QueryEngineFactory getFactory () { return factory; }

	/**
	 * Registers this engine so that it can be selected for query execution.
	 */
	static public void register () { QueryEngineRegistry.addFactory( factory ); }

	/**
	 * Unregisters this engine.
	 */
	static public void unregister () { QueryEngineRegistry.removeFactory( factory ); }


	// initialization methods

	public QueryEngine ( Op op, DatasetGraph dataset, Binding input, Context context )
	{
		super( op, dataset, input, context );
		registerOpExecutor();
	}

	public QueryEngine( Query query, DatasetGraph dataset, Binding input, Context context )
	{
		super( query, dataset, input, context );
		registerOpExecutor();
	}

	private void registerOpExecutor ()
	{
		QC.setFactory( context, OpExecutor.factory );
	}


	// operations

	@Override
	public QueryIterator eval ( Op op, DatasetGraph dsg, Binding input, Context context )
	{
		if ( SUBSTITUE && ! input.isEmpty() ) {
			op = Substitute.substitute( op, input );
		}

		IdBasedExecutionContext execCxt = createExecutionContext ( op, dsg, context );
		return createIteratorChain( op, input, execCxt );
	}


	// helpers

	protected IdBasedExecutionContext createExecutionContext ( Op op, DatasetGraph dsg, Context contextP )
	{
		VarDictionary varDict = initializeVarDictionary( op );
		return new IdBasedExecutionContext( ((QueriedDatasetWrappingJenaGraph) dsg.getDefaultGraph()).queriedDataset.nodeDict,
		                                    varDict,
		                                    RECORD_PROVENANCE,
		                                    contextP,
		                                    dsg.getDefaultGraph(),
		                                    dsg,
		                                    QC.getFactory(contextP) ) ;
	}

	protected QueryIterator createIteratorChain ( Op op, Binding input, IdBasedExecutionContext execCxt )
	{
		QueryIterator qIter1 = QueryIterRoot.create( input, execCxt );
		QueryIterator qIter = QC.execute( op, qIter1, execCxt );
		qIter = QueryIteratorCheck.check( qIter, execCxt ); // check for closed iterators
		return qIter;
	}

	/**
	 * Creates a dictionary of query variables that knows all variables in the
	 * operator tree of which the given operator is root.
	 */
	final protected VarDictionary initializeVarDictionary ( Op op )
	{
		// We cannot call OpVars.allVars(op) directly because it does not
		// consider all variables in sub-operators of OpProject. Hence,
		// we simply strip the solution modifiers and, thus, call the
		// method for the first operator that is not a solution modifier.
		Op tmp = op;
		while ( tmp instanceof OpModifier ) {
			tmp = ( (OpModifier) tmp ).getSubOp();
		}

		VarDictionary varDict = new VarDictionary();
		for ( Var v : OpVars.allVars(tmp) ) {
			varDict.createId( v );
		}
		return varDict;
	}

}
