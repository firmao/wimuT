/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import org.squin.common.Priority;
import org.squin.common.TemporallyPrioritizedObject;


/**
 * Base class for implementations of the {@link TemporallyPrioritizedObject}
 * interface.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class TemporallyPrioritizedObjectBase extends PrioritizedObjectBase
                                             implements TemporallyPrioritizedObject
{
	// members

	final public long timestamp = System.currentTimeMillis();


	// initialization

	public TemporallyPrioritizedObjectBase ( Priority priority )
	{
		super( priority );
	}


	// implementation of the TemporallyPrioritizedObject interface

	public long getTimestamp ()
	{
		return timestamp;
	}

}
