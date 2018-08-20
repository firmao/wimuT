/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.common;


/**
 * This is a common interface for all objects that provide statistics.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface StatisticsProvider
{
	/**
	 * Returns statistics about this object.
	 * The returned {@link Statistics} object should only be generated when
	 * requested (via this method) in order to ensure recent statistics and
	 * to avoid statistics information taking memory.
	 */
	public Statistics getStatistics ();
}
