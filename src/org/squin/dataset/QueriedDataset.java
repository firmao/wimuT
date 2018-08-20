/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.squin.common.Statistics;
import org.squin.common.StatisticsProvider;


/**
 * This interface represent a queried dataset as being used for Link traversal
 * based query execution.
 * A queried dataset contains multiple descriptor objects (i.e. RDF graphs) that
 * have been retrieved from the Web. A representation of such a queried dataset
 * must support four main operations:
 * 1.) find RDF triples that match a triple pattern in the union of all descriptor objects
 * 2.) add a descriptor object to the queried dataset
 * 3.) replace a descriptor object in a the queried dataset with a more recent version retrieved from the same URL
 * 4.) remove a descriptor object from the queried dataset
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface QueriedDataset extends StatisticsProvider
{
	/**
	 * This methods puts the RDF graph -which contains the given triples- to
	 * this queried dataset.
	 * The RDF graph was retrieved from the URL provided by the method
	 * {@link RDFGraphProvenance#getAccessedResourceURL} of the given
	 * {@link RDFGraphProvenance} object. If the queried dataset already
	 * contains an RDF graph retrieved from the same URL then this old
	 * version should be replaced by the given, more recent version.
	 */
	public void putRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv );

	/**
	 * This methods removes the RDF graph with the given source URL from this
	 * queried dataset.
	 */
	public void removeRDFGraph ( URL src );

	/**
	 * This method returns all triples that match the given pattern in all RDF
	 * graphs in this queried dataset.
	 * Use {@link Triple#UNKNOWN_IDENTIFIER} as wildcard.
	 */
// 	public Iterator<Triple> find ( long s, long p, long o );
	public Iterator<Triple> find ( int s, int p, int o );

	/**
	 * This method returns all triples, combined with their provenance, that
	 * match the given pattern in all RDF graphs in this queried dataset.
	 * Use {@link Triple#UNKNOWN_IDENTIFIER} as wildcard.
	 */
	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o );

	/**
	 * This method returns true if this queried dataset contains an RDF graph
	 * with the given source URL.
	 */
	public boolean containsRDFGraphWithSourceURL ( URL url );

	/**
	 * This method returns the source URLs of all RDF graphs in this queried
	 * dataset.
	 */
	public Set<URL> getRDFGraphsSourceURLs ();

	/**
	 * Returns the number of RDF graphs in this queried dataset.
	 */
	public int countRDFGraphs ();
}
