/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.individual;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.Index;
import org.squin.dataset.hashimpl.common.IndexImpl;

import org.squin.common.Statistics;
import org.squin.common.impl.StatisticsImpl;


/**
 * An implementation of the {@link QueriedDataset} that stores all descriptor
 * objects in individual {@link Index}es.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueriedDatasetImpl implements QueriedDataset
{
	static final public int DEFAULT_KEY_MASK_SIZE_FOR_HASH_TABLES = 4;

	// members

	final protected int keyMaskSizeForHashTabs;
	final protected Map<URL,Index> indexes = new HashMap<URL,Index> ();
	final protected Map<URL,RDFGraphProvenance> provenance; // access this object only when 'indexes' is locked


	// initialization

	public QueriedDatasetImpl ()
	{
		this( DEFAULT_KEY_MASK_SIZE_FOR_HASH_TABLES, true );
	}

	public QueriedDatasetImpl ( int keyMaskSizeForHashTabs )
	{
		this( keyMaskSizeForHashTabs, true );
	}

	public QueriedDatasetImpl ( boolean recordProvenance )
	{
		this( DEFAULT_KEY_MASK_SIZE_FOR_HASH_TABLES, recordProvenance );
	}

	public QueriedDatasetImpl ( int keyMaskSizeForHashTabs, boolean recordProvenance )
	{
		this.keyMaskSizeForHashTabs = keyMaskSizeForHashTabs;
		provenance = recordProvenance ? new HashMap<URL,RDFGraphProvenance> () : null;
	}


	// implementation of the QueriedDataset interface

	public void putRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv )
	{
		Index idx = new IndexImpl( keyMaskSizeForHashTabs );
		idx.indexTriples( itTriples );
		Index oldIdx = null;
		synchronized( indexes ) {
			oldIdx = indexes.put( prv.getAccessedResourceURL(), idx );
			if ( provenance != null ) {
				provenance.put( prv.getAccessedResourceURL(), prv );
			}
		}
	}

	public void removeRDFGraph ( URL src )
	{
		Index oldIdx = null;
		synchronized( indexes ) {
			oldIdx = indexes.remove( src );
		}
	}

// 	public Iterator<Triple> find ( long s, long p, long o )
	public Iterator<Triple> find ( int s, int p, int o )
	{
// 		List<Iterator<Triple>> iterators = new ArrayList<Iterator<Triple>> ();
// 		synchronized( indexes ) {
// 			for ( Index idx : indexes.values() ) {
// 				iterators.add( idx.find(s,p,o) );
// 			}
// 		}
// 		return new ConcatenatingIterator<Triple> ( iterators.iterator() );

		List<Index> indexesCopy;
		synchronized ( indexes ) {
			indexesCopy = new ArrayList<Index> ( indexes.values() );
		}
		return new UnionFindIteratorIterator( indexesCopy, s, p, o );
	}

	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o )
	{
		throw new UnsupportedOperationException( "TODO: org.squin.dataset.hashimpl.individual.QueriedDatasetImpl.findWithProvenance" );
	}

	public boolean containsRDFGraphWithSourceURL ( URL url )
	{
		synchronized ( indexes ) {
			return indexes.containsKey( url );
		}
	}

	public Set<URL> getRDFGraphsSourceURLs ()
	{
		synchronized ( indexes ) {
			return new HashSet<URL> ( indexes.keySet() );
		}
	}

	public int countRDFGraphs ()
	{
		synchronized ( indexes ) {
			return indexes.size();
		}
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		int graphs;
		int triples = 0;

		Set<Index> tmp;
		synchronized( indexes ) {
			graphs = indexes.size();
			tmp = new HashSet<Index> ( indexes.values() );
		}

		for ( Index idx : tmp ) {
			triples += idx.getStatistics().getAttributeValueAsInteger( "triples" );
		}

		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "graphs", graphs );
		statAttrs.add( "triples", triples );
		return new StatisticsImpl( statAttrs );
	}



	class UnionFindIteratorIterator implements Iterator<Triple>
	{
		/**
		 * This index stores the matching triples that have already been returned
		 * in order to skip later occurences of them.
		 */
		final protected Set<Triple> seen = new HashSet<Triple> ();

		final protected Iterator<Index> indexIterator;
		final protected int s, p, o;


		protected Iterator<Triple> itCurrentMatch;
		protected Triple currentMatch;

		public UnionFindIteratorIterator ( List<Index> indexes, int s, int p, int o )
		{
			indexIterator = indexes.iterator();
			this.s = s;
			this.p = p;
			this.o = o;
		}

		public boolean hasNext ()
		{
			if ( currentMatch != null ) {
				return true;
			}

			while ( currentMatch == null || seen.contains(currentMatch) )
			{
				while ( itCurrentMatch == null || ! itCurrentMatch.hasNext() )
				{
					if ( ! indexIterator.hasNext() )
					{
						currentMatch = null;
						return false;
					}

					itCurrentMatch = indexIterator.next().find( s, p, o );
				}

				currentMatch = itCurrentMatch.next();
			}

			return true;
		}

		public Triple next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			seen.add( currentMatch );

			Triple result = currentMatch;
			currentMatch = null;
			return result;
		}

		public void remove () { throw new UnsupportedOperationException(); }
	}


}
