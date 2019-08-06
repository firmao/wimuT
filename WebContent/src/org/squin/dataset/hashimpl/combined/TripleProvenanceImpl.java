/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import java.util.HashSet;
import java.util.Set;

import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.TripleProvenance;


/**
 * This class implements the {@link TripleProvenance} interface.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class TripleProvenanceImpl implements TripleProvenance
{
	// members

	final public Set<RDFGraphProvenance> provenanceOfContainingRDFGraphs;


	// initialization

	public TripleProvenanceImpl ( RDFGraphProvenance provenanceOfContainingRDFGraph )
	{
		assert provenanceOfContainingRDFGraph != null;
		provenanceOfContainingRDFGraphs = new HashSet<RDFGraphProvenance> ();
		provenanceOfContainingRDFGraphs.add( provenanceOfContainingRDFGraph );
	}

	public TripleProvenanceImpl ( Set<RDFGraphProvenance> provenanceOfContainingRDFGraphs )
	{
		assert provenanceOfContainingRDFGraphs != null;
		this.provenanceOfContainingRDFGraphs = provenanceOfContainingRDFGraphs;
	}

}
