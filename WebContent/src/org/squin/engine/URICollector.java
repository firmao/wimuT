/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;


/**
 * A utility class that visits all elements of a SPARQL query to provide a set
 * of all URIs mentioned in that query.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class URICollector extends OpVisitorBase
{
	final protected Set<Node> uris = new HashSet<Node> ();
	final protected boolean includePredicates;

	URICollector ( boolean includePredicates )
	{
		this.includePredicates = includePredicates;
	}

	public Set<Node> getURIs ()
	{
		return uris;
	}

	@Override
	public void visit ( OpBGP opBGP )
	{
		collectURIs( opBGP.getPattern() );
	}

	@Override
	public void visit ( OpPath opPath )
	{
		collectURIs( opPath.getTriplePath().getSubject() );
		collectURIs( opPath.getTriplePath().getObject() );
	}

	@Override
	public void visit ( OpQuadPattern quadPattern )
	{
		collectURIs( quadPattern.getGraphNode() );
		collectURIs( quadPattern.getBasicPattern() );
	}

	@Override
	public void visit ( OpGraph opGraph )
	{
		collectURIs( opGraph.getNode() );
	}

	@Override
	public void visit ( OpDatasetNames dsNames )
	{
		collectURIs( dsNames.getGraphNode() );
	}

	final protected void collectURIs ( BasicPattern bgp )
	{
		for ( Triple t : bgp.getList() ) {
			collectURIs( t );
		}
	}

	final protected void collectURIs ( Triple t )
	{
		collectURIs( t.getSubject() );
		if ( includePredicates ) { collectURIs( t.getPredicate() ); }
		collectURIs( t.getObject() );
	}

	final protected void collectURIs ( Node n )
	{
		if ( n.isURI() ) {
			uris.add( n );
		}
	}

}
