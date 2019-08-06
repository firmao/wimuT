/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.ldcache.jenaimpl;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.squin.common.Priority;
import org.squin.dataset.jenacommon.DecodingTriplesIterator;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.DataRetrievedListener;
import org.squin.ldcache.impl.LinkedDataCacheImpl;
import org.squin.lookup.URILookUpManager;
import org.squin.lookup.jenaimpl.JenaIOBasedURILookUpManager;


/**
 * An implementation of the {@link org.squin.ldcache.LinkedDataCache} interface
 * that makes use of the Jena framework to load and parse RDF data that has been
 * retrieved from the Web.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class JenaIOBasedLinkedDataCache extends LinkedDataCacheImpl
{
	// initialization

	public JenaIOBasedLinkedDataCache ( JenaIOBasedQueriedDataset dataset, URILookUpManager lookupMgr )
	{
		super( dataset, lookupMgr );
	}

	public JenaIOBasedLinkedDataCache ( JenaIOBasedQueriedDataset dataset )
	{
		this( dataset, new JenaIOBasedURILookUpManager(dataset) );
	}


	// accessor methods

	final public NodeDictionary getNodeDictionary ()
	{
		return ( (JenaIOBasedQueriedDataset) dataset ).nodeDict;
	}


	// operations

	final public boolean ensureAvailability ( AccessContext ac, Node uriNode, Priority prio )
	{
		return ensureAvailability( ac, uriNode, prio, null );
	}

	final public boolean ensureAvailability ( AccessContext ac, Node uriNode, Priority prio, DataRetrievedListener l )
	{
		if ( uriNode == null || ! uriNode.isURI() ) {
// 			log.debug( "ensureAvailability requested for a Node which is unknown or not a URI: {}.", uriNode );
			return true;
		}

		return ensureAvailability( ac, getNodeDictionary().createId(uriNode), prio, l );
	}

	public Graph asJenaGraph ()
	{
		return new GraphBase () {
			@Override
			protected ExtendedIterator<Triple> graphBaseFind ( TripleMatch m )
			{
				int s = ( m.getMatchSubject() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : getNodeDictionary().createId( m.getMatchSubject() );
				int p = ( m.getMatchPredicate() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : getNodeDictionary().createId( m.getMatchPredicate() );
				int o = ( m.getMatchObject() == null ) ? org.squin.dataset.Triple.UNKNOWN_IDENTIFIER : getNodeDictionary().createId( m.getMatchObject() );
				return new DecodingTriplesIterator( getNodeDictionary(), findWrap(s,p,o) );
			}

			@Override
			public void performAdd ( Triple t ) { throw new UnsupportedOperationException(); }

			@Override
			public void performDelete ( Triple t ) { throw new UnsupportedOperationException(); }
		};
	}


// 	// re-implementation of the LinkedDataCacheBase abstract worker methods
// 
// 	@Override
// 	protected boolean ensureAvailability ( int uriID, Priority prio, DataRetrievedListener l )
// 	{
// 		Node uriNode = getNodeDictionary().getNode( uriID );
// 		if ( uriNode == null || ! uriNode.isURI() ) {
// // 			log.warn( "ensureAvailability requested for node ID {} ({}) which is unknown or not a URI.", uriID, uriNode );
// 			return true;
// 		}
// 
// 		return super.ensureAvailability( uriID, prio, l );
// 	}


	// helpers

	final private Iterator<org.squin.dataset.Triple> findWrap ( int s, int p, int o )
	{
		return find( s, p, o );
	}

}
