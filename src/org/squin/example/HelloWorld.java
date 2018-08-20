package org.squin.example;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.hashimpl.individual.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;
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
		
//		String queryString = 
//			"PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#>\n" +  
//			"PREFIX swrc: <http://swrc.ontoware.org/ontology#>\n" + 
//			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
//			"PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
//			"SELECT DISTINCT ?author ?phone\n" +
//			"WHERE {\n" +
//			"    <http://data.semanticweb.org/conference/eswc/2009/proceedings> swc:hasPart ?pub .\n" + 
//			"    ?pub swc:hasTopic ?topic .\n" + 
//			"    ?topic rdfs:label ?topicLabel .\n" + 
//			"    FILTER regex ( str(?topicLabel), \"ontology_engineering\", \"i\" ) .\n" + 
//			"    ?pub swrc:author ?author .\n" + 
//			"    { ?author owl:sameAs ?authAlt } UNION { ?authAlth owl:sameAs ?author }\n" + 
//			"    ?authAlt foaf:phone ?phone .\n" + 
//			"}\n";
		String queryString = "SELECT ?uri ?id WHERE {?uri <http://dbpedia.org/ontology/wikiPageID> ?id. FILTER (?uri = <http://dbpedia.org/resource/Weight_gain>) }";
		//String queryString = "SELECT ?p ?o { <http://nasa.dataincubator.org/spacecraft/1968-089A> ?p ?o}";
		QueryExecution qe = QueryExecutionFactory.create( queryString, dsARQ );
		ResultSet results = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(results));
		try {
			ldcache.shutdownNow( 4000 ); // 4 sec.
		} catch ( Exception e ) {
			System.err.println( "Shutting down the Linked Data cache failed: " + e.getMessage() );
		}
		
		System.out.println("Now the same query with WIMU:");
		long start = System.currentTimeMillis();
		WimuResult wResult = WimuSelection.execQuery(queryString, false);
		long totalTime = System.currentTimeMillis() - start;
		
		System.out.println("Best Dataset to execute the query: " + wResult.getBestDataset());
		System.out.println("TotalTime: " + totalTime);
		wResult.printResults();
	}

}