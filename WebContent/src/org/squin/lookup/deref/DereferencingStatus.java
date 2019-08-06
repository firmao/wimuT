/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref;

import org.squin.common.TaskStatus;


/**
 * This interface represents the status of dereferencing an
 * HTTP-scheme based URI.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface DereferencingStatus extends TaskStatus
{
	/**
	 * Returns a finished dereferencing status as
	 * a {@link FinishedDereferencing} object.
	 *
	 * @throws IllegalStateException if the corresponding
	 *                dereferencing is not finished (i.e.
	 *                if {@link TaskStatus#isFinished}
	 *                returns false).
	 */
	public FinishedDereferencing asFinishedDereferencing () throws IllegalStateException;
}
