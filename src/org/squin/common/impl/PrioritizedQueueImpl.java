/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.squin.common.PrioritizedQueue;
import org.squin.common.Priority;


/**
 * Implementation of {@link PrioritizedQueue}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class PrioritizedQueueImpl<T> implements PrioritizedQueue<T>
{
	// members

	final protected List<Queue<T>> queues;


	// initialization

	public PrioritizedQueueImpl ()
	{
		queues = new ArrayList<Queue<T>> ( Priority.highestPriority.numericalValue + 1 );
		for ( int i = 0; i <= Priority.highestPriority.numericalValue ; i++ ) {
			queues.add( i, new LinkedList<T>() );
		}
	}


	// implementation of the PrioritizedQueue interface

	public void queue ( T t, Priority p )
	{
		assert ! contains( t );

		Queue<T> queue = queues.get( p.numericalValue );
		synchronized ( queue ) {
			queue.offer( t );
		}
	}

	public T poll ()
	{
		for ( int i = Priority.highestPriority.numericalValue; i >= 0; i-- ) {
			Queue<T> queue = queues.get( i );
			synchronized ( queue ) {
				T t = queue.poll();
				if ( t != null ) {
					return t;
				}
			}
		}

		return null;
	}

	public boolean contains ( T t )
	{
		return getContainingQueue( t ) != null;
	}


	// helpers

	protected Queue<T> getContainingQueue ( T t )
	{
		for ( Queue<T> queue : queues ) {
			synchronized ( queue ) {
				if ( queue.contains(t) ) {
					return queue;
				}
			}
		}
		return null;
	}

}
