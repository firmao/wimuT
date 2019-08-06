/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;


/**
 * An implementation of an ARQ {@link com.hp.hpl.jena.query.Dataset} that wraps
 * a {@link org.squin.ldcache.LinkedDataCache} in order to enable making use of
 * the ARQ query processing framework.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkedDataCacheWrappingDataset extends DatasetImpl
                                            implements Dataset
{
	public LinkedDataCacheWrappingDataset ( JenaIOBasedLinkedDataCache ldcache )
	{
		super( new LinkedDataCacheWrappingDatasetGraph(ldcache) );
	}
}
