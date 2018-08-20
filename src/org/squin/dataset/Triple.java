/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset;


/**
 * This class provides an ID-based representation of an RDF triple.
 * The RDF triple is represented using identifiers for the three components
 * (subject, predicate, and object).
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class Triple
{
// 	final static public long UNKNOWN_IDENTIFIER = 0;
	final static public int UNKNOWN_IDENTIFIER = 0;


	// members

	/** The identifier for the subject. */
// 	final public long s;
	final public int s;

	/** The identifier for the predicate. */
// 	final public long p;
	final public int p;

	/** The identifier for the object. */
// 	final public long o;
	final public int o;


	// initialization

// 	public Triple ( long s, long p, long o )
	public Triple ( int s, int p, int o )
	{
		assert s >= 0;
		assert p >= 0;
		assert o >= 0;

		this.s = s;
		this.p = p;
		this.o = o;
	}


	// redefinition of Object methods

	@Override
	public boolean equals ( Object obj )
	{
		if ( obj instanceof Triple ) {
			Triple t = (Triple) obj;
			return ( s == t.s && p == t.p && o == t.o );
		}

		return false;
	}

	@Override
	public String toString ()
	{
		return getClass().getSimpleName() + "(" + String.valueOf(s) + "," + String.valueOf(p) + "," + String.valueOf(o) + ")";
	}

}
