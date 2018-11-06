package org.wimu.datasetselection.resultsTotalDatasets;

import java.util.Set;

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

	private static void runQueryWIMU(String query) throws Exception {
		System.out.println("Now the same query with WIMU:");
		long start = System.currentTimeMillis();
		boolean onlyDatasets = false;
		WimuResult wResult = WimuSelection.execQuery(query, onlyDatasets, 600000);
		long totalTime = System.currentTimeMillis() - start;
		
		System.out.println("Best Dataset to execute the query: " + wResult.getBestDataset());
		System.out.println("With " + wResult.getSize() + " results.");
		System.out.println("TotalTime WIMU: " + totalTime);
		//wResult.printResults();
	}
}
