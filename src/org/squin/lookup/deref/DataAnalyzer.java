/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref;

import java.util.Iterator;

import org.squin.dataset.Triple;


/**
 * This is an abstract base class for components that
 * analyze data retrieved by dereferencing URIs.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
abstract public class DataAnalyzer
{
	// abstract methods that must be implemented by derived classes

	/**
	 * An implementation of this method returns a DataAnalyzingIterator
	 * (constructed with the given iterator as input) for this DataAnalyzer.
	 */
	abstract public DataAnalyzingIterator createDataAnalyzingIterator ( Iterator<Triple> input );


	/**
	 * Base class for iterators used by a {@link DataAnalyzer}.
	 * This iterator wraps another {@link Triple} iterator and iterates over all
	 * results of that input iterator. The method {@link #analyze} is a hook to
	 * analyze each {@link Triple} consumed from this iterator.
	 */
	static abstract public class DataAnalyzingIterator implements Iterator<Triple>
	{
		// abstract methods that must be implemented by derived classes

		abstract protected void analyze ( Triple t );


		// members

		final private Iterator<Triple> input;


		// initialization

		protected DataAnalyzingIterator ( Iterator<Triple> input ) { this.input = input; }


		// implementation of the Iterator interface

		final public void remove () { input.remove(); }

		final public boolean hasNext () { return input.hasNext(); }

		final public Triple next ()
		{
			Triple t = input.next();
			analyze( t );
			return t;
		}
	}

}
