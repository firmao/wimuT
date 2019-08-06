/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.TripleProvenance;


/**
 * This class extends the ID-based representation of an RDF triple with
 * a fourth element which denotes the RDF graph that contained the triple.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class Quad extends TraceableTriple
{
	// members

	/** The fourth element which makes this RDF triple a quad. */
	final public IndexedRDFGraph src;


	// initialization

// 	public Quad ( long s, long p, long o, IndexedRDFGraph src )
	public Quad ( int s, int p, int o, IndexedRDFGraph src )
	{
		super( s, p, o );
		this.src = src;
	}

	public Quad ( Triple t, IndexedRDFGraph src )
	{
		this( t.s, t.p, t.o, src );
	}

// 	public Quad ( Quad q )
// 	{
// 		this( q.s, q.p, q.o, q.src );
// 	}


	// implementation of the TraceableTriple abstract methods

	public TripleProvenance getProvenance ()
	{
		return new TripleProvenanceImpl( src.prv );
	}

}
