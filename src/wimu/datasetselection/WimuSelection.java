package org.wimu.datasetselection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.tukaani.xz.XZInputStream;

public class WimuSelection {

	public static String execQueryRes(String cSparql, String dataset) throws IOException {
		String ret = null;
		if (dataset == null)
			return ret;

		if (dataset.endsWith("hdt")) {
			ret = execQueryHDTRes(cSparql, dataset);
		} else {
			ret = execQueryRDFRes(cSparql, dataset);
		}
		return ret;
	}

	private static String execQueryRDFRes(String cSparql, String dataset) {
		String ret = null;
		File file = null;
		try {
			long start = System.currentTimeMillis();
			URL url = new URL(dataset);
			file = new File(Util.getURLFileName(url));
			if (!file.exists()) {
				FileUtils.copyURLToFile(url, file);
			}
			long limSize = 5000000; // 5 MB
			if (file.length() > limSize) {
				System.err.println("File: " + file.getAbsolutePath() + " is bigger than " + limSize + " bytes");
				return null;
			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			if (file.getName().endsWith("hdt")) {
				return execQueryHDTRes(cSparql, file.getAbsolutePath());
			}

			start = System.currentTimeMillis();
			org.apache.jena.rdf.model.Model model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();

			org.apache.jena.sparql.engine.QueryExecutionBase qe = null;
			org.apache.jena.query.ResultSet resultSet = null;
			/* First check the IRI file extension */
			if (file.getName().toLowerCase().endsWith(".ntriples") || file.getName().toLowerCase().endsWith(".nt")) {
				System.out.println("# Reading a N-Triples file...");
				model.read(file.getAbsolutePath(), "N-TRIPLE");
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
						.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".n3")) {
				System.out.println("# Reading a Notation3 (N3) file...");
				model.read(file.getAbsolutePath());
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
						.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".jsod")
					|| file.getName().toLowerCase().endsWith(".jsonld")) {
				System.out.println("# Trying to read a 'json-ld' file...");
				model.read(file.getAbsolutePath(), "JSON-LD");
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
						.create(cSparql, model);
				resultSet = qe.execSelect();
			} else {
				String contentType = getContentType(dataset); // get the IRI
																// content type
				System.out.println("# IRI Content Type: " + contentType);
				if (contentType.contains("application/ld+json") || contentType.contains("application/json")
						|| contentType.contains("application/json+ld")) {
					System.out.println("# Trying to read a 'json-ld' file...");
					model.read(file.getAbsolutePath(), "JSON-LD");
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
							.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("application/n-triples")) {
					System.out.println("# Reading a N-Triples file...");
					model.read(file.getAbsolutePath(), "N-TRIPLE");
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
							.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("text/n3")) {
					System.out.println("# Reading a Notation3 (N3) file...");
					model.read(file.getAbsolutePath());
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
							.create(cSparql, model);
					resultSet = qe.execSelect();
				} else {
					model.read(file.getAbsolutePath());
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory
							.create(cSparql, model);
					resultSet = qe.execSelect();
				}
			}
			if (resultSet != null) {
				ret = ResultSetFormatter.asText(resultSet);
				//ret = org.apache.jena.query.ResultSetFactory.copyResults(resultSet);
			}
			total = System.currentTimeMillis() - start;
			System.out.println("Time to query dataset: " + total + "ms");
			// file.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	/**
	 * Read the IRI content type by opening an HTTP connection. We set the value
	 * 'application/rdf+xml' to the ACCEPT request property for handling
	 * dereferenceable IRIs.
	 */
	public static String getContentType(String iri) {
		String contentType = "";
		try {
			URL url = new URL(iri);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			connection.setRequestProperty("ACCEPT", "application/rdf+xml");
			connection.connect();
			contentType = connection.getContentType();
			if (contentType == null) {
				contentType = "";
			}
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contentType;
	}

	private static File unconpress(File file) {
		File ret = file;
		try {
			File fUnzip = null;
			if (file.getName().endsWith(".bz2"))
				fUnzip = new File(file.getName().replaceAll(".bz2", ""));
			else if (file.getName().endsWith(".xz"))
				fUnzip = new File(file.getName().replaceAll(".xz", ""));
			else if (file.getName().endsWith(".zip"))
				fUnzip = new File(file.getName().replaceAll(".zip", ""));
			else if (file.getName().endsWith(".tar.gz"))
				fUnzip = new File(file.getName().replaceAll(".tar.gz", ""));
			else if (file.getName().endsWith(".gz"))
				fUnzip = new File(file.getName().replaceAll(".gz", ""));
			else
				return file;

			if (fUnzip.exists()) {
				return fUnzip;
			}
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			FileOutputStream out = new FileOutputStream(fUnzip);

			if (file.getName().endsWith(".bz2")) {
				BZip2CompressorInputStream bz2In = new BZip2CompressorInputStream(in);
				synchronized (bz2In) {
					final byte[] buffer = new byte[8192];
					int n = 0;
					while (-1 != (n = bz2In.read(buffer))) {
						out.write(buffer, 0, n);
					}
					out.close();
					bz2In.close();
				}
			} else if (file.getName().endsWith(".xz")) {
				XZInputStream xzIn = new XZInputStream(in);
				synchronized (xzIn) {
					final byte[] buffer = new byte[8192];
					int n = 0;
					while (-1 != (n = xzIn.read(buffer))) {
						out.write(buffer, 0, n);
					}
					out.close();
					xzIn.close();
				}
			} else if (file.getName().endsWith(".zip")) {
				ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in);
				synchronized (zipIn) {
					final byte[] buffer = new byte[8192];
					int n = 0;
					while (-1 != (n = zipIn.read(buffer))) {
						out.write(buffer, 0, n);
					}
					out.close();
					zipIn.close();
				}
			} else if (file.getName().endsWith(".tar.gz") || file.getName().endsWith(".gz")) {
				GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
				synchronized (gzIn) {
					final byte[] buffer = new byte[8192];
					int n = 0;
					while (-1 != (n = gzIn.read(buffer))) {
						out.write(buffer, 0, n);
					}
					out.close();
					gzIn.close();
				}
			}

			// file.delete();

			if (fUnzip != null)
				ret = fUnzip;
		} catch (Exception ex) {
			ret = file;
		}
		return ret;
	}

	public static String execQueryHDTRes(String cSparql, String dataset) throws IOException {
		String ret = null;
		File file = null;
		HDT hdt = null;
		try {
			System.out.println("Dataset: " + dataset);
			long start = System.currentTimeMillis();
			if(dataset.startsWith("http")){
				URL url = new URL(dataset);
				file = new File(Util.getURLFileName(url));
				if (!file.exists()) {
					FileUtils.copyURLToFile(url, file);
				}
			} else{
				file = new File(dataset);
			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			start = System.currentTimeMillis();
			hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
			HDTGraph graph = new HDTGraph(hdt);
			Model model = new ModelCom(graph);
			Query query = QueryFactory.create(cSparql);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			ret = ResultSetFormatter.asText(results);
//			System.out.println(results.getResourceModel().size());
//			ret = ResultSetFactory.copyResults(results);
			total = System.currentTimeMillis() - start;
			System.out.println("Time to query dataset: " + total + "ms");
			qe.close();
		} catch (Exception e) {
			System.out.println("FAIL: " + dataset + " Error: " + e.getMessage());
		} finally {
			// file.delete();
			if (hdt != null) {
				hdt.close();
			}
		}

		return ret;
	}

	public static WimuResult execQuery(String cSparql, boolean onlyDatasets) throws Exception {
		WimuResult ret = new WimuResult();
		long size = 0;
		org.apache.jena.query.ResultSet res = null;
		Map<String, String> mUDataset = Util.getDatasets(cSparql);
		ret.setDatasets(mUDataset);
		if (onlyDatasets) {
			return ret;
		}

		String results = "";
		for (String ds : mUDataset.values()) {
			try {
				ret.setBestDataset(ds);
				size++;
				if ((ds != null) && ds.contains("https://hdt.lod.labs.vu.nl")) {
					ret.setSize(execQueryLODAlot(cSparql, ds));
					continue;
				}
				results += execQueryRes(cSparql, ds);
//				if (res == null)
//					continue;
//				if (res.hasNext()) {
//					break;
//				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// });
		}
		ret.setSize(size);
		// ret.setBestDataset(dataset);
		ret.setResult(results);
		return ret;
	}

	private static long execQueryLODAlot(String cSparql, String dataset) {
		//System.out.println("Need to implement Sparql on: " + dataset);
		//System.out.println("Query to LOD-a-lot: " + cSparql);
		boolean onlyCheck = true;
		boolean ret = true;
		try {
			ret = QueryLODaLot.execQuery(cSparql, onlyCheck);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(ret)
			return 2;
		else
			return 0;
	}

	public static String executeQuery(String query, String dataset) {
		String ret = null;
		if (dataset == null)
			return ret;

		try {
			if (dataset.endsWith("hdt")) {
				ret = execQueryHDTRes(query, dataset);
			} else {
				ret = execQueryRDFRes(query, dataset);
			}
//			if (res == null){
//				return ret;
//			}
//			while (res.hasNext()) {
//				QuerySolution triple = res.next();
//				Iterator<String> itVars = triple.varNames();
//				String sTriple = "";
//				while(itVars.hasNext()){
//					String vName = itVars.next();
//					sTriple += "<("+vName+")" +triple.get(vName) + "> ";
//				}
//				ret.add(sTriple + ".");
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
