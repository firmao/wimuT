package org.wimu.datasetselection.bgploadalot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.squin.dataset.QueriedDataset;
import org.squin.dataset.hashimpl.combined.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class Traversal {

	public static String execTraversal(String query) {
		final Set<String> ret = new HashSet<String>();
		LinkTraversalBasedQueryEngine.register();
		QueriedDataset qds = new QueriedDatasetImpl();
		JenaIOBasedQueriedDataset qdsWrapper = new JenaIOBasedQueriedDataset( qds );
		JenaIOBasedLinkedDataCache ldcache = new JenaIOBasedLinkedDataCache( qdsWrapper );
		Dataset dsARQ = new LinkedDataCacheWrappingDataset( ldcache );
		QueryExecution qe = QueryExecutionFactory.create( query, dsARQ );
		ResultSet results = qe.execSelect();
		
		int count = 0;
		Util.updateCount(Approach.TRAVERSAL, count);
		List<QuerySolution> lst = ResultSetFormatter.toList(results);
		for (QuerySolution qSolution : lst) {
			final StringBuffer sb = new StringBuffer();
			for ( final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext(); ) {
                final String varName = varNames.next();
                sb.append(qSolution.get(varName).toString() + " ");
            }
			ret.add(sb.toString() + "\n");
		}
		try {
			ldcache.shutdownNow( 4000 ); // 4 sec.
		} catch ( Exception e ) {
			System.err.println( "Shutting down the Linked Data cache failed: " + e.getMessage() );
		}
		Util.updateCount(Approach.TRAVERSAL, ret.size());
		final StringBuffer res = new StringBuffer(); 
		for (String line : ret) {
			res.append(line);
		}
		return res.toString();
	}

}
