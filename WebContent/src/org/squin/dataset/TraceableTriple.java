/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset;


/**
 * This class represents a (ID-encoded) RDF triple,
 * combined with its provenance.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
abstract public class TraceableTriple extends Triple
{
	// initialization

	protected TraceableTriple ( int s, int p, int o )
	{
		super( s, p, o );
	}


	// abstract methods

	abstract public TripleProvenance getProvenance ();

}
