/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;

import com.hp.hpl.jena.sparql.util.Symbol;


/**
 * Represents different priorities.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class Priority extends Symbol
                      implements Comparable<Priority>
{
	final static public Priority HIGH = new Priority( "HIGH", 2 );
	final static public Priority MEDIUM = new Priority( "MEDIUM", 1 );
	final static public Priority LOW = new Priority( "LOW", 0 );
	final static public Priority highestPriority = HIGH;

	// members

	final public int numericalValue;


	// initialization

	protected Priority ( String name, int numericalValue )
	{
		super( Priority.class.getName() + "." + name );
		this.numericalValue = numericalValue;
	}


	// implementation of the Comparable<Priority> interface

	public int compareTo ( Priority other )
	{
		return other.numericalValue - this.numericalValue;
	}
}
