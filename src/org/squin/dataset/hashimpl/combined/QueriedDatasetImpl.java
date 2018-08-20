/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.squin.common.Statistics;
import org.squin.dataset.QueriedDataset;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;


/**
 * An implementation of the {@link QueriedDataset} that stores all descriptor
 * objects in a {@link CombinedIndex}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueriedDatasetImpl implements QueriedDataset
{
	static final public boolean FREE_RESOURCES_IMMEDIATELY = false;

	// members

	final protected CombinedIndex index;


	// initialization

	public QueriedDatasetImpl ( CombinedIndex index )
	{
		assert index != null;
		this.index = index;
	}

	public QueriedDatasetImpl ()
	{
		this( new QuadIndex() );
	}

	public QueriedDatasetImpl ( int keyMaskSizeForHashTabs )
	{
		this( new QuadIndex(keyMaskSizeForHashTabs) );
	}


	// implementation of the QueriedDataset interface

	public void putRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv )
	{
		index.indexRDFGraph( itTriples, prv, FREE_RESOURCES_IMMEDIATELY );
	}

	public void removeRDFGraph ( URL src )
	{
		index.removeRDFGraph ( src, FREE_RESOURCES_IMMEDIATELY );
	}

// 	public Iterator<Triple> find ( long s, long p, long o )
	public Iterator<Triple> find ( int s, int p, int o )
	{
		return index.find( s, p, o );
	}

	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o )
	{
		return index.findWithProvenance( s, p, o );
	}

	public boolean containsRDFGraphWithSourceURL ( URL url )
	{
		return index.isIndexedSourceURL( url );
	}

	public Set<URL> getRDFGraphsSourceURLs ()
	{
		return index.getIndexedSourceURLs();
	}

	public int countRDFGraphs ()
	{
		return index.countIndexedRDFGraphs();
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		return index.getStatistics();
	}
}
