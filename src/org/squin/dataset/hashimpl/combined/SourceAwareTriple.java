/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.TripleProvenance;


/**
 * This class extends the ID-based representation of an RDF triple with
 * provenance information.
 * Every object of this class is owned by a {@link SourceAwareTripleIndex};
 * i.e. the same SourceAwareTriple must not be contained in multiple
 * {@link SourceAwareTripleIndex}es.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class SourceAwareTriple extends TraceableTriple
{
	// members

	/** The set of RDF graphs that contained the represented RDF triple. */
	final protected Set<IndexedRDFGraph> sources = new HashSet<IndexedRDFGraph> ();

	/** This flag is set when the triple is about to be removed from its index. */
	protected boolean toBeRemoved = false;


	// initialization

// 	public SourceAwareTriple ( long s, long p, long o )
	public SourceAwareTriple ( int s, int p, int o )
	{
		super( s, p, o );
	}

	public SourceAwareTriple ( Triple t )
	{
		super( t.s, t.p, t.o );
	}


	// implementation of the TraceableTriple abstract methods

	public TripleProvenance getProvenance ()
	{
		Set<RDFGraphProvenance> provenanceOfContainingRDFGraphs = new HashSet<RDFGraphProvenance> ();
		synchronized ( this ) {
			for ( IndexedRDFGraph g : sources ) {
				provenanceOfContainingRDFGraphs.add( g.prv );
			}
		}

		return new TripleProvenanceImpl( provenanceOfContainingRDFGraphs );
	}


	// operations

	/**
	 * Returns true if this triple has any known source.
	 */
	synchronized final public boolean hasAnySource ()
	{
		return ! sources.isEmpty();
	}

	/**
	 * Returns true if this triple has at least one valid source.
	 */
	synchronized final public boolean hasValidSource ()
	{
		for ( IndexedRDFGraph src : sources ) {
			if ( src.isValid() ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this triple has at a valid source with the given URL.
	 */
	synchronized final public boolean hasValidSource ( URL url )
	{
		for ( IndexedRDFGraph src : sources ) {
			if ( src.isValid() && src.prv.getAccessedResourceURL().equals(url) ) {
				return true;
			}
		}
		return false;
	}

	synchronized public void addSource ( IndexedRDFGraph src )
	{
		sources.add( src );
	}

	synchronized public boolean removeSource ( IndexedRDFGraph src )
	{
		return sources.remove( src );
	}

	synchronized public boolean removeSources ( Set<IndexedRDFGraph> srcs )
	{
		boolean result = false;
		for ( IndexedRDFGraph src : srcs ) {
			result = result | removeSource( src );
		}
		return result;
	}

	synchronized public boolean isSetToBeRemoved ()
	{
		return toBeRemoved;
	}

	synchronized public void setToBeRemoved ()
	{
		if ( hasAnySource() ) {
			throw new IllegalStateException( "The triple cannot be set to be removed because it still has sources." );
		}
		toBeRemoved = true;
	}

}
