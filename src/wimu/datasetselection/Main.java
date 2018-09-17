package org.wimu.datasetselection;

import java.io.IOException;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		//LogCtl.setLog4j("log4j.properties");
		//org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		int initSize = Util.mUriDataset.size();
		try {
			Util.loadFileMap("URI_Dataset.tsv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long start = System.currentTimeMillis(); 
		//Set<String> setQueries = Util.getSampleQueries("/home/andre/queries.txt");
		
		String file = args[0];
		Set<String> setQueries = Util.getSampleQueries(file);
		System.out.println("File with the queries: " + file);
		System.out.println("######## Number of queries: "+setQueries.size());
		
		System.out.println("Number of queries: " + setQueries.size());
		Set<WimuTQuery> res = null;
		if(args[1].equals("all")){
			res = Util.executeAllQueries(setQueries);
		} else if(args[1].equals("squin")){
			res = Util.executeQueriesSquin(setQueries);
		} else if(args[1].equals("wimut")){
			res = Util.executeQueriesWimuT(setQueries);
		}
		long totalTime = System.currentTimeMillis() - start;
		Util.writeFile(res, "results.tsv");
		System.out.println("Total Time (ms): " + totalTime);
		if(Util.mUriDataset.size() > initSize) {
			Util.writeFile(Util.mUriDataset, "URI_Dataset.tsv");
		}
	}
}
