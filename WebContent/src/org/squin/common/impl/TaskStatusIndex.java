/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.ConcurrentModificationException;

import org.squin.common.StatisticsProvider;
import org.squin.common.TaskStatus;


/**
 * Represents an index of {@link LockableTaskStatus} objects.
 * Notice, inserting statuses (for previously unknown index keys) into this index
 * requires getting an unknown status by calling {@link #getLockedStatus} and
 * updating this status by calling {@link #updateStatus}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface TaskStatusIndex<S extends TaskStatus,L extends LockableTaskStatus> extends StatisticsProvider
{
	/**
	 * Returns the current status indexed by the given key.
	 */
	public S getStatus ( int key );

	/**
	 * Locks the current status indexed by the given key and returns the locked
	 * status.
	 * Ensure that you always (even in case of exceptions) unlock the status
	 * using the method {@link #unlockStatus} (or {@link #updateStatus}).
	 */
	public S getLockedStatus ( int key );

	/**
	 * Unlocks the status currently indexed by the given key.
	 *
	 * @throws ConcurrentModificationException if the calling thread does not
	 *                 hold the lock for the status currently indexed by the
	 *                 given key
	 */
	public void unlockStatus ( int key ) throws ConcurrentModificationException;

	/**
	 * Updates the status currently indexed by the given key to the given
	 * status and unlocks the previously indexed status.
	 * The lock of the previous status (i.e. the status which is currently
	 * indexed) must be held by the calling thread.
	 *
	 * @param key the index key in question
	 * @param newStatus the new status to be indexed by the given key (this
	 *                  status must not be locked)
	 * @throws ConcurrentModificationException if the calling thread does not
	 *                 hold the lock for the previous status indexed by the
	 *                 given key
	 * @throws IllegalArgumentException if the given new status is locked
	 */
	public void updateStatus ( int key, L newStatus ) throws ConcurrentModificationException, IllegalArgumentException;

	/**
	 * Clears the index completely.
	 */
	public void clear ();
}
