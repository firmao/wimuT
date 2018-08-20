/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache.impl;

import java.util.Collection;

import org.squin.ldcache.AccessContext;
import org.squin.ldcache.LinkedDataCache;


/**
 * Registry of {@link AccessContext}s for a specific {@link LinkedDataCache}.
 * This implementation is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class AccessContextRegistry
{
	final public LinkedDataCache cache;
	final protected Collection<Integer> registeredIDs = new java.util.HashSet<Integer> ();
	protected int nextFreeID = 0;

	public AccessContextRegistry ( LinkedDataCache cache )
	{
		assert cache != null;

		this.cache = cache;
	}

	synchronized public AccessContext getNewAccessContext ()
	{
		registeredIDs.add( Integer.valueOf(nextFreeID) );
		return new AccessContextImpl( nextFreeID++ );
	}

	synchronized public boolean isRegistered ( AccessContext ac )
	{
		if ( ac instanceof AccessContextImpl ) {
			int id = ( (AccessContextImpl) ac ).id;
			return registeredIDs.contains( Integer.valueOf(id) );
		} else {
			return false;
		}
	}

	synchronized public void unregister ( AccessContext ac ) throws UnregisteredAccessContextException
	{
		if ( ! ac.isRegistered() ) {
			throw new UnregisteredAccessContextException();
		}

		if ( ! (ac instanceof AccessContextImpl) ) {
			throw new UnregisteredAccessContextException();
		}

		if ( ! isRegistered(ac) ) {
			throw new UnregisteredAccessContextException();
		}

		int id = ( (AccessContextImpl) ac ).id;
		registeredIDs.remove( Integer.valueOf(id) );
		( (AccessContextImpl) ac ).setUnregistered();
	}


	class AccessContextImpl implements AccessContext
	{
		final public int id;
		protected boolean registered = true;

		public AccessContextImpl ( int id ) { this.id = id; }
		protected void finalize () throws Throwable { unregister(this); }

		public LinkedDataCache getCache () { return cache; }
		synchronized public boolean isRegistered () { return registered; }

		synchronized public void setUnregistered () { registered = false; }
	}

}
