/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.squin.common.Statistics;
import org.squin.common.impl.StatisticsImpl;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.common.IndexImpl;


/**
 * This class is an abstract base class for all implementations of the
 * {@link CombinedIndex} interface.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
abstract public class CombinedIndexBase extends IndexImpl
                                        implements CombinedIndex
{
	// members

	/**
	 * An index of the RDF graphs that are stored in this index, accessible based
	 * on the URL from which these graphs have been retrieved.
	 */
	final protected Map<URL,Set<IndexedRDFGraph>> graphs = new HashMap<URL,Set<IndexedRDFGraph>> ();
// TODO: investigate whether implementing 'graphs' as a  HashTable<HashMap<URL,Set<IndexedRDFGraph>>> 
//       with index keys (and, thus, a dictionary) for URLs may improve performance of query execution
//       during which multiple threads attempt to add RDF graphs


	// initialization

	public CombinedIndexBase ( int keyMaskSizeForHashTabs )
	{
		super( keyMaskSizeForHashTabs );
	}


	// implementation of the CombinedIndex interface

	public void indexRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv, boolean freeResourcesImmediately )
	{
		Set<IndexedRDFGraph> tmp;
		synchronized ( graphs ) {
			tmp = graphs.get( prv.getAccessedResourceURL() );
			if ( tmp == null ) {
				tmp = new HashSet<IndexedRDFGraph> ( 2 );
				         // Note, we use an initial capacity of 2 here to reduce
				         // the amount of memory consumed by this index. Usually,
				         // a certain RDF triple does not occur in so many RDF
				         // graphs from the Web.
				graphs.put( prv.getAccessedResourceURL(), tmp );
			}
		}

		IndexedRDFGraph g = new IndexedRDFGraph( prv );
		g.setBeingIndexed();

		while ( itTriples.hasNext() ) {
			indexTriple( itTriples.next(), g );
		}

		g.setIndexedValid();

		Set<IndexedRDFGraph> toBeRemoved = freeResourcesImmediately ? new HashSet<IndexedRDFGraph> () : null;
		synchronized ( tmp ) {
			for ( IndexedRDFGraph old : tmp ) {
				if ( old.isValid() ) {
					old.setIndexedInvalid();
				}
				if ( freeResourcesImmediately && old.isIndexed() ) {
					toBeRemoved.add( old );
				}
			}
			tmp.add( g );
		}

		if ( freeResourcesImmediately ) {
			remove( toBeRemoved );
		}
	}

	public void removeRDFGraph ( URL src, boolean freeResourcesImmediately )
	{
		Set<IndexedRDFGraph> tmp;
		synchronized ( graphs ) {
			tmp = graphs.get( src );
			if ( tmp == null ) {
				return;
			}
		}

		IndexedRDFGraph graph = null;
		synchronized ( tmp ) {
			for ( IndexedRDFGraph g : tmp ) {
				if ( g.isValid() ) {
					graph = g;
					graph.setIndexedInvalid();
					break;
				}
			}
		}

		if ( freeResourcesImmediately && graph != null ) {
			Set<IndexedRDFGraph> toBeRemoved = new HashSet<IndexedRDFGraph> ();
			toBeRemoved.add( graph );
			remove( toBeRemoved );
		}
	}

	/**
	 * Returns true if there is a valid RDF graph with the given source URL.
	 */
	public boolean isIndexedSourceURL ( URL url )
	{
		Set<IndexedRDFGraph> tmp;
		synchronized ( graphs ) {
			tmp = graphs.get( url );
			if ( tmp == null ) {
				return false;
			}
		}

		synchronized ( tmp ) {
			for ( IndexedRDFGraph g : tmp ) {
				if ( g.isValid() ) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns the source URLs of all RDF graphs that are currently indexed and valid.
	 */
	public Set<URL> getIndexedSourceURLs ()
	{
		Set<URL> result = new HashSet<URL> ();
		for ( URL key : graphs.keySet() ) {
			for ( IndexedRDFGraph g : graphs.get(key) ) {
				if ( g.isValid() ) {
					result.add( key );
					continue;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the number of RDF graphs that are currently indexed and valid.
	 */
	public int countIndexedRDFGraphs ()
	{
		Set<Set<IndexedRDFGraph>> values;
		synchronized ( graphs ) {
			values = new HashSet<Set<IndexedRDFGraph>> ( graphs.values() );
		}

		int i = 0;
		for ( Set<IndexedRDFGraph> tmp : values ) {
			for ( IndexedRDFGraph g : tmp ) {
				if ( g.isValid() ) {
					i++;
				}
			}
		}
		return i;
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		int allGraphs = 0;
		int validGraphs = 0;
		int invalidGraphs = 0;

		Set<Set<IndexedRDFGraph>> tmpGraphs;
		synchronized ( graphs ) {
			tmpGraphs = new HashSet<Set<IndexedRDFGraph>> ( graphs.values() );
		}

		for ( Set<IndexedRDFGraph> tmp : tmpGraphs ) {
			for ( IndexedRDFGraph g : tmp ) {
				synchronized ( g ) {
					allGraphs++;
					if ( g.isValid() ) {
						validGraphs++;
					}
					if ( g.getStatus() == IndexedRDFGraph.STATUS_INDEXED_INVALID ) {
						invalidGraphs++;
					}
				}
			}
		}

		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "graphs", allGraphs );
		statAttrs.add( "validGraphs", validGraphs );
		statAttrs.add( "invalidGraphs", invalidGraphs );
		Statistics tmp = htabS.getStatistics();
		statAttrs.add( "triples", tmp.getAttributeValue("triples") );
		statAttrs.add( "htabS", tmp );
		statAttrs.add( "htabP", htabP.getStatistics() );
		statAttrs.add( "htabO", htabO.getStatistics() );
		statAttrs.add( "htabSP", htabSP.getStatistics() );
		statAttrs.add( "htabSO", htabSO.getStatistics() );
		statAttrs.add( "htabPO", htabPO.getStatistics() );
		return new StatisticsImpl( statAttrs );
	}


	// helpers

	/**
	 * Adds the given triple, which is part of the given RDF graph, to this index.
	  */
	abstract protected void indexTriple ( Triple t, IndexedRDFGraph src );

	/**
	 * Removes the given RDF graphs from this index and frees all resources
	 * allocated for these graphs in this index.
	 */
	abstract protected void remove ( Set<IndexedRDFGraph> gs );

}
