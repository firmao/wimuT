/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * Represents any kind of tasks that produce a result of type R.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface Task<R> extends TemporallyPrioritizedObject, Comparable<Task>, Runnable
{
	/**
	 * Registers the given listener with this task.
	 *
	 * @param listener the listener to be registered
	 * @param priority priority of the listener (listeners registered with
	 *                 a higher priority are notified first)
	 * @throws IllegalStateException if it is impossible to register the
	 *                               listener; e.g. because this task is
	 *                               already notifying its listeners or
	 *                               it has already been completed
	 */
	public void registerListener ( TaskListener<R> listener, Priority priority ) throws IllegalStateException;

	/**
	 * Returns true if this task is currently being executed.
	 */
	public boolean isRunning ();

	/**
	 * Returns true if this task is currently notifying its listeners.
	 */
	public boolean isNotifying ();

	/**
	 * Returns true if the execution of this task has been completed.
	 */
	public boolean isDone ();

	/**
	 * Returns the result of this task once it is finished.
	 * As long as the execution of this task did not finish, this method returns
	 * null.
	 */
	public R getResult ();

	/**
	 * Returns the time (as a timestamp) when the task was actually started
	 * to execute.
	 */
	public long getExecutionStartTimestamp ();
}
