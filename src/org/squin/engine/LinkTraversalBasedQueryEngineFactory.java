/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Context;


/**
 * A factory object that creates a {@link LinkTraversalBasedQueryEngine}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkTraversalBasedQueryEngineFactory implements QueryEngineFactory
{
	// implementation of the QueryEngineFactory interface

	public boolean accept ( Query query, DatasetGraph ds, Context cxt )
	{
		return wrapsLinkedDataCache( ds );
	}

	public boolean accept ( Op op, DatasetGraph ds, Context cxt )
	{
		return wrapsLinkedDataCache( ds );
	}

	public Plan create ( Query query, DatasetGraph dataset, Binding initialBinding, Context context )
	{
		LinkTraversalBasedQueryEngine engine = new LinkTraversalBasedQueryEngine( query, dataset, initialBinding, context );
		return engine.getPlan();
	}

	public Plan create ( Op op, DatasetGraph dataset, Binding initialBinding, Context context )
	{
		LinkTraversalBasedQueryEngine engine = new LinkTraversalBasedQueryEngine( op, dataset, initialBinding, context );
		return engine.getPlan();
	}


	// helper methods

	private boolean wrapsLinkedDataCache ( DatasetGraph ds )
	{
		return ( ds instanceof LinkedDataCacheWrappingDatasetGraph );
	}
}
