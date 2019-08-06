/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref;

import java.util.Set;


/**
 * This interface represents the result of a finished
 * dereferencing of an HTTP-scheme based URI.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface DereferencingResult
{
	/**
	 * Returns the identifier of the dereferenced URI.
	 */
	public int getURIID ();

	// redirections

	/**
	 * Returns true if the result of the dereferencing
	 * was an HTTP redirection to another URI.
	 */
	public boolean hasBeenRedirected ();

	/**
	 * Returns the HTTP status code of the redirecting
	 * response (if the dereferencing resulted in an
	 * HTTP redirection).
	 *
	 * @throws UnsupportedOperationException when
	 *                    {@link #hasBeenRedirected} is false
	 */
	public int getRedirectionCode () throws UnsupportedOperationException;

	/**
	 * Returns the target URI of the redirecting response
	 * (if the dereferencing resulted in an HTTP redirection).
	 *
	 * @throws UnsupportedOperationException when
	 *                    {@link #hasBeenRedirected} is false
	 */
	public int getRedirectionURI () throws UnsupportedOperationException;


	// discovery of URIs

	/**
	 * Returns true if additional, potentially relevant URIs have been
	 * discovered by processing the result of URI dereferencing.
	 */
	public boolean hasDiscoveredOtherURIs ();

	/**
	 * Returns additional, potentially relevant URIs that have been
	 * discovered by processing the result of URI dereferencing.
	 *
	 * @throws UnsupportedOperationException when
	 *                     {@link #hasDiscoveredOtherURIs} is false
	 */
	public Set<DiscoveredURI> getDiscoveredURIs () throws UnsupportedOperationException;


	// failure

	/**
	 * Return true if the URI dereferencing failed.
	 */
	public boolean isFailure ();

	/**
	 * Returns the cause of the failure (if the dereferencing failed).
	 *
	 * @throws UnsupportedOperationException when {@link #isFailure} is false
	 */
	public Exception getException () throws UnsupportedOperationException;


	// statistics about the dereferencing process that led to this result

	/**
	 * Returns the time (in ms) the corresponding dereferencing task was spent
	 * in the queue. That is the interval between issuing the task and starting
	 * to actual execute it.
	 */
	public long getQueueTime ();

	/**
	 * Returns the time (in ms) required to execute the corresponding
	 * dereferencing task.
	 */
	public long getExecutionTime ();

}
