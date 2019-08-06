/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import org.squin.common.PrioritizedObject;
import org.squin.common.Priority;


/**
 * Base class for implementations of the {@link PrioritizedObject} interface.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class PrioritizedObjectBase implements PrioritizedObject
{
	// members

	private Priority priority;


	// initialization

	public PrioritizedObjectBase ( Priority priority )
	{
		assert priority != null;
		this.priority = priority;
	}


	// accessor methods

	/**
	 * Upgrades the priority of this object to the given priority.
	 *
	 * @param newPriority the new priority
	 * @throws IllegalArgumentException the given priority is less important
	 *                                  than the current priority
	 */
	public void upgradePriority ( Priority newPriority ) throws IllegalArgumentException
	{
		if ( newPriority.numericalValue < priority.numericalValue ) {
			throw new IllegalArgumentException( "Cannot upgrade to a less important priority (" + newPriority.toString() + "; current priority is: " + priority.toString() + ")." );
		}

		priority = newPriority;
	}


	// implementation of the PrioritizedObject interface

	public Priority getPriority ()
	{
		return priority;
	}

}
