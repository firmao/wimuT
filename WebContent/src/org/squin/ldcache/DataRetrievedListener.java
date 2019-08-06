/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache;


/**
 * Interface of a listener which will be notified when an asynchronous
 * retrieval of Web data has been finished.
 * Such a listener has to be registered by requesting the data retrieval via
 * the method
 * {@link LinkedDataCache#ensureAvailability(AccessContext,int, org.squin.common.Priority,DataRetrievedListener)}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface DataRetrievedListener
{
	public void ensureAvailabilityFinished ( int uriID );
}
