package org.wimu.datasetselection.parallelv1;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;

public class MainParallelv1 {

	public static void main(String[] args) {
		LogCtl.setLog4j("log4j.properties");
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		MemoryControl.beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		int initSize = Util.mUriDataset.size();
		try {
			Util.loadFileMap("URI_Dataset.tsv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long start = System.currentTimeMillis();
		// Set<String> setQueries = Util.getSampleQueries("/home/andre/queries.txt");

		String fName = args[0];
		File fDir = new File(fName);
		if (!fDir.isDirectory()) {
			List<String> setQueries = Util.getSampleQueries(fDir);
			System.out.println(" MainParallelv2 - <All DATASETS FROM URI + Results> -File with the queries: " + fName);
			System.out.println("######## MainResults - Number of queries: " + setQueries.size());

			System.out.println("Number of queries: " + setQueries.size());
			List<WimuTQuery> res = null;
			if (args[1].equals("all")) {
				res = Util.executeAllQueries(setQueries);
			} else if (args[1].equals("squin")) {
				res = Util.executeQueriesTraversal(setQueries);
			} else if (args[1].equals("wimut")) {
				res = Util.executeQueriesWimuT(setQueries);
			} else if (args[1].equals("lodalot")) {
				res = Util.executeQueriesLODaLOT(setQueries);
			}
			long totalTime = System.currentTimeMillis() - start;
			Util.writeFile(res, fName.substring(0, fName.length() - 4) + "_results.tsv");
			System.out.println("Total Time (ms): " + totalTime);
			if (Util.mUriDataset.size() > initSize) {
				Util.writeFile(Util.mUriDataset, "URI_Dataset.tsv");
			}
		} else {
			File[] allSubFiles = fDir.listFiles();
			for (File file : allSubFiles) {
				List<String> setQueries = Util.getSampleQueries(file);
				System.out.println(" MainParallelv2 - <All DATASETS FROM URI + Results> -File with the queries: "
						+ file.getAbsolutePath());
				System.out.println("######## MainResults - Number of queries: " + setQueries.size());

				System.out.println("Number of queries: " + setQueries.size());
				List<WimuTQuery> res = null;
				if (args[1].equals("all")) {
					res = Util.executeAllQueries(setQueries);
				} else if (args[1].equals("squin")) {
					res = Util.executeQueriesTraversal(setQueries);
				} else if (args[1].equals("wimut")) {
					res = Util.executeQueriesWimuT(setQueries);
				} else if (args[1].equals("lodalot")) {
					res = Util.executeQueriesLODaLOT(setQueries);
				}
				long totalTime = System.currentTimeMillis() - start;
				Util.writeFile(res, file.getName().substring(0, file.getName().length() - 4) + "_results.tsv");
				System.out.println("Total Time (ms): " + totalTime);
				if (Util.mUriDataset.size() > initSize) {
					Util.writeFile(Util.mUriDataset, "URI_Dataset.tsv");
				}
			}
		}
		System.out.println("FINISHED !!!");
	}
}
