/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.cache;

import java.io.InputStream;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;


/**
 * A cache for results of SPARQL queries.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface QueryResultCache
{
	/**
	 * Returns true if the cache contains results for the given query serialized
	 * in the specified format.
	 */
	public boolean hasResults ( String query, String responseContentType );

	/**
	 * Returns an import stream that contains the cached results for the given
	 * query serialized in the specified format.
	 */
	public InputStream getResults ( String query, String responseContentType );

	/**
	 * Caches the given results for the given query.
	 *
	 * @return true if the results have been cached successfully
	 */
	public boolean cacheResults ( String query, ResultSetMem results );
}
