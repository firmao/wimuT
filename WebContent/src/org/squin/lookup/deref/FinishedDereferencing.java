/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref;

import java.util.Date;


/**
 * This interface represents a finished dereferencing
 * of an HTTP-scheme based URI.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface FinishedDereferencing extends DereferencingStatus
{
	/**
	 * Returns the time at which the corresponding URI dereferencing was
	 * finished.
	 *
	 * @return the difference, measured in milliseconds, between the finish
	 *         time and midnight, January 1, 1970 UTC
	 */
	public long getFinishTimeMillis ();

	/**
	 * Returns the result of the corresponding URI dereferencing.
	 */
	public DereferencingResult getResult ();
}
