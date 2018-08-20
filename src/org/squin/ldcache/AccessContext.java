/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache;


/**
 * This interface represents a context in which a {@link LinkedDataCache} will
 * be accessed.
 * A {@link AccessContext} object is required for several methods of the
 * {@link LinkedDataCache} and it enables the cache to internally distinguish
 * different contexts of access. Different contexts should be registered for
 * different query executions. Once an access context ceases to exist it is
 * necessary to unregister the corresponding {@link AccessContext} object via
 * the method {@link LinkedDataCache#unregisterAccessContext}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface AccessContext
{
	/**
	 * Returns the {@link LinkedDataCache} for which this access context has
	 * been registered.
	 */
	public LinkedDataCache getCache ();

	/**
	 * Returns true if this access context is (still) registered.
	 */
	public boolean isRegistered ();
}
