/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.Quad;

import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;


/**
 * An implementation of an ARQ {@link com.hp.hpl.jena.sparql.core.DatasetGraph}
 * that wraps a {@link org.squin.ldcache.LinkedDataCache} to enable making use
 * of the ARQ query processing framework.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkedDataCacheWrappingDatasetGraph extends DatasetGraphBase
                                                 implements DatasetGraph
{
	final public JenaIOBasedLinkedDataCache ldcache;
	protected Graph dfltGraph;

	public LinkedDataCacheWrappingDatasetGraph ( JenaIOBasedLinkedDataCache ldcache )
	{
		assert ldcache != null;
		this.ldcache = ldcache;
	}

	// implementation of the abstract methods in DatasetGraphBase

	/**
	 * Returns null because a {@link org.squin.ldcache.LinkedDataCache} does not
	 * have a default graph.
	 */
	public Graph getDefaultGraph ()
	{
		if ( dfltGraph == null ) {
			dfltGraph = ldcache.asJenaGraph();
		}
		return dfltGraph;
	}

	public Graph getGraph ( Node graphNode )
	{
		throw new UnsupportedOperationException();
	}

	public Iterator<Quad> find ( Node g, Node s, Node p , Node o )
	{
		throw new UnsupportedOperationException();
	}

	public Iterator<Quad> findNG ( Node g, Node s, Node p , Node o )
	{
		throw new UnsupportedOperationException();
	}

	public Iterator<Node> listGraphNodes ()
	{
		throw new UnsupportedOperationException();
	}

}
