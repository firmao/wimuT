/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.dataset.query.arq.iterators;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterAssign;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterProcessBinding;

import org.squin.dataset.jenacommon.NodeDictionary;
import org.squin.dataset.query.arq.IdBasedExecutionContext;


/**
 * This iterator wraps a {@link com.hp.hpl.jena.sparql.engine.iterator.QueryIterAssign}
 * iterator to ensure the assigned values are in the node dictionary.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class QueryIterAssignWrapper extends QueryIterProcessBinding
{
	// members

	final protected NodeDictionary nodeDict;


	// initialization

	public QueryIterAssignWrapper( QueryIterAssign input, IdBasedExecutionContext execCxt )
	{
		super( input, execCxt );
		this.nodeDict = execCxt.nodeDict;
	}


	@Override
	public Binding accept ( Binding b )
	{
		Iterator<Var> itVar = b.vars();
		while ( itVar.hasNext() ) {
			nodeDict.createId( b.get(itVar.next()) );
		}

		return b;
	}

}
