/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.HashTableBucket;
import org.squin.util.ConvertingIterator;


/**
 * This class represents an index of RDF triples represented as {@link Quad}
 * objects.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QuadIndex extends CombinedIndexBase
{
	// initialization

	public QuadIndex ()
	{
		this( 12 );
	}

	public QuadIndex ( int keyMaskSizeForHashTabs )
	{
		super( keyMaskSizeForHashTabs );
	}


	// operations

	@Override
	public Iterator<Triple> find ( int s, int p, int o )
	{
		if ( s == Triple.UNKNOWN_IDENTIFIER )          // PO, P, O, or none
		{
			if ( p == Triple.UNKNOWN_IDENTIFIER )       // O or none
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // none !
					return new AllValidTriplesIterator( htabS.getBucketCopies() );
				} else {                                         // O !
					return new MatchingValidTripleIteratorO( htabO.getBucketCopy(o), o );
				}
			}
			else                                                // PO or P
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // P !
					return new MatchingValidTripleIteratorP( htabP.getBucketCopy(p), p );
				} else {                                         // PO !
					return new MatchingValidTripleIteratorPO( htabPO.getBucketCopy(p*o), p, o );
				}
			}
		}
		else                                                   // SPO, SP, SO, or S
		{
			if ( p == Triple.UNKNOWN_IDENTIFIER )       // SO or S
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // S !
					return new MatchingValidTripleIteratorS( htabS.getBucketCopy(s), s );
				} else {                                         // SO !
					return new MatchingValidTripleIteratorSO( htabSO.getBucketCopy(s*o), s, o );
				}
			}
			else                                                // SPO or SP
			{
				if ( o == Triple.UNKNOWN_IDENTIFIER ) {  // SP !
					return new MatchingValidTripleIteratorSP( htabSP.getBucketCopy(s*p), s, p );
				} else {                                         // SPO !
					return new MatchingValidTripleIteratorSPO( htabSO.getBucketCopy(s*o), s, p, o );
				}
			}
		}
	}

	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o )
	{
		return new MyConvertingIterator( find(s,p,o) );
	}


	// implementation of the abstract methods in CombinedIndexBase

	protected void indexTriple ( Triple t, IndexedRDFGraph src )
	{
		indexTriple( new Quad(t,src) );
	}

	/**
	 * Removes the given RDF graphs from this index and frees all resources
	 * allocated for these graphs in this index.
	 */
	protected void remove ( Set<IndexedRDFGraph> gs )
	{
		for ( IndexedRDFGraph g : gs ) {
			g.setBeingRemoved();
		}

		Iterator<Triple> itTriple = new AllTriplesIterator( htabS.getBucketCopies() );
		while ( itTriple.hasNext() ) {
			Quad q = (Quad) itTriple.next();
			for ( IndexedRDFGraph g : gs ) {
				if ( q.src.equals(g) ) {
					remove( q );
					break;
				}
			}
		}

		for ( IndexedRDFGraph g : gs ) {
			Set<IndexedRDFGraph> tmp;
			synchronized ( graphs ) {
				tmp = graphs.get( g.prv.getAccessedResourceURL() );
			}

			synchronized ( tmp ) {
				tmp.remove( g );
			}

			g.setRemoved();
		}
	}


	// helpers

	@Override
	protected void indexTriple ( Triple t )
	{
		putIntoHashTables( t );
	}


	// iterators

	static class MatchingValidTripleIteratorS extends MatchingTripleIteratorS
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorS ( HashTableBucket<Triple> bucket, int s ) { super(bucket,s); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorP extends MatchingTripleIteratorP
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorP ( HashTableBucket<Triple> bucket, int p ) { super(bucket,p); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorO extends MatchingTripleIteratorO
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorO ( HashTableBucket<Triple> bucket, int o ) { super(bucket,o); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorSP extends MatchingTripleIteratorSP
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorSP ( HashTableBucket<Triple> bucket, int s, int p ) { super(bucket,s,p); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorSO extends MatchingTripleIteratorSO
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorSO ( HashTableBucket<Triple> bucket, int s, int o ) { super(bucket,s,o); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorPO extends MatchingTripleIteratorPO
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorPO ( HashTableBucket<Triple> bucket, int p, int o ) { super(bucket,p,o); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorSPO extends MatchingTripleIteratorSPO
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorSPO ( HashTableBucket<Triple> bucket, int s, int p, int o ) { super(bucket,s,p,o); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}

	static class MatchingValidTripleIteratorNone extends MatchingTripleIteratorNone
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		public MatchingValidTripleIteratorNone ( HashTableBucket<Triple> bucket ) { super(bucket); }
		final protected boolean matches ( Triple t ) {
			if ( super.matches(t) && ((Quad) t).src.isValid() && ! seen.contains(t) ) {
				seen.add( t );
				return true;
			} else {
				return false;
			}
		}
	}


	// an iterator that returns all valid triples in all buckets
	static class AllValidTriplesIterator implements Iterator<Triple>
	{
		final protected Set<Triple> seen = new HashSet<Triple> ();
		final protected Iterator<HashTableBucket<Triple>> allBucketsIterator;
		private Iterator<Triple> curBucketIterator;
		private Triple nextTriple;

		public AllValidTriplesIterator ( Iterator<HashTableBucket<Triple>> allBucketsIterator )
		{
			this.allBucketsIterator = allBucketsIterator;
		}

		final public boolean hasNext ()
		{
			if ( nextTriple != null ) {
				return true;
			}

			while ( nextTriple == null || seen.contains(nextTriple) ) {
				while ( curBucketIterator == null || ! curBucketIterator.hasNext() ) {
					if ( ! allBucketsIterator.hasNext() ) {
						return false;
					}

					curBucketIterator = new MatchingValidTripleIteratorNone( allBucketsIterator.next() );
				}

				nextTriple = curBucketIterator.next();
			}

			return true;
		}

		final public Triple next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			seen.add( nextTriple );

			Triple t = nextTriple;
			nextTriple = null;
			return t;
		}

		final public void remove () { throw new UnsupportedOperationException(); }
	}

	class MyConvertingIterator extends ConvertingIterator<Triple,TraceableTriple>
	{
		public MyConvertingIterator ( Iterator<Triple> input ) { super( input ); }
		final protected TraceableTriple convert ( Triple t ) {
			return (Quad) t;
		}
	}

}
