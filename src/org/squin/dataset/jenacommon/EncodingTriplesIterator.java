/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import java.util.Iterator;

import org.squin.dataset.Triple;


/**
 * This iterator converts an iterator over RDF triples represented using the
 * Jena API to our ID-encoded representation.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class EncodingTriplesIterator implements Iterator<Triple>
{
	// members

	final protected NodeDictionary nodeDict;
	final protected Iterator<com.hp.hpl.jena.graph.Triple> inputIterator;


	// initialization

	public EncodingTriplesIterator ( NodeDictionary nodeDict, Iterator<com.hp.hpl.jena.graph.Triple> inputIterator )
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
	final public Triple next ()
	{
		return encode( nodeDict, inputIterator.next() );
	}

	@Override
	final public void remove ()
	{
		inputIterator.remove();
	}


	// operations

	static public Triple encode ( NodeDictionary nodeDict, com.hp.hpl.jena.graph.Triple jenaTriple )
	{
		return new Triple( nodeDict.createId(jenaTriple.getSubject()),
		                   nodeDict.createId(jenaTriple.getPredicate()),
		                   nodeDict.createId(jenaTriple.getObject()) );
	}

}
