/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl;

import java.util.Iterator;

import org.squin.common.Statistics;
import org.squin.dataset.Triple;


/**
 * This interface represents an index structure for RDF data.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface Index
{
	/**
	 * Adds the given triples to the index.
	 */
	public void indexTriples ( Iterator<Triple> itTriples );

	/**
	 * Returns triples from the index that match the triple pattern specified
	 * by the three parameters.
	 */
	public Iterator<Triple> find ( int s, int p, int o );

	/**
	 * Returns {@link Statistics} about the index.
	 */
	public Statistics getStatistics ();
}
