/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.squin.dataset.Triple;

import org.squin.common.Statistics;
import org.squin.common.impl.StatisticsImpl;
import org.squin.dataset.hashimpl.HashTable;
import org.squin.dataset.hashimpl.HashTableBucket;
import org.squin.dataset.hashimpl.Index;


/**
 * This class represents a generic implementation of the {@link Index}
 * interface.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class IndexImpl implements Index
{
	// members

	final protected HashTable<Triple> htabS;
	final protected HashTable<Triple> htabP;
	final protected HashTable<Triple> htabO;
	final protected HashTable<Triple> htabSP;
	final protected HashTable<Triple> htabSO;
	final protected HashTable<Triple> htabPO;


	// initialization

	public IndexImpl ( int keyMaskSizeForHashTabs )
	{
		htabS = new HashTable<Triple> ( keyMaskSizeForHashTabs );
		htabP = new HashTable<Triple> ( keyMaskSizeForHashTabs );
		htabO = new HashTable<Triple> ( keyMaskSizeForHashTabs );
		htabSP = new HashTable<Triple> ( keyMaskSizeForHashTabs );
		htabSO = new HashTable<Triple> ( keyMaskSizeForHashTabs );
		htabPO = new HashTable<Triple> ( keyMaskSizeForHashTabs );
	}


	// implementation of the Index interface

	public void indexTriples ( Iterator<Triple> itTriples )
	{
		while ( itTriples.hasNext() ) {
			indexTriple( itTriples.next() );
		}
	}

	public Iterator<Triple> find ( int s, int p, int o )
	{
		if ( s == Triple.UNKNOWN_IDENTIFIER )          // PO, P, O, or none
		{
			if ( p == Triple.UNKNOWN_IDENTIFIER )       // O or none
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // none !
					return new AllTriplesIterator( htabS.getBucketCopies() );
				} else {                                         // O !
					return new MatchingTripleIteratorO( htabO.getBucketCopy(o), o );
				}
			}
			else                                                // PO or P
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // P !
					return new MatchingTripleIteratorP( htabP.getBucketCopy(p), p );
				} else {                                         // PO !
					return new MatchingTripleIteratorPO( htabPO.getBucketCopy(p*o), p, o );
				}
			}
		}
		else                                                   // SPO, SP, SO, or S
		{
			if ( p == Triple.UNKNOWN_IDENTIFIER )       // SO or S
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // S !
					return new MatchingTripleIteratorS( htabS.getBucketCopy(s), s );
				} else {                                         // SO !
					return new MatchingTripleIteratorSO( htabSO.getBucketCopy(s*o), s, o );
				}
			}
			else                                                // SPO or SP
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // SP !
					return new MatchingTripleIteratorSP( htabSP.getBucketCopy(s*p), s, p );
				} else {                                         // SPO !
					return new MatchingTripleIteratorSPO( htabSO.getBucketCopy(s*o), s, p, o );
				}
			}
		}
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
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

	protected void indexTriple ( Triple t )
	{
		if ( find(t.s,t.p,t.o).hasNext() ) {
			return;
		}

		putIntoHashTables( t );
	}

	protected void putIntoHashTables ( Triple t )
	{
		htabS.put( getHashTableKeyS(t), t );
		htabP.put( getHashTableKeyP(t), t );
		htabO.put( getHashTableKeyO(t), t );
		htabSP.put( getHashTableKeySP(t), t );
		htabSO.put( getHashTableKeySO(t), t );
		htabPO.put( getHashTableKeyPO(t), t );
	}

// 	static public long getHashTableKeyS ( Triple t )
	static public int getHashTableKeyS ( Triple t )
	{
		return t.s;
	}

// 	static public long getHashTableKeyP ( Triple t )
	static public int getHashTableKeyP ( Triple t )
	{
		return t.p;
	}

// 	static public long getHashTableKeyO ( Triple t )
	static public int getHashTableKeyO ( Triple t )
	{
		return t.o;
	}

// 	static public long getHashTableKeySP ( Triple t )
	static public int getHashTableKeySP ( Triple t )
	{
		return t.s * t.p;
	}

// 	static public long getHashTableKeySO ( Triple t )
	static public int getHashTableKeySO ( Triple t )
	{
		return t.s * t.o;
	}

// 	static public long getHashTableKeyPO ( Triple t )
	static public int getHashTableKeyPO ( Triple t )
	{
		return t.p * t.o;
	}

	final protected void remove ( Triple t )
	{
		htabS.remove( getHashTableKeyS(t), t );
		htabP.remove( getHashTableKeyP(t), t );
		htabO.remove( getHashTableKeyO(t), t );
		htabSP.remove( getHashTableKeySP(t), t );
		htabSO.remove( getHashTableKeySO(t), t );
		htabPO.remove( getHashTableKeyPO(t), t );
	}


	// iterators

	// Base class for all iterators over one of the indexes.
	static abstract class MatchingTripleIteratorBase implements Iterator<Triple>
	{
		final protected Iterator<Triple> bucketIterator;
		private Triple nextTriple;

		public MatchingTripleIteratorBase ( HashTableBucket<Triple> bucket )
		{
			bucketIterator = ( bucket != null ) ? bucket.iterator() : null;
		}

		final public boolean hasNext ()
		{
			if ( bucketIterator == null ) {
				return false;
			}

			if ( nextTriple != null ) {
				return true;
			}

			Triple t;
			while ( bucketIterator.hasNext() )
			{
				t = bucketIterator.next();
				if ( matches(t) )
				{
					nextTriple = t;
					break;
				}
			}

			return ( nextTriple != null );
		}

		final public Triple next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			Triple t = nextTriple;
			nextTriple = null;
			return t;
		}

		final public void remove () { throw new UnsupportedOperationException(); }

		abstract protected boolean matches ( Triple t );
	}

	static public class MatchingTripleIteratorS extends MatchingTripleIteratorBase
	{
// 		final protected long s;
		final protected int s;
// 		public MatchingTripleIteratorS ( HashTableBucket<Triple> bucket, long s ) { super(bucket); this.s = s; }
		public MatchingTripleIteratorS ( HashTableBucket<Triple> bucket, int s ) { super(bucket); this.s = s; }
		protected boolean matches ( Triple t ) { return t.s == s; }
	}

	static public class MatchingTripleIteratorP extends MatchingTripleIteratorBase
	{
// 		final protected long p;
		final protected int p;
// 		public MatchingTripleIteratorP ( HashTableBucket<Triple> bucket, long p ) { super(bucket); this.p = p; }
		public MatchingTripleIteratorP ( HashTableBucket<Triple> bucket, int p ) { super(bucket); this.p = p; }
		protected boolean matches ( Triple t ) { return t.p == p; }
	}

	static public class MatchingTripleIteratorO extends MatchingTripleIteratorBase
	{
// 		final protected long o;
		final protected int o;
// 		public MatchingTripleIteratorO ( HashTableBucket<Triple> bucket, long o ) { super(bucket); this.o = o; }
		public MatchingTripleIteratorO ( HashTableBucket<Triple> bucket, int o ) { super(bucket); this.o = o; }
		protected boolean matches ( Triple t ) { return t.o == o; }
	}

	static public class MatchingTripleIteratorSP extends MatchingTripleIteratorBase
	{
// 		final protected long s, p;
		final protected int s, p;
// 		public MatchingTripleIteratorSP ( HashTableBucket<Triple> bucket, long s, long p ) { super(bucket); this.s = s; this.p = p; }
		public MatchingTripleIteratorSP ( HashTableBucket<Triple> bucket, int s, int p ) { super(bucket); this.s = s; this.p = p; }
		protected boolean matches ( Triple t ) { return t.s == s && t.p == p; }
	}

	static public class MatchingTripleIteratorSO extends MatchingTripleIteratorBase
	{
// 		final protected long s, o;
		final protected int s, o;
// 		public MatchingTripleIteratorSO ( HashTableBucket<Triple> bucket, long s, long o ) { super(bucket); this.s = s; this.o = o; }
		public MatchingTripleIteratorSO ( HashTableBucket<Triple> bucket, int s, int o ) { super(bucket); this.s = s; this.o = o; }
		protected boolean matches ( Triple t ) { return t.s == s && t.o == o; }
	}

	static public class MatchingTripleIteratorPO extends MatchingTripleIteratorBase
	{
// 		final protected long p, o;
		final protected int p, o;
// 		public MatchingTripleIteratorPO ( HashTableBucket<Triple> bucket, long p, long o ) { super(bucket); this.p = p; this.o = o; }
		public MatchingTripleIteratorPO ( HashTableBucket<Triple> bucket, int p, int o ) { super(bucket); this.p = p; this.o = o; }
		protected boolean matches ( Triple t ) { return t.p == p && t.o == o; }
	}

	static public class MatchingTripleIteratorSPO extends MatchingTripleIteratorBase
	{
// 		final protected long s, p, o;
		final protected int s, p, o;
// 		public MatchingTripleIteratorSPO ( HashTableBucket<Triple> bucket, long s, long p, long o ) { super(bucket); this.s = s; this.p = p; this.o = o; }
		public MatchingTripleIteratorSPO ( HashTableBucket<Triple> bucket, int s, int p, int o ) { super(bucket); this.s = s; this.p = p; this.o = o; }
		protected boolean matches ( Triple t ) { return t.s == s && t.p == p && t.o == o; }
	}

	static public class MatchingTripleIteratorNone extends MatchingTripleIteratorBase
	{
		public MatchingTripleIteratorNone ( HashTableBucket<Triple> bucket ) { super(bucket); }
		protected boolean matches ( Triple t ) { return true; }
	}


	// an iterator that returns all triples in all buckets
	static public class AllTriplesIterator implements Iterator<Triple>
	{
		final protected Iterator<HashTableBucket<Triple>> allBucketsIterator;
		private Iterator<Triple> curBucketIterator;

		public AllTriplesIterator ( Iterator<HashTableBucket<Triple>> allBucketsIterator )
		{
			this.allBucketsIterator = allBucketsIterator;
		}

		final public boolean hasNext ()
		{
			while ( curBucketIterator == null || ! curBucketIterator.hasNext() ) {
				if ( ! allBucketsIterator.hasNext() ) {
					return false;
				}

				curBucketIterator = allBucketsIterator.next().iterator();
			}

			return true;
		}

		final public Triple next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			return curBucketIterator.next();
		}

		final public void remove () { throw new UnsupportedOperationException(); }
	}

}
