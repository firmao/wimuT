/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

import org.squin.dataset.Triple;


/**
 * This iterator converts the elements of an iterator over our ID-encoded
 * representation of RDF triples to RDF triples represented using the Jena API.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class DecodingTriplesIterator extends NiceIterator<com.hp.hpl.jena.graph.Triple>
                                     implements ExtendedIterator<com.hp.hpl.jena.graph.Triple>
{
	// members

	final protected NodeDictionary nodeDict;
	final protected Iterator<Triple> inputIterator;


	// initialization

	public DecodingTriplesIterator ( NodeDictionary nodeDict, Iterator<Triple> inputIterator )
	{
		this.nodeDict = nodeDict;
		this.inputIterator = inputIterator;
	}


	// implementation of the Iterator interface

	@Override
	final public boolean hasNext ()
	{
		return inputIterator.hasNext();
	}

	@Override
	final public com.hp.hpl.jena.graph.Triple next ()
	{
		return decode( nodeDict, inputIterator.next() );
	}

	@Override
	final public void remove ()
	{
		inputIterator.remove();
	}


	// operations

	static public com.hp.hpl.jena.graph.Triple decode ( NodeDictionary nodeDict, Triple t )
	{
		Node s = nodeDict.getNode( t.s );
		assert s != null;
		Node p = nodeDict.getNode( t.p );
		assert p != null;
		Node o = nodeDict.getNode( t.o );
		assert o != null;

		return com.hp.hpl.jena.graph.Triple.create( s, p, o );
	}

}
