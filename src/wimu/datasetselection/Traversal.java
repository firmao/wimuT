package org.wimu.datasetselection;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.hashimpl.combined.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class Traversal {

	public static String execTraversal(String query) {
		String ret = null;
		LinkTraversalBasedQueryEngine.register();
		QueriedDataset qds = new QueriedDatasetImpl();
		JenaIOBasedQueriedDataset qdsWrapper = new JenaIOBasedQueriedDataset( qds );
		JenaIOBasedLinkedDataCache ldcache = new JenaIOBasedLinkedDataCache( qdsWrapper );
		Dataset dsARQ = new LinkedDataCacheWrappingDataset( ldcache );
		QueryExecution qe = QueryExecutionFactory.create( query, dsARQ );
		ResultSet results = qe.execSelect();
		int count = ResultSetFormatter.consume(results);
		Util.updateCount("traversal", count);
		ret = "execTraversal: " + count;
		//ret = ResultSetFormatter.asText(results);
		//System.out.println(ResultSetFormatter.asText(results));
		//boolean hasResults = results.hasNext(); 
		try {
			ldcache.shutdownNow( 4000 ); // 4 sec.
		} catch ( Exception e ) {
			System.err.println( "Shutting down the Linked Data cache failed: " + e.getMessage() );
		}
		return ret;
	}

}
