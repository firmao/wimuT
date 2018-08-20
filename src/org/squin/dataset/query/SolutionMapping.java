/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query;


/**
 * This interface represents an identifier based solution mapping which is a
 * mapping from query variables that are represented by identifiers to values
 * which are also represented by identifiers.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface SolutionMapping
{
	/** A special value for unbound variables. */
	final static public int UNBOUND = 0;

	/**
	 * Sets the (variable, value) pair in the mapping and associates this
	 * binding with the given provenance.
	 */
	void set ( int varId, int valueId, BindingProvenance prv );

	/**
	 * Returns true if the variable specified by the given identifier is bound
	 * to some value in this mapping.
	 */
	boolean contains ( int varId );

	/**
	 * Returns the identifier of the value bound to the specified variable,
	 * or {@link #UNBOUND}.
	 */
	int get ( int varId );

	/**
	 * Returns the provenance of the binding for the specified variable.
	 */
	BindingProvenance getProvenance ( int varId );

	/**
	 * Returns the number of (variable, value) pairs in this mapping.
	 * This method also counts pairs where the variable is unbound.
	 */
	int size ();
}
