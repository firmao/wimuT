/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref;


/**
 * This class represents a URI which has been
 * discovered by dereferencing another URI.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class DiscoveredURI
{
	/**
	 * The identifier of the discovered URI.
	 */
	final public int uriID;

	/**
	 * The way in which this URI was discovered.
	 */
	final public TypeOfURIDiscovery discoveryType;

	/**
	 * The identifier of the source URI, that is, the URI
	 * which has been dereferenced to discover this URI.
	 */
	final public int srcUriID;


	public DiscoveredURI ( int uriID, TypeOfURIDiscovery discoveryType, int srcUriID )
	{
		assert discoveryType != null;

		this.uriID = uriID;
		this.discoveryType = discoveryType;
		this.srcUriID = srcUriID;
	}
}
