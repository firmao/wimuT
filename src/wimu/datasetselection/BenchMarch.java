package org.wimu.datasetselection;

import java.util.Set;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.hashimpl.individual.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;

public class BenchMarch {
	
	public static void main(String args[]) throws Exception{
		Set<String> querys = Util.getQueries();
		int idQuery = 0;
		long start = System.currentTimeMillis();
		for(String query : querys){
			System.out.println("Query: " + (++idQuery));
			//runQuerySQUIN(query, idQuery);
			runQueryWIMU(query);
		}
		long totalTime = System.currentTimeMillis() - start;
		System.out.println("TotalTime ALL: " + totalTime);
	}

	private static void runQuerySQUIN(String query, int idQuery) {
		long start = System.currentTimeMillis();
		LinkTraversalBasedQueryEngine.register();
		QueriedDataset qds = new QueriedDatasetImpl();
		JenaIOBasedQueriedDataset qdsWrapper = new JenaIOBasedQueriedDataset( qds );
		JenaIOBasedLinkedDataCache ldcache = new JenaIOBasedLinkedDataCache( qdsWrapper );
		Dataset dsARQ = new LinkedDataCacheWrappingDataset( ldcache );
		QueryExecution qe = QueryExecutionFactory.create( query, dsARQ );
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			System.out.println("SQUIN got results for query: " + idQuery);
		}
		//System.out.println(ResultSetFormatter.asText(results));
		try {
			ldcache.shutdownNow( 4000 ); // 4 sec.
		} catch ( Exception e ) {
			System.err.println( "Shutting down the Linked Data cache failed: " + e.getMessage() );
		}
		long totalTime = System.currentTimeMillis() - start;
		System.out.println("TotalTime SQUIN: " + totalTime);
	}

	private static void runQueryWIMU(String query) throws Exception {
		System.out.println("Now the same query with WIMU:");
		long start = System.currentTimeMillis();
		boolean onlyDatasets = false;
		WimuResult wResult = WimuSelection.execQuery(query, onlyDatasets);
		long totalTime = System.currentTimeMillis() - start;
		
		System.out.println("Best Dataset to execute the query: " + wResult.getBestDataset());
		System.out.println("With " + wResult.getSize() + " results.");
		System.out.println("TotalTime WIMU: " + totalTime);
		//wResult.printResults();
	}
}
