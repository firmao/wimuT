/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

import org.squin.common.Statistics;
import org.squin.common.impl.StatisticsImpl;
import org.squin.dataset.Triple;
import org.squin.dataset.jenacommon.NodeDictionary;


/**
 * A dictionary that assigns identifiers to RDF terms.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class NodeDictionaryImpl implements NodeDictionary
{
	// members

	final protected ArrayList<Node> dictId2Node = new ArrayList<Node> ();
// 	final protected Map<String,Long> dictURINode2Id = new HashMap<String,Long> ();
	final protected Map<String,Integer> dictURINode2Id = new HashMap<String,Integer> ();
// 	final protected Map<String,Long> dictBlankNode2Id = new HashMap<String,Long> ();
	final protected Map<String,Integer> dictBlankNode2Id = new HashMap<String,Integer> ();
// 	final protected Map<String,Long> dictLitNode2Id = new HashMap<String,Long> ();
	final protected Map<String,Integer> dictLitNode2Id = new HashMap<String,Integer> ();


	// implementation of the NodeDictionary interface

// 	synchronized final public Node getNode ( long id )
	synchronized final public Node getNode ( int id )
	{
		if ( id == Triple.UNKNOWN_IDENTIFIER ) {
			return null;
		}

// 		int i = (int) id;
// 		return dictId2Node.get( i-1 );
		return dictId2Node.get( id-1 );
	}

// 	synchronized final public long getId ( Node n )
	synchronized final public int getId ( Node n )
	{
// 		Long id;
		Integer id;
		if ( n.isURI() ) {
			id = dictURINode2Id.get( n.getURI() );
		} else if ( n.isBlank() ) {
			id = dictBlankNode2Id.get( n.getBlankNodeId().getLabelString() );
		} else if ( n.isLiteral() ) {
			id = dictLitNode2Id.get( n.getLiteral().toString(true) );
		} else {
			id = null;
		}

		// We have to check for (id == null) because the access to dict*Node2Id
		// may also return null (if there is no identifier for the given node).
// 		return ( id == null ) ? Triple.UNKNOWN_IDENTIFIER : id.longValue();
		return ( id == null ) ? Triple.UNKNOWN_IDENTIFIER : id.intValue();
	}

// 	synchronized final public long createId ( Node n )
	synchronized final public int createId ( Node n )
	{
// 		long id = getId( n );
		int id = getId( n );

		if ( id == Triple.UNKNOWN_IDENTIFIER )
		{
// 			int i = dictId2Node.size();
			id = dictId2Node.size() + 1;
			dictId2Node.add( n );

// 			assert i < Integer.MAX_VALUE;
			assert id < Integer.MAX_VALUE;
			if ( id == Integer.MAX_VALUE ) {
				throw new Error( "Maximum number of identifiers reached in the node dictionary." );
			}

			if ( n.isURI() ) {
// 				dictURINode2Id.put( n.getURI(), Long.valueOf(i) );
				dictURINode2Id.put( n.getURI(), Integer.valueOf(id) );
			} else if ( n.isBlank() ) {
// 				dictBlankNode2Id.put( n.getBlankNodeId().getLabelString(), Long.valueOf(i) );
				dictBlankNode2Id.put( n.getBlankNodeId().getLabelString(), Integer.valueOf(id) );
			} else { // if ( n.isLiteral() ) {
// 				dictLitNode2Id.put( n.getLiteral().toString(true), Long.valueOf(i) );
				dictLitNode2Id.put( n.getLiteral().toString(true), Integer.valueOf(id) );
			}

// 			id = i;
		}

		return id;
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		int size;
		synchronized ( this ) {
			size = dictId2Node.size();
		}

		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "size", size );
		return new StatisticsImpl( statAttrs );
	}

}
