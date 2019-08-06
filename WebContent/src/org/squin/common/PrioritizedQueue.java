/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * Represents a prioritized queue of arbitrary objects.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface PrioritizedQueue<T>
{
	/**
	 * Places the given object into this queue, whereas the actual place depends
	 * on the given priority.
	 * A higher priority guarantees a place closer to the front of the queue.
	 */
	public void queue ( T t, Priority p );

	/**
	 * Returns the object from the front of this queue, that is, one of those
	 * objects queued with the highest priority among all objects in the queue
	 * (or null if the queue is empty).
	 */
	public T poll ();

	/**
	 * Returns true if the queue contains the given object.
	 */
	public boolean contains ( T t );
}
