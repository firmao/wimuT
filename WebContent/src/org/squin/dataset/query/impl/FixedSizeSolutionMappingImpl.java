/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.impl;

import org.squin.dataset.query.BindingProvenance;
import org.squin.dataset.query.SolutionMapping;


/**
 * This class implements {@link SolutionMapping} using a fixed size for the
 * actual mapping.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class FixedSizeSolutionMappingImpl implements SolutionMapping
{
	// members

	final protected int[] map;
	final protected BindingProvenance [] provenance;


	// initialization

	public FixedSizeSolutionMappingImpl ( int size )
	{
		map = new int[size];
		provenance = new BindingProvenance[size];
		for ( int i = 0; i < size; ++i ) {
			map[i] = SolutionMapping.UNBOUND;
			provenance[i] = null;
		}
	}

	/**
	 * Copy constructor which assumes that the given {@link SolutionMapping} is
	 * actually a {@link FixedSizeSolutionMappingImpl}.
	 */
	public FixedSizeSolutionMappingImpl ( SolutionMapping template )
	{
		FixedSizeSolutionMappingImpl input = (FixedSizeSolutionMappingImpl) template;

		int size = input.map.length;
		map = new int[size];
		provenance = new BindingProvenance[size];
		for ( int i = 0; i < size; ++i ) {
			map[i] = input.map[i];
			provenance[i] = input.provenance[i];
		}
	}


	// implementation of the SolutionMapping interface

	public void set ( int varId, int valueId, BindingProvenance prv )
	{
		map[varId] = valueId;
		provenance[varId] = prv;
	}

	public boolean contains ( int varId )
	{
		return ( map[varId] != SolutionMapping.UNBOUND );
	}

	public int get ( int varId )
	{
		return map[varId];
	}

	public BindingProvenance getProvenance ( int varId )
	{
		return provenance[varId];
	}

	public int size ()
	{
		return map.length;
	}


	// redefinition of Object methods

	@Override
	public String toString ()
	{
		String s = "SolutionMapping(";

		int size = map.length;
		for ( int i = 0; i < size; ++i )
		{
			if ( map[i] != SolutionMapping.UNBOUND ) {
				s += String.valueOf(i) + "->" + String.valueOf(map[i]) + " ";
			}
		}

		s += ")";
		return s;
	}

}
