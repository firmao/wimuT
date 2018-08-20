/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import org.squin.common.Priority;
import org.squin.common.TemporallyPrioritizedObject;


/**
 * Base class for {@link java.lang.Comparable} implementations of the
 * {@link TemporallyPrioritizedObject} interface.
 * For two objects with a different priority it holds that the object with the
 * higher priority is "less than" the other object. For two objects with the
 * same priority it holds that the object with the older timestamp is "less
 * than" the other object. Two objects with the same priority and the same
 * timestamp are equal.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class ComparableTemporallyPrioritizedObjectBase<T extends TemporallyPrioritizedObject>
                        extends TemporallyPrioritizedObjectBase
                        implements Comparable<T>
{
	// initialization

	public ComparableTemporallyPrioritizedObjectBase ( Priority priority )
	{
		super( priority );
	}


	// implementation of the Comparable interface

	public int compareTo ( TemporallyPrioritizedObject other )
	{
		int priorityBasedResult = other.getPriority().numericalValue - this.getPriority().numericalValue;
		if ( priorityBasedResult != 0 ) {
			return priorityBasedResult;
		}
		else {
			long t1 = this.getTimestamp();
			long t2 = other.getTimestamp();
			if ( t1 == t2 ) {
				return 0;
			} else if ( t1 < t2 ) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
