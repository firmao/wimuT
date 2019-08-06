/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.RiotReader;

import com.hp.hpl.jena.sparql.core.DatasetGraph;

import org.squin.common.Statistics;
import org.squin.common.impl.StatisticsImpl;
import org.squin.dataset.QueriedDataset;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TraceableTriple;
import org.squin.dataset.Triple;
import org.squin.dataset.jenacommon.impl.NodeDictionaryImpl;


/**
 * An implementation of the {@link org.squin.dataset.QueriedDataset} that wraps
 * another implementation and enables the use of the Jena framework to load and
 * parse RDF data that has to be added to the wrapped dataset.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class JenaIOBasedQueriedDataset implements QueriedDataset
{
	final static private Logger logger = LoggerFactory.getLogger( JenaIOBasedQueriedDataset.class );

	// members

	final public NodeDictionary nodeDict;
	final protected QueriedDataset wrappedDataset;


	// initialization

	public JenaIOBasedQueriedDataset ( QueriedDataset wrappedDataset, NodeDictionary nodeDict )
	{
		this.wrappedDataset = wrappedDataset;
		this.nodeDict = nodeDict;
	}

	public JenaIOBasedQueriedDataset ( QueriedDataset wrappedDataset )
	{
		this( wrappedDataset, new NodeDictionaryImpl() );
	}

	public JenaIOBasedQueriedDataset ( String seedDataFilename,
	                                   QueriedDataset wrappedDataset )
	{
		this( wrappedDataset );
		loadSeedDataFile( seedDataFilename );
	}

	public JenaIOBasedQueriedDataset ( String seedDataFilename,
	                                   QueriedDataset wrappedDataset,
	                                   NodeDictionary nodeDict )
	{
		this( wrappedDataset, nodeDict );
		loadSeedDataFile( seedDataFilename );
	}

	public JenaIOBasedQueriedDataset ( DatasetGraph initialContent, QueriedDataset wrappedDataset )
	{
		this( wrappedDataset );
		putRDFGraphs( initialContent );
	}

	public JenaIOBasedQueriedDataset ( DatasetGraph initialContent, QueriedDataset wrappedDataset, NodeDictionary nodeDict )
	{
		this( wrappedDataset, nodeDict );
		putRDFGraphs( initialContent );
	}

	private void loadSeedDataFile ( String filename )
	{
		Lang guessedLanguage = Lang.guess( filename );
		if ( guessedLanguage == null ) {
			throw new IllegalArgumentException( "Guessing the type of the given file (" + filename + ") failed." );
		}

		if ( guessedLanguage.isTriples() ) {
			MyTripleSink sink = new MyTripleSink();
			RiotReader.parseTriples( filename, guessedLanguage, null, sink );
			putRDFGraph( sink, new NaiveRDFGraphProvenanceImpl(null) );
		}
		else {
			DatasetGraph arqDataset = RiotLoader.load( filename, guessedLanguage );
			putRDFGraphs( arqDataset );
		}
	}

	class MyTripleSink implements Sink<com.hp.hpl.jena.graph.Triple>, Iterator<Triple>
	{
		List<Triple> cachedTriples = new ArrayList<Triple> ();

		// implementation of the Sink interface
		public void send ( com.hp.hpl.jena.graph.Triple t ) { cachedTriples.add( EncodingTriplesIterator.encode(nodeDict,t) ); }
		public void flush () {} // nothing to flush

		// implementation of the Closeable interface
		public void close () {} // nothing to close

		// implementation of the Iterator interface
		public boolean hasNext () { return ! cachedTriples.isEmpty(); }
		public Triple next () { return cachedTriples.remove( 0 ); }
		public void remove () {} // nothing to remove
	}


	// implementation of the QueriedDataset interface

	/**
	 * Attention, call this method only if you know what you do, otherwise use
	 * {@link #putRDFGraph(com.hp.hpl.jena.graph.Graph,RDFGraphProvenance)}.
	 * In particular, make sure that the given triples have been encoded
	 * with the {@link NodeDictionary} of this queried dataset (i.e. with
	 * {@link #nodeDict}).
	 */
	public void putRDFGraph ( Iterator<Triple> itTriples, RDFGraphProvenance prv )
	{
		wrappedDataset.putRDFGraph( itTriples, prv );
	}

	public void removeRDFGraph ( URL src )
	{
		wrappedDataset.removeRDFGraph( src );
	}

// 	public Iterator<Triple> find ( long s, long p, long o );
	public Iterator<Triple> find ( int s, int p, int o )
	{
		return wrappedDataset.find( s, p, o );
	}

	public Iterator<TraceableTriple> findWithProvenance ( int s, int p, int o )
	{
		return wrappedDataset.findWithProvenance( s, p, o );
	}

	public boolean containsRDFGraphWithSourceURL ( URL url )
	{
		return wrappedDataset.containsRDFGraphWithSourceURL( url );
	}

	public Set<URL> getRDFGraphsSourceURLs ()
	{
		return wrappedDataset.getRDFGraphsSourceURLs();
	}

	public int countRDFGraphs ()
	{
		return wrappedDataset.countRDFGraphs();
	}


	// implementation of the StatisticsProvider interface

	public Statistics getStatistics ()
	{
		StatisticsImpl.AttributeList statAttrs = new StatisticsImpl.AttributeList();
		statAttrs.add( "wrappedDataset", wrappedDataset.getStatistics() );
		statAttrs.add( "nodeDict", nodeDict.getStatistics() );

		if ( StatisticsImpl.isObjectProfilingPossible() ) {
			statAttrs.add( "size", StatisticsImpl.sizeof(this) );
		}

		return new StatisticsImpl( statAttrs );
	}


	// additional operations

	public void putRDFGraph ( Graph jenaGraph, RDFGraphProvenance prv )
	{
		EncodingTriplesIterator itTriples = new EncodingTriplesIterator( nodeDict, jenaGraph.find(null,null,null) );
		putRDFGraph( itTriples, prv );
	}

	public void putRDFGraphs ( DatasetGraph dsg )
	{
		Iterator<Node> itGraphNodes = dsg.listGraphNodes();
		while ( itGraphNodes.hasNext() ) {
			Node gn = itGraphNodes.next();

			URL src;
			if ( ! gn.isURI() ) {
				logger.warn( "The current graph node ({}) is not a URI. Ignore the corresponding RDF graph.", gn.toString() );
				continue;
			}
			try {
				src = new URL( gn.getURI() );
			} catch ( MalformedURLException e ) {
				logger.warn( "The current graph node URI ({}) cannot parsed as a URL. Ignore the corresponding RDF graph.", gn.toString() );
				continue;
			}

// throw new UnsupportedOperationException( "TODO: JenaIOBasedQueriedDataset.putRDFGraphs - we need an RDFGraphProvenance object for these graphs" );
			putRDFGraph ( dsg.getGraph(gn), new NaiveRDFGraphProvenanceImpl(src) );

		}
	}

	public void clear ()
	{
// TODO:
// 		throw new UnsupportedOperationException ();
	}

	public void close ()
	{
// TODO:
// 		throw new UnsupportedOperationException ();
	}

	public String toString ()
	{
// 		return "JenaIOBasedQueriedDataset with " + countQuads() + " quads in " + countRDFGraphs() + " graphs";
		return "JenaIOBasedQueriedDataset with " + countRDFGraphs() + " graphs";
	}


	static class NaiveRDFGraphProvenanceImpl implements RDFGraphProvenance
	{
		final public URL url;
		public NaiveRDFGraphProvenanceImpl ( URL url ) { this.url = url; }
		public URL getAccessedResourceURL () { return url; }
		public java.util.Date getRetrievalTime () { throw new UnsupportedOperationException( "TODO: JenaIOBasedQueriedDataset.putRDFGraphs - we need an RDFGraphProvenance object for these graphs" ); }
	}
}
