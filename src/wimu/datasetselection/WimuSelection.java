package org.wimu.datasetselection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.tukaani.xz.XZInputStream;

import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;


public class WimuSelection {

	public static Map<String, String> mUriDataset = new HashMap<String, String>();

	public static void main(String[] args) throws InterruptedException, IOException {
		// mUriDataset.putAll(loadTSV("URI_Dataset.tsv"));
		long start = System.currentTimeMillis();
		String cSparql = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT DISTINCT ?i "
				+ "WHERE { <http://www.w3.org/People/Berners-Lee/card#i> " + "foaf:knows ?p . ?p foaf:interest ?i .}";
		System.out.println(cSparql);
		boolean bSearchURIPrefix = false;
		boolean bFirstDataset = false;
		System.out.println("Search in URI prefix: " + bSearchURIPrefix);
		Map<String, String> mUDataset = getDatasets(cSparql, bSearchURIPrefix, bFirstDataset);
		long total = System.currentTimeMillis() - start;
		System.out.println("List of URI -> Dataset:");
		mUDataset.forEach((uri, dataset) -> {
			System.out.println("uri: " + uri);
			System.out.println("dataset: " + dataset);
		});
		System.out.println("TotalTime getDatasets: " + total + "ms");
		start = System.currentTimeMillis();
		Map<String, Long> bestDataset = getBestDataset(mUDataset, cSparql);
		total = System.currentTimeMillis() - start;
		System.out.println("Best dataset: " + bestDataset);
		System.out.println("TotalTime getBestDataset: " + total + "ms");
		// String bestDataset2 = getBestDataset(cSparql);
	}

	/*
	 * The best dataset is the dataset that returns more results.
	 */
	private static Map<String, Long> getBestDataset(Map<String, String> pMUriDataset, String cSparql)
			throws IOException {
		Map<String, Long> ret = new HashMap<String, Long>();
		long prevRes = 0;
		String dataset = null;
		for (String ds : pMUriDataset.values()) {
			long totalResults = execQuery(cSparql, ds);
			if (totalResults > prevRes) {
				prevRes = totalResults;
				System.out.println("results: " + prevRes);
				dataset = ds;
			}
		}
		ret.put(dataset, prevRes);
		return ret;
	}

	private static long execQuery(String cSparql, String dataset) throws IOException {
		long ret = 0;
		if (dataset.endsWith("hdt")) {
			ret = execQueryHDT(cSparql, dataset);
		} else {
			ret = execQueryRDF(cSparql, dataset);
		}
		return ret;
	}

	private static org.apache.jena.query.ResultSet execQueryRes(String cSparql, String dataset) throws IOException {
		org.apache.jena.query.ResultSet ret = null;
		if (dataset == null)
			return ret;

		if (dataset.endsWith("hdt")) {
			ret = execQueryHDTRes(cSparql, dataset);
		} else {
			ret = execQueryRDFRes(cSparql, dataset);
		}
		return ret;
	}

	private static org.apache.jena.query.ResultSet execQueryRDFRes(String cSparql, String dataset) {
		org.apache.jena.query.ResultSet ret = null;
		File file = null;
		try {
			long start = System.currentTimeMillis();
			URL url = new URL(dataset);
			file = new File(getURLFileName(url));
			if (!file.exists()) {
				FileUtils.copyURLToFile(url, file);
			}
			long limSize = 5000000; // 5 MB
			if(file.length() > limSize){
				System.out.println("File: "+file.getAbsolutePath()+" is bigger than " + limSize + " bytes");
				return null;
			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			if (file.getName().endsWith("hdt")) {
				return execQueryHDTRes(cSparql, file);
			}

			start = System.currentTimeMillis();
			org.apache.jena.rdf.model.Model model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();

			org.apache.jena.sparql.engine.QueryExecutionBase qe = null;
			org.apache.jena.query.ResultSet resultSet = null;
			/* First check the IRI file extension */
			if (file.getName().toLowerCase().endsWith(".ntriples") || file.getName().toLowerCase().endsWith(".nt")) {
				System.out.println("# Reading a N-Triples file...");
				model.read(file.getAbsolutePath(), "N-TRIPLE");
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".n3")) {
				System.out.println("# Reading a Notation3 (N3) file...");
				model.read(file.getAbsolutePath());
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".jsod")
					|| file.getName().toLowerCase().endsWith(".jsonld")) {
				System.out.println("# Trying to read a 'json-ld' file...");
				model.read(file.getAbsolutePath(), "JSON-LD");
				qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else {
				String contentType = getContentType(dataset); // get the IRI
																// content type
				System.out.println("# IRI Content Type: " + contentType);
				if (contentType.contains("application/ld+json") || contentType.contains("application/json")
						|| contentType.contains("application/json+ld")) {
					System.out.println("# Trying to read a 'json-ld' file...");
					model.read(file.getAbsolutePath(), "JSON-LD");
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("application/n-triples")) {
					System.out.println("# Reading a N-Triples file...");
					model.read(file.getAbsolutePath(), "N-TRIPLE");
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("text/n3")) {
					System.out.println("# Reading a Notation3 (N3) file...");
					model.read(file.getAbsolutePath());
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else {
					model.read(file.getAbsolutePath());
					qe = (org.apache.jena.sparql.engine.QueryExecutionBase) org.apache.jena.query.QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				}
			}
			if (resultSet != null) {
				ret = org.apache.jena.query.ResultSetFactory.copyResults(resultSet);
			}
			total = System.currentTimeMillis() - start;
			System.out.println("Time to query dataset: " + total + "ms");
			// file.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	private static long execQueryRDF(String cSparql, String dataset) {
		long ret = 0;
		File file = null;
		try {
			long start = System.currentTimeMillis();
			URL url = new URL(dataset);
			file = new File(getURLFileName(url));
			if (!file.exists()) {
				FileUtils.copyURLToFile(url, file);
			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			if (file.getName().endsWith("hdt")) {
				return execQueryHDT(cSparql, file);
			}

			start = System.currentTimeMillis();
			Model model = ModelFactory.createDefaultModel();

			QueryExecutionBase qe = null;
			ResultSet resultSet = null;
			/* First check the IRI file extension */
			if (file.getName().toLowerCase().endsWith(".ntriples") || file.getName().toLowerCase().endsWith(".nt")) {
				System.out.println("# Reading a N-Triples file...");
				model.read(file.getAbsolutePath(), "N-TRIPLE");
				qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".n3")) {
				System.out.println("# Reading a Notation3 (N3) file...");
				model.read(file.getAbsolutePath());
				qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".jsod")
					|| file.getName().toLowerCase().endsWith(".jsonld")) {
				System.out.println("# Trying to read a 'json-ld' file...");
				model.read(file.getAbsolutePath(), "JSON-LD");
				qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
				resultSet = qe.execSelect();
			} else {
				String contentType = getContentType(dataset); // get the IRI
																// content type
				System.out.println("# IRI Content Type: " + contentType);
				// if (contentType.contains("text/html") ||
				// contentType.contains("application/xhtml+xml")) {
				// System.out.println("# Checking if the URI contains 'RDFa'
				// data...");
				// JenaRdfaReader.inject();
				// model.read(iri, "RDFA");
				// qe = (QueryExecutionBase) QueryExecutionFactory.create(query,
				// model);
				// resultSet = qe.execSelect();
				// } else
				if (contentType.contains("application/ld+json") || contentType.contains("application/json")
						|| contentType.contains("application/json+ld")) {
					System.out.println("# Trying to read a 'json-ld' file...");
					model.read(file.getAbsolutePath(), "JSON-LD");
					qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("application/n-triples")) {
					System.out.println("# Reading a N-Triples file...");
					model.read(file.getAbsolutePath(), "N-TRIPLE");
					qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else if (contentType.contains("text/n3")) {
					System.out.println("# Reading a Notation3 (N3) file...");
					model.read(file.getAbsolutePath());
					qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				} else {
					model.read(file.getAbsolutePath());
					qe = (QueryExecutionBase) QueryExecutionFactory.create(cSparql, model);
					resultSet = qe.execSelect();
				}
			}
			if (resultSet != null) {
				ret = resultSet.getResourceModel().size();
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

	public static String getURLFileName(URL pURL) {
		String name = null;
		try {
			String[] str = pURL.getFile().split("/");
			name = str[str.length - 1];
			name = name.replaceAll("=", ".");
		} catch (Exception e) {
			System.err.println("Problem with URL: " + pURL);
		}
		return name;
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

	private static long execQueryHDT(String cSparql, File dataset) throws IOException {
		long ret = 0;
//		HDT hdt = null;
//		try {
//			System.out.println("Dataset: " + dataset.getAbsolutePath());
//			long start = System.currentTimeMillis();
//			hdt = HDTManager.mapHDT(dataset.getAbsolutePath(), null);
//			HDTGraph graph = new HDTGraph(hdt);
//			Model model = new ModelCom(graph);
//			Query query = QueryFactory.create(cSparql);
//
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//			ResultSet results = qe.execSelect();
//			ResultSetFormatter.out(System.out, results, query);
//			ret = results.getResourceModel().size();
//			long total = System.currentTimeMillis() - start;
//			System.out.println("Time to query dataset: " + total + "ms");
//			qe.close();
//		} catch (Exception e) {
//			System.out.println("FAIL: " + dataset + " Error: " + e.getMessage());
//		} finally {
//			// dataset.delete();
//			if (hdt != null) {
//				hdt.close();
//			}
//		}

		return ret;
	}

	private static long execQueryHDT(String cSparql, String dataset) throws IOException {
		long ret = 0;
//		File file = null;
//		HDT hdt = null;
//		try {
//			System.out.println("Dataset: " + dataset);
//			long start = System.currentTimeMillis();
//			URL url = new URL(dataset);
//			file = new File(getURLFileName(url));
//			if (!file.exists()) {
//				FileUtils.copyURLToFile(url, file);
//			}
//			long total = System.currentTimeMillis() - start;
//			System.out.println("Time to download dataset: " + total + "ms");
//			start = System.currentTimeMillis();
//			hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
//			HDTGraph graph = new HDTGraph(hdt);
//			Model model = new ModelCom(graph);
//			Query query = QueryFactory.create(cSparql);
//
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//			ResultSet results = qe.execSelect();
//			ResultSetFormatter.out(System.out, results, query);
//			ret = results.getResourceModel().size();
//			total = System.currentTimeMillis() - start;
//			System.out.println("Time to query dataset: " + total + "ms");
//			qe.close();
//		} catch (Exception e) {
//			System.out.println("FAIL: " + dataset + " Error: " + e.getMessage());
//		} finally {
//			// file.delete();
//			if (hdt != null) {
//				hdt.close();
//			}
//		}

		return ret;
	}

	private static org.apache.jena.query.ResultSet execQueryHDTRes(String cSparql, File file) throws IOException {
		org.apache.jena.query.ResultSet ret = null;
//		HDT hdt = null;
//		try {
//			System.out.println("Dataset: " + file.getAbsolutePath());
//			long start = System.currentTimeMillis();
//			hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
//			long total = System.currentTimeMillis() - start;
//			start = System.currentTimeMillis();
//			// hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
//			HDTGraph graph = new HDTGraph(hdt);
//			Model model = new ModelCom(graph);
//			Query query = QueryFactory.create(cSparql);
//
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//			ResultSet results = qe.execSelect();
//			// System.out.println(results.getResourceModel().size());
//			ret = ResultSetFactory.copyResults(results);
//			total = System.currentTimeMillis() - start;
//			System.out.println("Time to query dataset: " + total + "ms");
//			qe.close();
//		} catch (Exception e) {
//			System.out.println("FAIL: " + file.getAbsolutePath() + " Error: " + e.getMessage());
//		} finally {
//			// file.delete();
//			if (hdt != null) {
//				hdt.close();
//			}
//		}

		return ret;
	}

	public static org.apache.jena.query.ResultSet execQueryHDTRes(String cSparql, String dataset) throws IOException {
		org.apache.jena.query.ResultSet ret = null;
		File file = null;
		HDT hdt = null;
		try {
			System.out.println("Dataset: " + dataset);
			long start = System.currentTimeMillis();
			URL url = new URL(dataset);
			file = new File(getURLFileName(url));
			if (!file.exists()) {
				FileUtils.copyURLToFile(url, file);
			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			start = System.currentTimeMillis();
			hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
			org.rdfhdt.hdtjena.HDTGraph graph = new HDTGraph(hdt);
			org.apache.jena.rdf.model.Model model = new org.apache.jena.rdf.model.impl.ModelCom(graph);
			org.apache.jena.query.Query query = org.apache.jena.query.QueryFactory.create(cSparql);

			org.apache.jena.query.QueryExecution qe = org.apache.jena.query.QueryExecutionFactory.create(query, model);
			org.apache.jena.query.ResultSet results = qe.execSelect();
			System.out.println(results.getResourceModel().size());
			ret = org.apache.jena.query.ResultSetFactory.copyResults(results);
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

	/*
	 * @includePrefix URI Lookup also with the prefix.
	 */
	private static Map<String, String> getDatasets(String cSparql, boolean includePrefix, boolean firstDataset)
			throws InterruptedException, IOException {
		Map<String, String> ret = new HashMap<String, String>();

		String[] uris = null;
		if (includePrefix) {
			uris = cSparql.split("<");
		} else {
			uris = cSparql.substring(cSparql.toLowerCase().indexOf("select")).split("<");
		}

		String dsWIMU = null;
		for (String uri : uris) {
			if (uri.startsWith("http")) {
				uri = uri.substring(0, uri.indexOf(">"));
				dsWIMU = getDsWIMU(uri);
				if ((dsWIMU != null) && (firstDataset)) {
					ret.put(uri, dsWIMU);
					break;
				}
				ret.put(uri, dsWIMU);
			}
		}
		return ret;
	}

	private static String getDsWIMU(String uri) throws InterruptedException, IOException {
		String sRet = null;

		if (mUriDataset.get(uri) != null) {
			return mUriDataset.get(uri);
		}

		URL urlSearch = new URL("http://139.18.8.58:8080/LinkLion2_WServ/Find?uri=" + uri);
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(urlSearch.openStream());
		} catch (Exception e) {
			Thread.sleep(5000);
			reader = new InputStreamReader(urlSearch.openStream());
		}
		try {
			WIMUDataset[] wData = new Gson().fromJson(reader, WIMUDataset[].class);
			for (WIMUDataset wDs : wData) {
				sRet = wDs.getDataset();
				if (sRet.length() < 1) {
					sRet = wDs.getHdt();
				}
				break;
			}
		} catch (Exception e) {
			System.err.println("No dataset for the URI: " + uri);
		}

		return sRet;
	}

	private static Map<String, String> loadTSV(String fName) {
		long start = System.currentTimeMillis();
		System.out.println("Loading URI -> Dataset mapping...");
		Map<String, String> ret = new HashMap<String, String>();
		try {
			List<String> lstLines = FileUtils.readLines(new File(fName), "UTF-8");
			for (String line : lstLines) {
				String s[] = line.split("\t");
				if (s.length < 2)
					continue;
				String uri = s[0];
				String dataset = s[1];
				ret.put(uri, dataset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long totalTime = System.currentTimeMillis() - start;
		System.out.println("TotalTime load URI_Dataset: " + totalTime);
		return ret;
	}

	public static WimuResult execQuery(String cSparql, boolean onlyDatasets) throws Exception {
		WimuResult ret = new WimuResult();
		long size = 0;
		long prevRes = 0;
		String dataset = "";
		org.apache.jena.query.ResultSet res = null;
		boolean includePrefixURILookup = true;
		boolean justFirstDataset = false;
		Map<String, String> mUDataset = getDatasets(cSparql, includePrefixURILookup, justFirstDataset);
		ret.setDatasets(mUDataset);
		if(onlyDatasets){
			return ret;
		}
		// mUDataset.forEach((uri,ds) -> {
		for (String ds : mUDataset.values()) {
			try {
				ret.setBestDataset(ds);
				size++;
				// size = execQuery(cSparql, ds);
				if((ds != null) && ds.contains("https://hdt.lod.labs.vu.nl")){
					ret.setSize(execQueryLODAlot(cSparql, ds));
					continue;
				}
				res = execQueryRes(cSparql, ds);
				if (res == null)
					continue;
				if(res.hasNext()){
					break;
				}
//				while (res.hasNext()) {
//					size++;
//					if (size > prevRes) {
//						prevRes = size;
//						dataset = ds;
//						break;
//					}
//				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// });
		}
		ret.setSize(size);
		//ret.setBestDataset(dataset);
		ret.setResult(res);
		return ret;
	}

	private static long execQueryLODAlot(String cSparql, String dataset) {
		System.out.println("Need to implement Sparql on: " + dataset);
		System.out.println("Query to LOD-a-lot: " + cSparql);
		return 2;
	}

}
