/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl.combined;

import org.squin.dataset.RDFGraphProvenance;


/**
 * This class represents a descriptor object (an RDF graph) that is stored in
 * a {@link CombinedIndex}.
 * This class is thread-safe.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class IndexedRDFGraph
{
	// class members

	/**
	 * This status indicates that the RDF graph has just been created.
	 */
	final static public int STATUS_NEW = 0;

	/**
	 * This status indicates that the RDF graph is currently being added to the
	 * index.
	 */
	final static public int STATUS_BEING_INDEXED = 1;

	/**
	 * This status indicates that the RDF graph is completely contained in the
	 * index and its containment is valid.
	 * Only valid graphs can be used for query execution (i.e. triple pattern
	 * matching). The containment might be invalid if a more recent version of
	 * the graph has been retrieved and added to the index but this out-dated
	 * version has not been removed from the index yet.
	 */
	final static public int STATUS_INDEXED_VALID = 2;

	/**
	 * This status indicates that the RDF graph is completely contained in the
	 * index but its containment is invalid.
	 */
	final static public int STATUS_INDEXED_INVALID = 4;

	/**
	 * This status indicates that the RDF graph is currently being removed from
	 * the index.
	 */
	final static public int STATUS_BEING_REMOVED = 8;

	/**
	 * This status indicates that the RDF graph has been removed from the index.
	 */
	final static public int STATUS_REMOVED = 16;

	// members

	/** The identifier of the represented RDF graph. */
	final public RDFGraphProvenance prv;

	/** The current status of the represented RDF graph. */
	private int status = STATUS_NEW;


	// initialization

	public IndexedRDFGraph ( RDFGraphProvenance prv )
	{
		assert prv != null;
		this.prv = prv;
	}


	// accessors

	/**
	 * Returns the status of the containment of this RDF graph in the index.
	 */
	synchronized public int getStatus ()
	{
		return status;
	}

	/**
	 * Returns true if the containment of this RDF graph in the index is valid.
	 * Only valid graphs can be used for query execution (i.e. triple pattern
	 * matching). The containment might be invalid if a more recent version of
	 * the graph has been retrieved and added to the index but this out-dated
	 * version has not been removed from the index yet.
	 */
	synchronized public boolean isValid ()
	{
		return status == STATUS_INDEXED_VALID;
	}

	/**
	 * Returns true if this RDF graph is completely contained in the index.
	 */
	synchronized public boolean isIndexed ()
	{
		return (status == STATUS_INDEXED_VALID) || (status == STATUS_INDEXED_INVALID);
	}

	/**
	 * Sets the containment of this RDF graph in the index to valid.
	 */
	synchronized public void setBeingIndexed ()
	{
		if (    status == STATUS_INDEXED_VALID
		     || status == STATUS_INDEXED_INVALID
		     || status == STATUS_BEING_REMOVED ) {
			throw new IllegalStateException( "The current status (" + status + ") cannot be changed as requested." );
		}

		status = STATUS_BEING_INDEXED;
	}

	synchronized public void setIndexedValid ()
	{
		if (    status != STATUS_INDEXED_VALID
		     && status != STATUS_BEING_INDEXED ) {
			throw new IllegalStateException( "The current status (" + status + ") cannot be changed as requested." );
		}

		status = STATUS_INDEXED_VALID;
	}

	synchronized public void setIndexedInvalid ()
	{
		if ( status != STATUS_INDEXED_VALID ) {
			throw new IllegalStateException( "The current status (" + status + ") cannot be changed as requested." );
		}

		status = STATUS_INDEXED_INVALID;
	}

	synchronized public void setBeingRemoved ()
	{
		if ( status != STATUS_INDEXED_INVALID ) {
			throw new IllegalStateException( "The current status (" + status + ") cannot be changed as requested." );
		}

		status = STATUS_BEING_REMOVED;
	}

	synchronized public void setRemoved ()
	{
		if ( status != STATUS_BEING_REMOVED ) {
			throw new IllegalStateException( "The current status (" + status + ") cannot be changed as requested." );
		}

		status = STATUS_REMOVED;
	}

}
