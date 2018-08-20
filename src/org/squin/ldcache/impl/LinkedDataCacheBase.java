/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache.impl;

import java.util.Iterator;

import org.squin.common.Priority;
import org.squin.dataset.QueriedDataset;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.DataRetrievedListener;
import org.squin.ldcache.LinkedDataCache;


/**
 * Base class for implementations of an active cache of Linked Data.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public abstract class LinkedDataCacheBase implements LinkedDataCache
{
	// members

	final protected AccessContextRegistry acRegistry;
	final protected QueriedDataset dataset;


	// initialization

	public LinkedDataCacheBase ( QueriedDataset dataset )
	{
		assert dataset != null;

		this.dataset = dataset;
		acRegistry = new AccessContextRegistry( this );
	}


	// implementation of the LinkedDataCache interface

	final public AccessContext registerAccessContext ()
	{
		return acRegistry.getNewAccessContext();
	}

	final public void unregisterAccessContext ( AccessContext ac )
	{
		acRegistry.unregister( ac );
	}

	final public Iterator<Triple> find ( AccessContext ac, int s, int p, int o ) throws UnregisteredAccessContextException
	{
		ensureRegistered( ac );
		return find( s, p, o );
	}

	final public Iterator<TraceableTriple> findWithProvenance ( AccessContext ac, int s, int p, int o ) throws UnregisteredAccessContextException
	{
		ensureRegistered( ac );
		return findWithProvenance( s, p, o );
	}

	final public boolean ensureAvailability ( AccessContext ac, int uriID, Priority prio ) throws UnregisteredAccessContextException
	{
		return ensureAvailability( ac, uriID, prio, null );
	}

	final public boolean ensureAvailability ( AccessContext ac, int uriID, Priority prio, DataRetrievedListener l ) throws UnregisteredAccessContextException
	{
		ensureRegistered( ac );
		return ensureAvailability( uriID, prio, l );
	}


	// worker methods

	final protected Iterator<Triple> find ( int s, int p, int o )
	{
		return dataset.find( s, p, o );
	}

	final protected Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o )
	{
		return dataset.findWithProvenance( s, p, o );
	}

	abstract protected boolean ensureAvailability ( int uriID, Priority prio, DataRetrievedListener l );


	// helper methods

	final protected void ensureRegistered ( AccessContext ac ) throws UnregisteredAccessContextException
	{
		if ( ! acRegistry.isRegistered(ac) ) {
			throw new UnregisteredAccessContextException();
		}
	}

}
