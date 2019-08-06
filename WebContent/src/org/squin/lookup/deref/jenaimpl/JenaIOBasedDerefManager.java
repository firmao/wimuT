/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.jenaimpl;

import com.hp.hpl.jena.graph.Node;

import org.squin.common.Priority;
import org.squin.dataset.Triple;
import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.lookup.DataImporter;
import org.squin.lookup.deref.DataAnalyzer;
import org.squin.lookup.deref.impl.DerefTask;
import org.squin.lookup.deref.impl.URIDerefManagerBase;


/**
 * A {@link org.squin.lookup.deref.URIDerefManager} that makes use of the Jena
 * framework to parse RDF data retrieved from the Web.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class JenaIOBasedDerefManager extends URIDerefManagerBase
{
	// members

	final public JenaIOBasedDerefContext derefCxt;


	// initialization

	public JenaIOBasedDerefManager ( NodeDictionary nodeDict )
	{
		this( new JenaIOBasedDerefContext(nodeDict) );
	}

	public JenaIOBasedDerefManager ( JenaIOBasedDerefContext derefCxt )
	{
		assert derefCxt != null;
		this.derefCxt = derefCxt;
	}


	// implementation of the URIDerefManagerBase abstract worker methods

	final protected DerefTask createDerefTask ( int uriID, Priority priority, DataImporter importer, DataAnalyzer analyzer )
	{
		return new JenaIOBasedDerefTask( derefCxt, uriID, priority, importer, analyzer );
	}

	protected void checkDereferenceability ( int uriID ) throws IllegalArgumentException
	{
		Node n = derefCxt.nodeDict.getNode( uriID );
		if ( n == null ) {
			throw new IllegalArgumentException( "The given URI identifier " + uriID + " is unknown." );
		}
		else if ( ! n.isURI() ) {
			throw new IllegalArgumentException( "The given identifier " + uriID + " does not identify a URI but " + n.toString() + "." );
		}

		String uriString = n.getURI();
		if ( ! uriString.startsWith("http://") ) {
			throw new IllegalArgumentException( "The URI <" + uriString + "> identified by " + uriID + " is not an HTTP scheme based URI." );
		}
		if ( uriString.contains("#") ) {
			throw new IllegalArgumentException( "The URI <" + uriString + "> identified by " + uriID + " is a hash URI and, thus, not dereferenceable." );
		}
	}

	public int getAsDereferenceableURI ( int uriID )
	{
		Node n = derefCxt.nodeDict.getNode( uriID );
		if ( n == null || ! n.isURI() ) {
			return Triple.UNKNOWN_IDENTIFIER;
		}

		String uriString = n.getURI();
		if ( ! uriString.startsWith("http://") ) {
			return Triple.UNKNOWN_IDENTIFIER;
		}

		if ( ! uriString.contains("#") ) {
			return uriID;
		}
		else {
			String derefUriString = uriString.substring( 0, uriString.indexOf("#") );
			return derefCxt.nodeDict.createId( Node.createURI(derefUriString) );
		}
	}

}
