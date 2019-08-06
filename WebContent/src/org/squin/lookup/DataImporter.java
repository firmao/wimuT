/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup;

import java.util.Iterator;

import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.Triple;


/**
 * This interface represents a component that imports data retrieved during a
 * URI look-up.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface DataImporter
{
	/**
	 * Imports the given set of ID-encoded triples.
	 *
	 * @param data the triples to be imported
	 * @param prv the provenance of the data that has to be imported
	 */
	public void importData ( Iterator<Triple> data, RDFGraphProvenance prv );
}
