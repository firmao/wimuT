/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;

import java.util.Comparator;


/**
 * A {@link java.util.Comparator} for {@link PrioritizedObject}s.
 * For two objects with a different priority it holds that the object with the
 * higher priority is "less than" the other object. Two objects with the same
 * priority are equal.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class PriorityBasedComparator<T extends PrioritizedObject> implements Comparator<T>
{
	// implementation of the Comparator interface

	public int compare ( T o1, T o2 )
	{
		return ( (PrioritizedObject) o2 ).getPriority().numericalValue - ( (PrioritizedObject) o1 ).getPriority().numericalValue;
	}

	@Override
	public boolean equals ( Object obj )
	{
		return ( obj instanceof PriorityBasedComparator );
	}

}
