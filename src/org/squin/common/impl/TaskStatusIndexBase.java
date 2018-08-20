/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.squin.common.Statistics;
import org.squin.common.TaskStatus;


/**
 * Base class for implementations of {@link TaskStatusIndex}.
 * This implementation assumes that L extends S!
 * Attention: This class is not thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public abstract class TaskStatusIndexBase<S extends TaskStatus,L extends LockableTaskStatus> implements TaskStatusIndex<S,L>
{
	// abstract methods to be implemented in sub-classes

	abstract protected S getUnknownSingleton ();

	abstract protected L getNewUnknownStatus ();


	// members

	final protected Map<Integer,L> map = new HashMap<Integer,L> ();

                                
	// implementation of the TaskStatusIndexBase interface

	@SuppressWarnings("unchecked")
	public S getStatus ( int key )
	{
		L s = map.get( Integer.valueOf(key) );
		return ( s == null ) ? getUnknownSingleton() : (S) s;
	}

	@SuppressWarnings("unchecked")
	public S getLockedStatus ( int key )
	{
		L s = map.get( Integer.valueOf(key) );
		if ( s == null ) {
			s = getNewUnknownStatus();
			put( key, s );
		}

		s.lock();
		return (S) s;
	}

	public void unlockStatus ( int key ) throws ConcurrentModificationException, IllegalArgumentException
	{
		L s = map.get( Integer.valueOf(key) );
		if ( s == null ) {
			throw new IllegalArgumentException( "This index doesn't contain a status for index key " + key + "." );
		}

		if ( ! s.isLocked() ) {
			throw new ConcurrentModificationException( "The current status of the given index key " + key + " is not locked." );
		}

		if ( ! s.isLockedByCurrentThread() ) {
			throw new ConcurrentModificationException( "The calling thread does not hold the lock for the current status of the given index key " + key + "." );
		}

		s.unlock();
	}

	public void updateStatus ( int key, L newStatus ) throws ConcurrentModificationException, IllegalArgumentException
	{
		if ( newStatus.isLocked() ) {
			throw new IllegalArgumentException( "The given status is locked." );
		}

		L oldStatus = map.get( Integer.valueOf(key) );
		if ( oldStatus == null || ! oldStatus.isLockedByCurrentThread() ) {
			throw new ConcurrentModificationException( "The calling thread does not hold the lock for the previous status indexed by index key " + key + "." );
		}

		put( key, newStatus );
		oldStatus.unlock();
	}

	public void clear ()
	{
		map.clear();
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "entries", map.size() );
		return new StatisticsImpl( statAttrs );
	}


	// helpers

	protected void put ( int key, L status )
	{
		map.put( key, status );
	}

}
