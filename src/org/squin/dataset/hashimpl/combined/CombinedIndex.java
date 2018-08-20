/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.Index;


/**
 * This interface represents an index structure that indexes the data from
 * multiple RDF graphs.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface CombinedIndex extends Index
{
	/**
	 * Returns all triples, combined with their provenance, from the index
	 * that match the triple pattern specified by the three parameters.
	 */
	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o );

	/**
	 * Adds the given set of triples, which make up an RDF graph with
	 * the given provenance, to this index.
	 * The RDF graph was retrieved from the URL provided by the method
	 * {@link RDFGraphProvenance#getAccessedResourceURL} of the given
	 * {@link RDFGraphProvenance} object. If the index contains another
	 * set of triples from the same URL then these old triples must be
	 * replaced by the given triples. The implementation of this method
	 * must provide the transactional property isolation.
	 *
	 * @param itTriples the triples that make up the RDF graph to be added
	 *                  to this indexed
	 * @param prv the provenance of the given set of triples
	 * @param freeResourcesImmediately if true, replaced triples have to be
	 *            removed completely and all resources have to be freed
	 *            immediately; otherwise removed triples may be removed lazily
	 *            (e.g. they could only be marked as inactive and will be removed
	 *            completely by a clean-up thread later; however, in the mean
	 *            time they must not be taken into account by the {@link #find}
	 *            method anymore.
	 */
	public void indexRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv, boolean freeResourcesImmediately );

	/**
	 * Removes the RDF graph that was retrieved from the given URL from this
	 * index.
	 *
	 * @param src the source URL with which the RDF graph has been added to
	 *            this index (more precisely, the graph has been added with
	 *            a {@link RDFGraphProvenance} object from which the source
	 *            URL can be obtained by calling the method
	 *            {@link RDFGraphProvenance#getAccessedResourceURL})
	 * @param freeResourcesImmediately if true the RDF graph has to be removed
	 *            completely and all resources have to be freed immediately;
	 *            otherwise the graph may be removed lazily (e.g. its triples
	 *            could only marked as inactive and will be removed by a clean-up
	 *            thread later; however, in the mean time they must not be taken
	 *            into account by the {@link #find} method anymore.
	 */
	public void removeRDFGraph ( URL src, boolean freeResourcesImmediately );

	/**
	 * Returns true if this index contains the triples from an RDF graph
	 * retrieved from the given URL.
	 */
	public boolean isIndexedSourceURL ( URL url );

	/**
	 * Returns the source URLs of all RDF graphs that are currently indexed and valid.
	 */
	public Set<URL> getIndexedSourceURLs ();

	/**
	 * Returns the number of RDF graphs that are currently indexed and valid.
	 */
	public int countIndexedRDFGraphs ();

}
