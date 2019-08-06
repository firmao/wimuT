/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.util.Context;

import org.squin.dataset.jenacommon.NodeDictionary;


/**
 * An extension of the usual {@link com.hp.hpl.jena.sparql.engine.ExecutionContext}
 * class to be used with {@link QueryEngine} and {@link OpExecutor}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class IdBasedExecutionContext extends ExecutionContext
{
	// members

	final public NodeDictionary nodeDict;
	final public VarDictionary varDict;
	final public boolean recordProvenance;


	// initialization

	public IdBasedExecutionContext ( NodeDictionary nodeDict,
	                                 VarDictionary varDict,
	                                 boolean recordProvenance,
	                                 Context params,
	                                 Graph activeGraph,
	                                 DatasetGraph dataset,
	                                 OpExecutorFactory factory )
	{
		super( params, activeGraph, dataset, factory );
		this.nodeDict = nodeDict;
		this.varDict = varDict;
		this.recordProvenance = recordProvenance;
	}

}
