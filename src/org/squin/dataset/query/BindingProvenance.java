/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query;

import org.squin.dataset.TraceableTriple;


/**
 * This interface represents the provenance of a specific binding for a query
 * variable within a specific solution mapping.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface BindingProvenance
{
	TraceableTriple getMatchingTriple ();
	TriplePattern getMatchedTriplePattern ();
}
