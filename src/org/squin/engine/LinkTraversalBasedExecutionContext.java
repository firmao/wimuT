/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.util.Context;

import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.dataset.query.arq.IdBasedExecutionContext;
import org.squin.dataset.query.arq.VarDictionary;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.LinkedDataCache;


/**
 * An extension of the usual {@link com.hp.hpl.jena.sparql.engine.ExecutionContext}
 * class to be used with {@link LinkTraversalBasedQueryEngine}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkTraversalBasedExecutionContext extends IdBasedExecutionContext
{
	// members

	final public LinkedDataCache ldcache;
	final public AccessContext accessContext;
	final public boolean predicateLookUpEnabled;


	// initialization

	public LinkTraversalBasedExecutionContext ( boolean predicateLookUpEnabled,
	                                            NodeDictionary nodeDict,
	                                            VarDictionary varDict,
	                                            boolean recordProvenance,
	                                            Context params,
	                                            Graph activeGraph,
	                                            LinkedDataCacheWrappingDatasetGraph dataset,
	                                            OpExecutorFactory factory )
	{
		super( nodeDict, varDict, recordProvenance, params, activeGraph, dataset, factory );

		this.predicateLookUpEnabled = predicateLookUpEnabled;
		this.ldcache = dataset.ldcache;
		accessContext = ldcache.registerAccessContext();
		    // TODO: Attention, this implementation does not unregister access
		    //       contexts! We can only hope the LinkedDataCache implementation
		    //       takes care of that, as the default implementation does. To
		    //       properly unregister access contexts registered within the
		    //       scope of the link traversal based query engine it would be
		    //       necessary to hook into a close method of the ARQ query
		    //       processing machinery which would mean the close method of
		    //       a QueryExecution implementation and, thus, would require to
		    //       reimplement QueryExecutionFactory it seems.
	}

}
