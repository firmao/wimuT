/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * This interface represents a listener that handles (the result of) a
 * finished tasks.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface TaskListener<R>
{
	/**
	 * Handles the result of tasks that completed execution.
	 * Notice, the execution might not have been successful.
	 *
	 * @param result the result of the completed task
	 */
	public void handleCompletedTask ( R result );

	/**
	 * Handles the result of tasks that definitely failed to complete.
	 *
	 * @param result the result of the failed task
	 */
	public void handleFailedTask ( R result );
}
