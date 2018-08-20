/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.concurrent.locks.Lock;
import org.squin.common.TaskStatus;


/**
 * This interface represents a {@link TaskStatus} that can be locked.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface LockableTaskStatus extends TaskStatus, Lock
{
	/**
	 * Returns true if this status is currently locked by any thread.
	 */
	public boolean isLocked ();

	/**
	 * Returns true if this status is currently locked by the current thread.
	 */
	public boolean isLockedByCurrentThread ();
}
