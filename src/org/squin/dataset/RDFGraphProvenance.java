/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset;

import java.net.URL;
import java.util.Date;


/**
 * This interface represents the provenance of an RDF graph
 * that is stored in the query-local dataset.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface RDFGraphProvenance
{
	/**
	 * Returns the URL which was dereferenced to retrieve the
	 * corresponding RDF graph.
	 */
	URL getAccessedResourceURL ();

	/**
	 * Returns the time at which the corresponding RDF graph
	 * was retrieved from the Web.
	 */
	Date getRetrievalTime ();
}
