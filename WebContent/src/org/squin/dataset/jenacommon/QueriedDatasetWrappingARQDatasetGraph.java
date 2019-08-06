/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.Quad;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.combined.SourceAwareTriple;
import org.squin.util.ConcatenatingIterator;
import org.squin.util.EmptyIterator;


/**
 * An implementation of the ARQ DatasetGraph interface that wraps a queried
 * dataset.
 * Notice, this implementation is very inefficient; it is primarily meant to
 * write the content of a {@link QueriedDataset} to a file.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueriedDatasetWrappingARQDatasetGraph extends DatasetGraphBase
                                                   implements DatasetGraph
{
	final static private Logger logger = LoggerFactory.getLogger( QueriedDatasetWrappingARQDatasetGraph.class );

	// members

	final public JenaIOBasedQueriedDataset queriedDataset;


	// initialization

	public QueriedDatasetWrappingARQDatasetGraph ( JenaIOBasedQueriedDataset queriedDataset )
	{
		this.queriedDataset = queriedDataset;
	}


	// implementation of the DatasetGraphBase abstract methods

	/**
	 * Returns null because {@link QueriedDataset}s do not have a default graph.
	 */
	@Override
	public Graph getDefaultGraph ()
	{
		return null;
	}

	@Override
	public Graph getGraph ( Node graphNode )
	{
		if ( ! graphNode.isURI() ) {
			return null;
		}

		try {
			URL url = new URL( graphNode.getURI() );
			return new ContainedGraph( url );
		}
		catch ( MalformedURLException e ) {
			logger.warn( "Since the current graph node URI ({}) cannot parsed as a URL it is impossible to return anything.", graphNode.getURI() );
			return null;
		}
	}

	public Iterator<Quad> find ( Node g, Node s, Node p, Node o )
	{
		if ( g == null || g.equals(Node.ANY) ) {
			Set<URL> srcURLs = queriedDataset.getRDFGraphsSourceURLs();
			Vector<Iterator<Quad>> tmp = new Vector<Iterator<Quad>> ( srcURLs.size() , 1 );
			for ( URL url : srcURLs ) {
				Iterator<Quad> it = find( Node.createURI(url.toString()), s, p, o );
				tmp.add( it );
			}
			return new ConcatenatingIterator<Quad> ( tmp );
		}
		else {
			Graph graph = getGraph( g );
			if ( graph == null ) {
				return new EmptyIterator<Quad> ();
			}
			return new TriplesToQuadsIterator( g, graph.find(s,p,o) );
		}
	}

	public Iterator<Quad> findNG ( Node g, Node s, Node p, Node o )
	{
		return find( g, s, p, o );
	}

	public Iterator<Node> listGraphNodes ()
	{
		return new GraphNodesIterator();
	}


	// additional operations

	public Graph getGraph ( URL accessedResource )
	{
		if ( ! queriedDataset.containsRDFGraphWithSourceURL(accessedResource) ) {
			return null;
		}

		return new ContainedGraph( accessedResource );
	}


	class ContainedGraph extends GraphBase
                                implements Graph
	{
		final public URL url;

		public ContainedGraph ( URL url ) { this.url = url; }

		@Override
		protected ExtendedIterator<com.hp.hpl.jena.graph.Triple> graphBaseFind ( TripleMatch m )
		{
			int s = ( m.getMatchSubject() == null ) ? Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchSubject() );
			int p = ( m.getMatchPredicate() == null ) ? Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchPredicate() );
			int o = ( m.getMatchObject() == null ) ? Triple.UNKNOWN_IDENTIFIER : queriedDataset.nodeDict.createId( m.getMatchObject() );

			Iterator<Triple> tmpIt = new EnsureGraphIterator( url, queriedDataset.find(s,p,o) );
			return new DecodingTriplesIterator( queriedDataset.nodeDict, tmpIt );
		}

		@Override
		public void performAdd ( com.hp.hpl.jena.graph.Triple t ) { throw new UnsupportedOperationException(); }

		@Override
		public void performDelete ( com.hp.hpl.jena.graph.Triple t ) { throw new UnsupportedOperationException(); }
	}

	static class EnsureGraphIterator implements Iterator<Triple>
	{
		final public URL url;
		final protected Iterator<Triple> inputIterator;
		protected Triple curTriple = null;

		public EnsureGraphIterator ( URL url, Iterator<Triple> inputIterator )
		{
			this.url = url;
			this.inputIterator = inputIterator;
		}
		
		@Override
		final public boolean hasNext ()
		{
// re-think this stuff!!
// The idea of SourceAwareTriple.hasValidSource(URL) was to return true
// if the triple was contained in an RDF graph that i) came from the given
// URL and ii) that is valid (status=indexedValid). The problem is that
// i) the triple does not know about URLs anymore and ii) not every QueriedDataset
// implementation uses SourceAwareTriple
// throw new UnsupportedOperationException( "TODO: org.squin.dataset.jenacommon.QueriedDatasetWrappingARQDatasetGraph.EnsureGraphIterator.hasNext()" );
			while ( curTriple == null ) {
				if ( ! inputIterator.hasNext() ) {
					return false;
				}
				curTriple = inputIterator.next();
				if ( curTriple instanceof org.squin.dataset.hashimpl.combined.Quad ) {
					if ( ! ((org.squin.dataset.hashimpl.combined.Quad) curTriple).src.isValid() ) {
						curTriple = null;
					}
				}
				else {
					if ( ! ((SourceAwareTriple) curTriple).hasValidSource(url) ) {
						curTriple = null;
					}
				}
			}
			return ( curTriple != null );
		}

		@Override
		final public Triple next ()
		{
			if ( ! hasNext() ) {
				throw new NoSuchElementException();
			}

			Triple tmp = curTriple;
			curTriple = null;
			return tmp;
		}

		@Override
		final public void remove () { inputIterator.remove(); }
	}


	class GraphNodesIterator implements Iterator<Node>
	{
		final protected Iterator<URL> inputIterator;
		public GraphNodesIterator () { inputIterator = queriedDataset.getRDFGraphsSourceURLs().iterator(); }
		public boolean hasNext () { return inputIterator.hasNext(); }
		public Node next () { return Node.createURI( inputIterator.next().toString() ); }
		public void remove () { throw new UnsupportedOperationException(); }
	}

	static class TriplesToQuadsIterator implements Iterator<Quad>
	{
		final protected Node graphNode;
		final protected Iterator<com.hp.hpl.jena.graph.Triple> inputIterator;

		public TriplesToQuadsIterator ( Node graphNode, Iterator<com.hp.hpl.jena.graph.Triple> inputIterator )
		{
			this.graphNode = graphNode;
			this.inputIterator = inputIterator;
		}

		public boolean hasNext () { return inputIterator.hasNext(); }
		public Quad next () { return new Quad( graphNode, inputIterator.next() ); }
		public void remove () { inputIterator.remove(); }
	}

}
