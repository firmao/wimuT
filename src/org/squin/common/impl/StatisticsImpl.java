/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.squin.common.Statistics;


/**
 * This is a default implementations of the {@link Statistics} interface.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class StatisticsImpl implements Statistics
{
	static public class Attribute
	{
		public String name;
		public Object value;
		public Attribute ( String name, Object value ) { this.name = name; this.value = value; }
	}

	static public class AttributeList extends ArrayList<Attribute>
	{
		public AttributeList add ( String name, Object value )
		{
			add( new Attribute(name,value) );
			return this;
		}
	}

	// members

	final static public String indentation = "  ";
	final private List<Attribute> attributes;


	// initialization

	public StatisticsImpl ( List<Attribute> attributes )
	{
		assert attributes != null;
		this.attributes = attributes;
	}


	// implementation of the Statistics interface

	public Object getAttributeValue ( String attrName )
	{
		for ( Attribute a : attributes ) {
			if ( a.name.equals(attrName) ) {
				return a.value;
			}
		}
		return null;
	}

	public int getAttributeValueAsInteger ( String attrName ) throws NoSuchElementException, ClassCastException
	{
		Object v = getAttributeValue( attrName );
		if ( v == null ) {
			throw new NoSuchElementException( "This statistics object (" + this + ") does not contain an attribute named '" + attrName + "'." );
		}
		return ( (Integer) v ).intValue();
	}

	public long getAttributeValueAsLong ( String attrName ) throws NoSuchElementException, ClassCastException
	{
		Object v = getAttributeValue( attrName );
		if ( v == null ) {
			throw new NoSuchElementException( "This statistics object (" + this + ") does not contain an attribute named '" + attrName + "'." );
		}
		return ( (Long) v ).longValue();
	}

	public double getAttributeValueAsDouble ( String attrName ) throws NoSuchElementException, ClassCastException
	{
		Object v = getAttributeValue( attrName );
		if ( v == null ) {
			throw new NoSuchElementException( "This statistics object (" + this + ") does not contain an attribute named '" + attrName + "'." );
		}
		return ( (Double) v ).intValue();
	}

	public Statistics getAttributeValueAsStatistics ( String attrName ) throws NoSuchElementException, ClassCastException
	{
		Object v = getAttributeValue( attrName );
		if ( v == null ) {
			throw new NoSuchElementException( "This statistics object (" + this + ") does not contain an attribute named '" + attrName + "'." );
		}
		return (Statistics) v;
	}

	public void print ( PrintStream out, int indentLevel )
	{
		String ind = "";
		for ( int i = 0; i < indentLevel; i++ ) {
			ind += indentation;
		}

		for ( Attribute a : attributes ) {
			out.print( ind + a.name + ": " );
			if ( a.value instanceof Statistics ) {
				out.println();
				( (Statistics) a.value ).print( out, indentLevel + 1 );
			}
			else {
				out.println( a.value.toString() );
			}
		}
	}

	public String toStringCSV ()
	{
		return toStringCSV( null );
	}

	public String toStringCSV ( String[] attrNames )
	{
		List filter = ( attrNames == null ) ? null : Arrays.asList( attrNames );
		String result = "";
		for ( Attribute a : attributes ) {
			if ( filter == null || filter.contains(a.name) ) {
				result += getAttributeValueAsStringForCSV( a.value, attrNames );
			}
		}
		return result;
	}


	// helpers

	protected String getAttributeValueAsStringForCSV ( Object value, String[] attrNames )
	{
		if ( value instanceof Statistics ) {
			return ( (Statistics) value ).toStringCSV( attrNames );
		}

		return value.toString() + ",";
	}


	static private boolean triedToInitiateObjectProfiling = false;
	static private Method sizeofMethod = null;

	static public boolean isObjectProfilingPossible ()
	{
		if ( ! triedToInitiateObjectProfiling ) {
			triedToInitiateObjectProfiling = true;
			try {
				Class profilerClass = Class.forName( "com.vladium.utils.ObjectProfiler" );
				sizeofMethod = profilerClass.getMethod( "sizeof", Object.class );
			}
			catch ( Exception e ) {
				sizeofMethod = null;
				return false;
			}
		}
		return sizeofMethod != null;
	}

	static public int sizeof ( Object obj ) throws UnsupportedOperationException
	{
		if ( ! isObjectProfilingPossible() ) {
			throw new UnsupportedOperationException( "Object profiling is not possible." );
		}

		try {
			Object result = sizeofMethod.invoke( null, obj );
			Integer size = (Integer) result;
			return size.intValue();
		}
		catch ( Exception e ) {
			return -1;
		}
	}

}
