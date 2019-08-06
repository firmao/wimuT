package org.squin.example;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.hashimpl.individual.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;
import org.wimu.datasetselection.Util;
import org.wimu.datasetselection.WimuResult;
import org.wimu.datasetselection.WimuSelection;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class HelloWorld {

	public static void main(String[] args) throws Exception {
		LinkTraversalBasedQueryEngine.register();
		QueriedDataset qds = new QueriedDatasetImpl();
		JenaIOBasedQueriedDataset qdsWrapper = new JenaIOBasedQueriedDataset( qds );
		JenaIOBasedLinkedDataCache ldcache = new JenaIOBasedLinkedDataCache( qdsWrapper );
		Dataset dsARQ = new LinkedDataCacheWrappingDataset( ldcache );
		
		String queryString = "SELECT ?uri ?id WHERE {?uri <http://dbpedia.org/ontology/wikiPageID> ?id. FILTER (?uri = <http://dbpedia.org/resource/Weight_gain>) }";
		//String queryString = "SELECT ?p ?o { <http://nasa.dataincubator.org/spacecraft/1968-089A> ?p ?o}";
		
		queryString = Util.forceDerefURIs(queryString);
		
		QueryExecution qe = QueryExecutionFactory.create( queryString, dsARQ );
		ResultSet results = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(results));
		try {
			ldcache.shutdownNow( 4000 ); // 4 sec.
		} catch ( Exception e ) {
			System.err.println( "Shutting down the Linked Data cache failed: " + e.getMessage() );
		}
		
//		System.out.println("Now the same query with WIMU:");
//		long start = System.currentTimeMillis();
//		WimuResult wResult = WimuSelection.execQuery(queryString, false);
//		long totalTime = System.currentTimeMillis() - start;
//		
//		System.out.println("Best Dataset to execute the query: " + wResult.getBestDataset());
//		System.out.println("TotalTime: " + totalTime);
		//wResult.printResults();
	}

}