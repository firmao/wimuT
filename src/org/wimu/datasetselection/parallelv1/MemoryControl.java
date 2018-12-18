package org.wimu.datasetselection.parallelv1;

import java.io.PrintWriter;

public class MemoryControl {
	public static final long LIMIT = 1073741824; // 1GB
	//public static final long LIMIT = 10; // 1KB
	private static int indFile = 0;
	public static long beforeUsedMem = 0;
	public static long afterUsedMem = 0;
	
	public static void main(String[] args) {
		String results = "Testing...";
		String cSparql = "select test";
		String approach = Approach.WIMU_DUMP;
		int size = 10;
		copyToDisk(results, cSparql, approach, size);
		copyToDisk(results, cSparql, approach, size);
	}

	public static void copyToDisk(String results, String cSparql, String approach, int size) {
		indFile++;
		String fileName = approach + "_" + indFile + ".txt";
		System.out.println(approach + " -- LIMIT OF MEMORY, copying to disk results from SPARQL: " + cSparql);
		System.out.println("FileDump: " + fileName);
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println("#------SPARQL:\n" + cSparql + "\n");
			writer.println("#------ResultSIZE=" + size);
			writer.println(results);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isLimitMemory() {
		afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		long actualMemUsed=afterUsedMem-beforeUsedMem;
		
		return (actualMemUsed > LIMIT);
	}

}
