/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq.iterators;

import java.util.Iterator;

import org.openjena.atlas.lib.Closeable;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;

import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.dataset.query.SolutionMapping;
import org.squin.dataset.query.arq.IdBasedExecutionContext;
import org.squin.dataset.query.arq.VarDictionary;


/**
 * This iterator converts {@link SolutionMapping}s to {@link Binding}s.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class DecodeBindingsIterator extends QueryIter
{
	// members

	final protected NodeDictionary nodeDict;
	final protected VarDictionary varDict;

	/** the input iterator consumed by this one */
	final protected Iterator<SolutionMapping> input;


	// initialization

	public DecodeBindingsIterator ( Iterator<SolutionMapping> input, IdBasedExecutionContext execCxt )
	{
		super( execCxt );

		this.input = input;
		this.nodeDict = execCxt.nodeDict;
		this.varDict = execCxt.varDict;
	}

	// implementation of the QueryIteratorBase abstract methods

	protected boolean hasNextBinding ()
	{
		return input.hasNext();
	}

	protected Binding moveToNextBinding ()
	{
		SolutionMapping curInput = input.next();

		BindingHashMap curOutput = new BindingHashMap();
		for ( int i = curInput.size() - 1; i >= 0; i-- )
		{
			if ( curInput.contains(i) ) {
				curOutput.add( varDict.getVar(i),
				               nodeDict.getNode(curInput.get(i)) );
			}
		}

		return curOutput;
	}

	protected void requestCancel ()
	{
		// Do we have to cancel the (chain of) input iterator(s) ?
		throw new UnsupportedOperationException( "TODO (DecodeBindingsIterator.requestCancel)" );
	}

	protected void closeIterator ()
	{
		if ( input instanceof Closeable ) {
			( (Closeable) input ).close();
		}
	}

}
