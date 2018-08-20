/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;


/**
 * A dictionary that assigns identifiers to query variables.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class VarDictionary
{
	// members

	final protected ArrayList<Var> dictId2Var = new ArrayList<Var> ();
	final protected Map<String,Integer> dictVarName2Id = new HashMap<String,Integer> ();


	// accessors

	/**
	 * Returns the query variable identified by the given identifier.
	 *
	 * @throws IllegalArgumentException if the given identifier is unknown to
	 *                                  this dictionary
	 */
	final public Var getVar ( int id ) throws IllegalArgumentException
	{
		Var v = dictId2Var.get( id );

		if ( v == null ) {
			throw new IllegalArgumentException( "The given identifier (" + String.valueOf(id) + ") is unknown." );
		}

		return v;
	}

	/**
	 * Returns the identifier that identifies the given query variable.
	 *
	 * @throws IllegalArgumentException if the given variable is unknown to
	 *                                  this dictionary
	 */
	final public int getId ( Var v ) throws IllegalArgumentException
	{
		Integer i = dictVarName2Id.get( v.getVarName() );

		if ( i == null ) {
			throw new IllegalArgumentException( "The given variable (" + v.getVarName() + ") is unknown." );
		}

		return  i.intValue();
	}

	/**
	 * Returns the number of query variables known by this dictionary.
	 */
	final public int size ()
	{
		return dictId2Var.size();
	}


	// operations

	/**
	 * Returns an identifier that identifies the given query variable.
	 * If there is no identifier for the given query variable yet this method
	 * creates a new identifier and adds it to the dictionary.
	 */
	final public int createId ( Var v )
	{
		int result;
		Integer i = dictVarName2Id.get( v.getVarName() );

		if ( i == null )
		{
			result = dictId2Var.size();
			dictId2Var.add( v );

			assert result < Integer.MAX_VALUE;

			dictVarName2Id.put( v.getVarName(), Integer.valueOf(result) );
		}
		else {
			result = i.intValue();
		}

		return result;
	}

}
