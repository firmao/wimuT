/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;

import java.util.Comparator;


/**
 * A {@link java.util.Comparator} for {@link TemporallyPrioritizedObject}s.
 * For two objects with a different priority it holds that the object with the
 * higher priority is "less than" the other object. For two objects with the
 * same priority it holds that the object with the older timestamp is "less
 * than" the other object. Two objects with the same priority and the same
 * timestamp are equal.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class TemporalPriorityBasedComparator<T extends TemporallyPrioritizedObject> extends PriorityBasedComparator<T>
{
	// implementation of the Comparator interface

	@Override
	public int compare ( T o1, T o2 )
	{
		int priorityBasedResult = super.compare( o1, o2 );
		if ( priorityBasedResult != 0 ) {
			return priorityBasedResult;
		}
		else {
			long t1 = ( (TemporallyPrioritizedObject) o1 ).getTimestamp();
			long t2 = ( (TemporallyPrioritizedObject) o2 ).getTimestamp();
			if ( t1 == t2 ) {
				return 0;
			} else if ( t1 < t2 ) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	@Override
	public boolean equals ( Object obj )
	{
		return ( obj instanceof TemporalPriorityBasedComparator );
	}

}
