/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * Represents objects (such as tasks) that have a priority and a timestamp.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface TemporallyPrioritizedObject extends PrioritizedObject
{
	/**
	 * Returns the timestamp of this object.
	 */
	public long getTimestamp ();
}
