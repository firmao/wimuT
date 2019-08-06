/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Base class for implementations of {@link LockableTaskStatus}.
 * This base class implements the {@link java.util.concurrent.locks.Lock}
 * interface so that sub-classes only have to implement the
 * {@link org.squin.common.TaskStatus} interface.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public abstract class LockableTaskStatusBase extends ReentrantLock
                                             implements LockableTaskStatus
{
	public boolean isLocked ()
	{
		return super.isLocked();
	}

	public boolean isLockedByCurrentThread ()
	{
		return isHeldByCurrentThread();
	}

}
