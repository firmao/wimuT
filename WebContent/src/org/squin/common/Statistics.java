/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * This is a common interface for all classes that represent statistics.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface Statistics
{
	/**
	 * Returns the value of the given attribute of this statistics.
	 */
	public Object getAttributeValue ( String attrName );

	/**
	 * Returns the value of the given attribute of this statistics,
	 * assuming it is an Integer.
	 *
	 * @throws NoSuchElementException if this Statistics object does not contain
	 *                                an attribute with the given name
	 * @throws ClassCastException if the given attribute value cannot be parsed
	 *                            as an int
	 */
	public int getAttributeValueAsInteger ( String attrName ) throws NoSuchElementException, ClassCastException;

	/**
	 * Returns the value of the given attribute of this statistics,
	 * assuming it is a Long.
	 *
	 * @throws NoSuchElementException if this Statistics object does not contain
	 *                                an attribute with the given name
	 * @throws ClassCastException if the given attribute value cannot be parsed
	 *                            as a long
	 */
	public long getAttributeValueAsLong ( String attrName ) throws NoSuchElementException, ClassCastException;

	/**
	 * Returns the value of the given attribute of this statistics,
	 * assuming it is a Double.
	 *
	 * @throws NoSuchElementException if this Statistics object does not contain
	 *                                an attribute with the given name
	 * @throws ClassCastException if the given attribute value cannot be parsed
	 *                            as an double
	 */
	public double getAttributeValueAsDouble ( String attrName ) throws NoSuchElementException, ClassCastException;

	/**
	 * Returns the value of the given attribute of this statistics,
	 * assuming it is a (nested) {@link Statistics} object.
	 *
	 * @throws NoSuchElementException if this Statistics object does not contain
	 *                                an attribute with the given name
	 * @throws ClassCastException if the given attribute value is not a
	 *                            {@link Statistics} object
	 */
	public Statistics getAttributeValueAsStatistics ( String attrName ) throws NoSuchElementException, ClassCastException;

	/**
	 * Prints this statistics to the given stream, one attribute value pair on
	 * each line.
	 * For values that are {@link Statistics} objects again, this method is
	 * called recursively (with an increased indentation level).
	 */
	public void print ( PrintStream out, int indentLevel );

	/**
	 * This methods returns the values of this statistics as a string of
	 * comma separated values.
	 * For values that are {@link Statistics} objects again, this method is
	 * called recursively.
	 */
	public String toStringCSV ();

	/**
	 * This methods returns the values of this statistics as a string of
	 * comma separated values, omitting all attributes that are not in the
	 * given attribute array.
	 * For values that are {@link Statistics} objects again, this method is
	 * called recursively (if their attribute name is in the given array).
	 *
	 * @param attrNames names of those attributes that have to be added to
	 *                  the string, if this parameter is null then all
	 *                  attributes are added
	 */
	public String toStringCSV ( String[] attrNames );

}
