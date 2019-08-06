/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.jenaimpl;

import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.lookup.deref.URIDerefContext;


/**
 * Represents the context of {@link org.squin.lookup.deref.impl.DerefTask}s
 * that make use of the Jena framework to parse RDF data retrieved from the
 * Web.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class JenaIOBasedDerefContext extends URIDerefContext
{
	// members

	final public NodeDictionary nodeDict;


	// initialization

	public JenaIOBasedDerefContext ( NodeDictionary nodeDict )
	{
		assert nodeDict != null;
		this.nodeDict = nodeDict;
	}

}
