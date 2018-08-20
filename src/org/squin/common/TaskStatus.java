/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * This interface represents a status of a task.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface TaskStatus
{
	/**
	 * Returns true if the status of the corresponding task is unknown.
	 */
	public boolean isUnknown ();

	/**
	 * Returns true if the corresponding task is pending.
	 * In most cases a task is pending if it is either queued for execution or
	 * it is currently being executed.
	 */
	public boolean isPending ();

	/**
	 * Returns true if the execution of the corresponding task finished.
	 */
	public boolean isFinished ();
}
