/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.impl;


/**
 * Exception thrown when a {@link DerefTask}
 * already started importing retrieved data.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class DerefTaskAlreadyImportingException extends IllegalStateException
{
	/**
	 * Constructs a {@link DerefTaskAlreadyImportingException} for a
	 * {@link DerefTask} which was responsible for the given URI.
	 *
	 * @param uriID identifier of the URI that the corresponding
	 *              {@link DerefTask} was responsible for
	 */
	public DerefTaskAlreadyImportingException( int uriID )
	{
		super( "The dereferencing task for URI " + uriID + " already started importing retrieved data." );
	}

}
