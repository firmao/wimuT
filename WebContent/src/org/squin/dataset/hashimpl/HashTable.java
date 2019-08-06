/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.squin.common.Statistics;
import org.squin.common.StatisticsProvider;
import org.squin.common.impl.StatisticsImpl;


/**
 * Thus hash table indexes objects by identifiers.
 * The main instantiation of this template is an index of identifier-based
 * RDF triple representations ({@link org.squin.dataset.Triple} objects).
 * This class can be used to create S, P, and O indexes of the RDF triples.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class HashTable<T> implements StatisticsProvider
{
	// members

	/** Bitmask that selects the bits of identifiers used for hash keys. */
	static final public int DEFAULT_KEYMASKSIZE = 4;
// 	final public long indexKeyMask;
	final public int indexKeyMask;

	/** the actual hash table */
	final private HashTableBucket<T> [] index;

	// for benchmarking only
// 	private int accessCounter = 0;


	// initialization

	public HashTable ()
	{
		this( DEFAULT_KEYMASKSIZE );
	}

	@SuppressWarnings("unchecked")
	public HashTable ( int keyMaskSize )
	{
		indexKeyMask = ( 1 << keyMaskSize ) - 1;
		assert indexKeyMask <= Integer.MAX_VALUE;
// 		index = new HashTableBucket [ (int) indexKeyMask + 1 ];
		index = new HashTableBucket [ indexKeyMask + 1 ];
	}


	// accessors

	/**
	 * Stores the given object using the given key.
	 */
// 	public void put ( long key, T obj )
	public void put ( int key, T obj )
	{
		int indexKey = getIndexKey( key );
		synchronized ( index ) {
			if ( index[indexKey] == null ) {
				index[indexKey] = new HashTableBucket<T> ();
			}
		}

		synchronized ( index[indexKey] ) {
			index[indexKey].add( obj );
		}
	}

	/**
	 * Removes the given object with the given key.
	 *
	 * @return true if this hash table contained the given object
	 */
// 	public boolean remove ( long key, T obj )
	public boolean remove ( int key, T obj )
	{
// 		accessCounter++;
		int indexKey = getIndexKey( key );
		HashTableBucket<T> bucket;
		synchronized ( index ) {
			bucket = index[indexKey];
		}

		if ( bucket != null ) {
			synchronized ( bucket ) {
				return bucket.remove( obj );
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Clears the hash table completely.
	 */
	public void clear ()
	{
		synchronized ( index ) {
			for ( int i = index.length - 1; i >= 0; --i ) {
				if ( index[i] != null ) {
					synchronized ( index[i] ) {
						index[i].clear();
					}
				}

				index[i] = null;
			}
		}
	}

	/**
	 * Returns a COPY of the bucket that contains objects indexed with the
	 * given key (or null if there is no such bucket yet).
	 * Attention: the bucket may contain more objects as the object with the
	 * given key.
	 */
// 	public HashTableBucket<T> getBucketCopy ( long key )
	public HashTableBucket<T> getBucketCopy ( int key )
	{
// 		accessCounter++;
		int indexKey = getIndexKey( key );
		HashTableBucket<T> bucket;
		synchronized ( index ) {
			bucket = index[indexKey];
		}

		if ( bucket != null ) {
			synchronized ( bucket ) {
// return bucket;
				return new HashTableBucket<T> ( bucket );
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Returns an iterator over COPIES of the buckets in this hash table.
	 */
	public Iterator<HashTableBucket<T>> getBucketCopies ()
	{
// 		accessCounter++;
		return new AllBucketsIterator ();
	}

	/**
	 * Returns the number of entries in this index.
	 */
	public int size ()
	{
		int result = 0;
		int buckets = index.length;
		for ( int i = 0; i < buckets; ++i )
		{
			if ( index[i] != null ) {
				synchronized ( index[i] ) {
					result += index[i].size();
				}
			}
		}
		return result;
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		int buckets = index.length;
		int sizes[] = new int[buckets];
		for ( int i = 0; i < buckets; ++i )
		{
			if ( index[i] != null ) {
				synchronized ( index[i] ) {
					sizes[i] = index[i].size();
				}
			}
			else {
				sizes[i] = 0;
			}
		}

		int triples = 0;
		int largestBucketSize = 0;
		int smallestBucketSize = Integer.MAX_VALUE;
		for ( int i = 0; i < buckets; ++i ) {
			triples += sizes[i];
			if ( largestBucketSize < sizes[i] ) {
				largestBucketSize = sizes[i];
			}
			if ( smallestBucketSize > sizes[i] ) {
				smallestBucketSize = sizes[i];
			}
		}

		double averageBucketSize = triples / buckets;

		double tmp = 0;
		for ( int i = 0; i < buckets; ++i ) {
			tmp += Math.pow( sizes[i] - averageBucketSize, 2 );
		}
		double stdDevBucketSize = Math.sqrt( tmp / buckets );

		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "buckets", buckets ); // number of buckets in the hash table
		statAttrs.add( "triples", triples ); // overall number of RDF triples in the hash table
		statAttrs.add( "largestBucketSize", largestBucketSize ); // number of RDF triples in the bucket with the most triples
		statAttrs.add( "smallestBucketSize", smallestBucketSize ); // number of RDF triples in the bucket with the fewest triples
		statAttrs.add( "averageBucketSize", averageBucketSize ); // average number of RDF triples in the buckets
		statAttrs.add( "stdDevBucketSize", stdDevBucketSize ); // standard deviation of the number of RDF triples in the buckets
		return new StatisticsImpl( statAttrs );
	}


	// helpers

	/**
	 * Calculates the hash key from the given key.
	 */
// 	final protected int getIndexKey ( long key )
	final protected int getIndexKey ( int key )
	{
// 		return (int) ( key & indexKeyMask );
		return ( key & indexKeyMask );
	}


	/**
	 * This iterator provides all copies of all buckets in this hash table.
	 */
	protected class AllBucketsIterator implements Iterator<HashTableBucket<T>>
	{
		private int curBucketIdx = -1;
		private HashTableBucket<T> curBucket = null;

		public boolean hasNext ()
		{
			if ( curBucket != null ) {
				return true;
			}

			synchronized ( index ) {
				while ( curBucket == null && curBucketIdx < indexKeyMask ) {
					curBucketIdx++;
					curBucket = index[curBucketIdx];
				}
			}

			return ( curBucket != null );
		}

		public HashTableBucket<T> next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			HashTableBucket<T> result;
			synchronized ( curBucket ) {
// return curBucket;
				result = new HashTableBucket<T> ( curBucket );
			}
			curBucket = null;
			return result;
		}

		public void remove () { throw new UnsupportedOperationException(); }
	}

}
