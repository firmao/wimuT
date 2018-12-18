package org.wimu.datasetselection.parallelv1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Frank {

	public static void main(String[] args) throws IOException {
		//second();
		 String[] commands = {"./frank","statements"
		 ,"-p","http://dbpedia.org/ontology/nationality"
		 ,"-o","http://dbpedia.org/resource/United_States"};
		//String[] commands = { "./testFrank.sh" };

		Runtime runtime = Runtime.getRuntime();
		// String[] commands = {"free", "-h"};
		Process process = runtime.exec(commands);

		BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		lineReader.lines().forEach(System.out::println);

		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		errorReader.lines().forEach(System.out::println);
	}

	public static void second() {
		try { 		
			String[] commands = {"./frank","statements"
					 ,"-p","http://dbpedia.org/ontology/nationality"
					 ,"-o","http://dbpedia.org/resource/United_States"};
			//Process	p = Runtime.getRuntime().exec("/media/andre/DATA/python/anaconda/bin/curl http://wimu.aksw.org"); 			
			Process	p = Runtime.getRuntime().exec(commands);
			p.waitFor(); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine())!= null) 
			{
				System.out.println(line + "\n");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
