/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache.impl;


/**
 * Thrown to indicate that an unregistered
 * {@link org.squin.ldcache.AccessContext}
 * has been used.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class UnregisteredAccessContextException extends IllegalArgumentException
{
	public UnregisteredAccessContextException () {};
	public UnregisteredAccessContextException ( String msg ) { super(msg); }
	public UnregisteredAccessContextException ( String msg, Throwable cause ) { super(msg,cause); }
	public UnregisteredAccessContextException ( Throwable cause ) { super(cause); }
}
