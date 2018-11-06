package org.wimu.datasetselection.cbd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.tukaani.xz.XZInputStream;

public class WimuSelection {

	public static Set<String> getCBDWIMUDump(String cSparql, String dataset) throws IOException {
		Set<String> ret = new HashSet<String>();
		if (dataset == null)
			return ret;

		if (dataset.endsWith("hdt")) {
			ret = execQueryHDTRes(cSparql, dataset);
		} else {
			ret = execQueryRDFRes(cSparql, dataset);
		}
		return ret;
	}

	private static Set<String> execQueryRDFRes(String cSparql, String dataset) {
		Set<String> ret = new HashSet<String>();
		File file = null;
		try {
			long start = System.currentTimeMillis();

			Set<String> uris = Util.extractUrls(cSparql);

			URL url = new URL(dataset);
			file = new File(Util.getURLFileName(url));
			if (!file.exists()) {
				try {
					FileUtils.copyURLToFile(url, file);
				} catch (Exception e) {
					return ret;
				}
			}
//			long limSize = 5000000; // 5 MB
//			if (file.length() > limSize) {
//				System.err.println("File: " + file.getAbsolutePath() + " is bigger than " + limSize + " bytes");
//				for (String uri : uris) {
//					ret.addAll(Util.getCBD_LOD_a_lot(uri));
//				}
//				return ret;
//			}
			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			if (file.getName().endsWith("hdt")) {
				return execQueryHDTRes(cSparql, file.getAbsolutePath());
			}

			for (String uri : uris) {
				ret.addAll(Util.getCBD_RDF(uri, dataset, file));
			}
			return ret;
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

	public static Set<String> execQueryHDTRes(String cSparql, String dataset) throws IOException {
		Set<String> ret = new HashSet<String>();
		File file = null;
		HDT hdt = null;
		try {
			Set<String> uris = Util.extractUrls(cSparql);

			System.out.println("Dataset: " + dataset);
			long start = System.currentTimeMillis();
			if (dataset.startsWith("http")) {
				URL url = new URL(dataset);
				file = new File(Util.getURLFileName(url));
				if (!file.exists()) {
					try {
						FileUtils.copyURLToFile(url, file);
					} catch (Exception e) {
						return ret;
					}
				}
			} else {
				file = new File(dataset);
			}
//			long limSize = 5000000; // 5 MB
//			if (file.length() > limSize) {
//				System.err.println("File: " + file.getAbsolutePath() + " is bigger than " + limSize + " bytes");
//				for (String uri : uris) {
//					ret.addAll(Util.getCBD_LOD_a_lot(uri));
//				}
//				return ret;
//			}

			file = unconpress(file);
			long total = System.currentTimeMillis() - start;
			System.out.println("Time to download dataset: " + total + "ms");
			start = System.currentTimeMillis();
			hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
			HDTGraph graph = new HDTGraph(hdt);
			Model model = new ModelCom(graph);

			for (String uri : uris) {
				String sparqlCBD = "Select * where {?s ?p ?o . filter(?s=<" + uri + "> || ?o=<" + uri + ">) }";
				Query query = QueryFactory.create(sparqlCBD);

				QueryExecution qe = QueryExecutionFactory.create(query, model);
				ResultSet results = qe.execSelect();

				while (results.hasNext()) {
					QuerySolution thisRow = results.next();
					String nTriple = Util.toNTNotation(thisRow.get("s"), thisRow.get("p"), thisRow.get("o"));
					ret.add(nTriple);
				}

				qe.close();
			}
			total = System.currentTimeMillis() - start;
			System.out.println("Time to query dataset: " + total + "ms");

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

	public static WimuResult execQuery(String cSparql, boolean b, long timeout)
			throws InterruptedException, IOException {
		long spliTimeout = timeout / 3;
		WimuResult ret = new WimuResult();
		long size = 0;

		Util.updateCount(Approach.SPARQL_A_LOT, 0);
		Util.updateCount(Approach.ENDPOINT, 0);
		Util.updateCount(Approach.WIMU_DUMP, 0);
		Map<String, Set<String>> mUDataset = Util.getDatasetsTotal(cSparql);
		ret.setDatasets(mUDataset);

		final StringBuffer results = new StringBuffer();
		final Set<String> cbdRes = new HashSet<String>();
		for (String uri : mUDataset.keySet()) {
			for (String ds : mUDataset.get(uri)) {
				ret.setBestDataset(ds);
				size++;
				if ((ds != null) && ds.contains("dbpedia")) {

					try {
						TimeOutBlock timeoutBlock = new TimeOutBlock(spliTimeout);
						Runnable block = new Runnable() {
							@Override
							public void run() {
								String sDBpedia = Util.execQueryEndPoint(cSparql, "http://dbpedia.org/sparql");
								if (sDBpedia.contains("http")) {
									ret.setResultDBpedia(true);
									results.append(sDBpedia);
								}
							}
						};
						timeoutBlock.addBlock(block);// execute the runnable block
					} catch (Throwable e) {
						System.out.println("TIME-OUT-ERROR(execQueryEndPoint): " + e.getMessage());
						// continue;
					}
					// continue;
				}
				if ((ds != null) && ds.contains("https://hdt.lod.labs.vu.nl")) {

					try {
						TimeOutBlock timeoutBlock = new TimeOutBlock(spliTimeout);
						Runnable block = new Runnable() {
							@Override
							public void run() {
								// ret.setSize(execQueryLODAlot(cSparql, ds));
								try {
									results.append(QueryLODaLot.execQuery(cSparql));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
						timeoutBlock.addBlock(block);// execute the runnable block
					} catch (Throwable e) {
						System.out.println("TIME-OUT-ERROR(execQueryEndPoint): " + e.getMessage());
						// continue;
					}

					// continue;
				}

				try {
					TimeOutBlock timeoutBlock = new TimeOutBlock(spliTimeout);
					Runnable block = new Runnable() {
						@Override
						public void run() {
							try {
								// results.append(execQueryRes(cSparql, ds));
								cbdRes.addAll(getCBDWIMUDump(cSparql, ds));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					timeoutBlock.addBlock(block);// execute the runnable block
				} catch (Throwable e) {
					System.out.println("TIME-OUT-ERROR(execQueryEndPoint): " + e.getMessage());
					// continue;
				}
			}
		}

		Set<String> retCBDWimu = Util.executeQueryJena(cSparql, cbdRes);
		Util.updateCount(Approach.WIMU_DUMP, retCBDWimu.size());

		ret.setSize(size);
		// if (!results.toString().contains("http")) {
		final Set<String> resLodALot = new HashSet<String>();

		try {
			TimeOutBlock timeoutBlock = new TimeOutBlock(spliTimeout);
			Runnable block = new Runnable() {
				@Override
				public void run() {
					try {
						resLodALot.addAll(QueryLODaLot.execQuery(cSparql));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			timeoutBlock.addBlock(block);// execute the runnable block
		} catch (Throwable e) {
			System.out.println("TIME-OUT-ERROR(execQueryLODaLOT): " + e.getMessage());
		}

		if (resLodALot.size() > 0) {
			results.delete(0, results.length());
			ret.setResultLODaLOT(true);
			for (String triple : resLodALot) {
				results.append(triple + "\n");
			}
		}
		// }

		ret.setResult(results.toString());
		return ret;
	}

}
