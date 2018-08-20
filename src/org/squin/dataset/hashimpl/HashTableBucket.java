/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.hashimpl;

import java.util.ArrayList;


/**
 * This class represents a bucket of a hash table.
 * Note, using ArrayList as superclass (instead of other List or Set
 * implementations) was not a random choice. Experiments revealed that
 * ArrayList is better w.r.t the consumed memory and the query execution
 * times than LinkedList or Vector, and it is much better than HashSet.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class HashTableBucket<T> extends ArrayList<T>
{
	// initialization

	/** Creates a new, empty bucket. */
	public HashTableBucket ()
	{
	}

	/** Creates a copy of the given bucket. */
	public HashTableBucket ( HashTableBucket<T> tmpl )
	{
		super( tmpl );
	}

}
