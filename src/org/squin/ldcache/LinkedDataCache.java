/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.squin.common.Priority;
import org.squin.common.StatisticsProvider;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;


/**
 * This interface represents an active cache of Linked Data from the Web.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface LinkedDataCache extends StatisticsProvider
{
	/**
	 * Registers a new {@link AccessContext} object and returns this object.
	 * A {@link AccessContext} object is required to access a Linked Data
	 * cache and it enables the cache to internally distinguish different
	 * contexts of access. Different contexts should be registered for
	 * different query executions.
	 * Do not forget to call the method {@link #unregisterAccessContext} in
	 * order to unregister {@link AccessContext} objects once the corresponding
	 * access context ceases to exist.
	 */
	public AccessContext registerAccessContext ();

	/**
	 * Unregisters an access context.
	 * After unregistering an {@link AccessContext} object, that object cannot
	 * be used anymore to access the Linked Data cache.
	 *
	 * @param ac the access context to unregister
	 * @see #registerAccessContext
	 */
	public void unregisterAccessContext ( AccessContext ac );

	/**
	 * This method returns all triples that match the given pattern in all RDF
	 * graphs currently accessible in this cache.
	 * Use {@link org.squin.dataset.Triple#UNKNOWN_IDENTIFIER} as wildcard.
	 */
	public Iterator<Triple> find ( AccessContext ac, int s, int p, int o );

	/**
	 * This method returns all triples, combined with their provenance, that
	 * match the given pattern in all RDF graphs currently accessible in this
	 * cache.
	 * Use {@link org.squin.dataset.Triple#UNKNOWN_IDENTIFIER} as wildcard.
	 */
	public Iterator<TraceableTriple> findWithProvenance ( AccessContext ac, int s, int p, int o );

	/**
	 * Ensures that data from the Web about the given URI is available in the
	 * cache.
	 *
	 * @param ac the access context to which this request belongs
	 * @param uriID identifier of the URI for which the data is requested
	 * @param priority priority for this request
	 * @return true if the requested data is already in the cache; false if the
	 *         asynchronous retrieval of the requested data has been initiated
	 */
	public boolean ensureAvailability ( AccessContext ac, int uriID, Priority priority );

	/**
	 * Ensures that data from the Web about the given URI is available in the
	 * cache.
	 * This method enables the calling component to register a listener which
	 * will be notified when the asynchronous retrieval of the requested data
	 * has been finished (in case this method initiates such an asynchronous
	 * data retrieval task).
	 *
	 * @param ac the access context to which this request belongs
	 * @param uriID identifier of the URI for which the data is requested
	 * @param priority priority for this request
	 * @param l the listener
	 * @return true if the requested data is already in the cache; false if the
	 *         asynchronous retrieval of the requested data has been initiated
	 */
	public boolean ensureAvailability ( AccessContext ac, int uriID, Priority priority, DataRetrievedListener l );

	/**
	 * Shuts down this cache.
	 *
	 * @param timeoutInMilliSeconds timeout (in milliseconds) after which
	 *                              the shut down process is interrupted
	 *                              and a TimeoutException is thrown
	 * @throws TimeoutException when the shut down process takes
	 *                          longer than the given timeout
	 * @throws ExecutionException when completing the shut down
	 *                            fails for another reason
	 */
	public void shutdownNow ( long timeoutInMilliSeconds ) throws ExecutionException, TimeoutException;

	/**
	 * Returns true if this cache has been shut down completely.
	 */
	public boolean isShutdown ();

}
