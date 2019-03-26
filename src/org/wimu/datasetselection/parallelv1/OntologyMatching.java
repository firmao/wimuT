package org.wimu.datasetselection.parallelv1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class OntologyMatching {

	public static void main(String[] args) throws IOException {
		Set<String> datasets = new LinkedHashSet<String>();
		datasets.add("85d5a476b56fde200e770cefa0e5033c?type.hdt");
		datasets.add("b7081efa178bc4ab3ff3a6ef5abac9b2?type.hdt");
		datasets.add("c66ff6bbdb8eeac9c17adbe7dfe4efd5?type.hdt");
		File f = generateMatchFile(datasets);
		if (f.exists()) {
			System.out.println("File generated: " + f.getAbsolutePath());
		}
	}

	public static File generateMatchFile(Set<String> datasets) throws IOException {
		File fRet = null;
		Set<String> owlFiles = new LinkedHashSet<String>();
		for (String source : datasets) {
			if (Util.isEndPoint(source)) {
				owlFiles.add(extractOntoEndPoint(source));
				// mRet.putAll(Util.execQueryEndPointMap(cSparql, source));
			} else {
				owlFiles.add(extractOntoRDFFile(source));
				// mRet.putAll(Util.execQueryRDFRes(cSparql, source));
			}
		}
		fRet = generateMatch(owlFiles, "fileMatch.txt");
		return fRet;
	}

	private static File generateMatch(Set<String> owlFiles, String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		for (String owl : owlFiles) {
			writer.println(owl);
		}
		writer.close();
		
		return new File(fileName);
	}

	private static String extractOntoEndPoint(String source) {
		System.err.println("extractOntoEndPoint - NEED TO IMPLEMENT");
		return null;
	}

	private static String extractOntoRDFFile(String source) throws IOException {
		String ret = null;
		if (source.endsWith("hdt")) {
			File f = extractNTFile(source);
			ret = extractOWLFile(f);
		}
		return ret;
	}

	private static File extractNTFile(String source) throws IOException {
		File hdtFile = new File(source);
		String sRet = hdtFile.getName().substring(0, hdtFile.getName().indexOf("?"));
		if(sRet.contains(".")) {
			sRet = sRet.substring(0, sRet.indexOf("."));
		}
		HDT hdt = HDTManager.mapHDT(source, null);
		HDTGraph graph = new HDTGraph(hdt);
		Model model = new ModelCom(graph);
		PrintWriter writer = new PrintWriter(sRet, "UTF-8");
		model.write(writer, "N-TRIPLES");
		// model.write(System.out);
		writer.close();
		hdt.close();
		return new File(sRet);
	}

	private static String extractOWLFile(File f) {
		String fileRet = f.getName() + ".owl";
		try {
//			String[] commands = { "rapper", "-i", "ntriples", "-o", "rdfxml", f.getAbsolutePath(), ">", fileRet };
			String sFileShell = generateShell("rapper -i ntriples -o rdfxml "+f.getAbsolutePath()+" > "+fileRet, "executeRapper.sh");
			String[] commands = { "./" + sFileShell };
			Runtime runtime = Runtime.getRuntime();
			// String[] commands = {"free", "-h"};
			Process process = runtime.exec(commands);

			BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			lineReader.lines().forEach(System.out::println);

			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			errorReader.lines().forEach(System.out::println);
			
//			String commandLine = "rapper -i ntriples -o rdfxml "+f.getAbsolutePath()+" > "+fileRet;
//			Process p = Runtime.getRuntime().exec(commandLine);
//			p.waitFor(); 
//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = "";
//			while ((line = reader.readLine())!= null) 
//			{
//				System.out.println(line + "\n");
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileRet;
	}

	private static String generateShell(String sCommand, String sFile) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(sFile, "UTF-8");
		writer.println(sCommand);
		writer.close();
		return sFile;
	}

}
