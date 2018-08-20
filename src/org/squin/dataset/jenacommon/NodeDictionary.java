/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.jenacommon;

import com.hp.hpl.jena.graph.Node;

import org.squin.common.Statistics;
import org.squin.common.StatisticsProvider;


/**
 * A dictionary that assigns identifiers to RDF nodes.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface NodeDictionary extends StatisticsProvider
{
	/**
	 * Returns the RDF term (Jena Node object) identified by the given
	 * identifier, or null if there is no such identifier in the dictionary.
	 */
// 	public Node getNode ( long id );
	public Node getNode ( int id );

	/**
	 * Returns the identifier that identifies the given RDF term (Jena Node
	 * object), or {@link org.squin.dataset.Triple#UNKNOWN_IDENTIFIER} if
	 * there is no such node in the dictionary.
	 */
// 	public long getId ( Node n );
	public int getId ( Node n );

	/**
	 * Returns an identifier that identifies the given RDF term (Jena Node
	 * object).
	 * If there is no identifier for the given node, yet, this method creates
	 * a new identifier and adds it to the dictionary.
	 */
// 	public long createId ( Node n );
	public int createId ( Node n );

	/**
	 * Returns statistics about this dictionary.
	 * The actual statistics depends on the implementation.
	 */
	public Statistics getStatistics ();

}
