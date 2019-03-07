package org.wimu.datasetselection.parallelv1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.util.CharArrayMap.EntrySet;

public class ClusterKmeans {
	public static final Map<String, String> mDatasetCode = new HashMap<String, String>();
	public static final Map<String, String> mPropertyCode = new HashMap<String, String>();
	public static final Map<String, Integer> mPropOccur = new HashMap<String, Integer>();
	private static final Map<String, Map<String, Integer>> mDsPropOccur = new HashMap<String, Map<String, Integer>>();

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		Set<String> datasets = new HashSet<String>();
		// datasets.add("http://dbpedia.org/sparql");
		// datasets.add("http://lod2.openlinksw.com/sparql");
		// datasets.add("https://query.wikidata.org/");
		// datasets.add("http://download.lodlaundromat.org/85d5a476b56fde200e770cefa0e5033c?type=hdt");
		datasets.add("85d5a476b56fde200e770cefa0e5033c?type.hdt");
		datasets.add("b7081efa178bc4ab3ff3a6ef5abac9b2?type.hdt");
		datasets.add("c66ff6bbdb8eeac9c17adbe7dfe4efd5?type.hdt");
		// boolean sparceMatrix = true;
		generateFileKMeans(datasets);
		long total = System.currentTimeMillis() - start;
		System.out.println("FINISHED in " + TimeUnit.MILLISECONDS.toMinutes(total) + " minutes");
	}

	private static void generateFileKMeans(Set<String> datasets) throws IOException {
		String fileName = "dense_ClusterKMeans.tsv";
		String dataset_code_file = "Dataset_code.tsv";
		String prop_code_file = "Property_code.tsv";
		String prop_occur_file = "sparse_ClusterKMeans.tsv";
		String sep = "\t";

		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		final String cSparql = "Select ?p (count(?p) as ?qtd) where {\n" + "?s ?p ?o .\n" + "}\n" + "group by ?p";
//		final String cSparql = "Select ?p (count(?p) as ?qtd) where {\n" + "?s ?p ?o .\n"
//				+ "filter(?p=<http://www.aktors.org/ontology/extension#has-authority> || "
//				+ "?p=<http://purl.org/dc/elements/1.1/description> || "
//				+ "?p=<http://www.w3.org/2002/07/owl#imports> || "
//				+ "?p=<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)}\n" + "group by ?p";
		System.out.println(cSparql);
		// loadMaps(dataset_code_file);
		// loadMaps(prop_code_file);
		// loadMaps(prop_occur_file);
		Set<String> setHeader = new LinkedHashSet<String>();
		for (String ds : datasets) {
			if (mDsPropOccur.get(ds) == null) {
				mDsPropOccur.put(ds, execSparql(cSparql, ds));
			}
			for (String prop : mDsPropOccur.get(ds).keySet()) {
				setHeader.add(getCodeProp(prop));
				// sbHeader.append("," + getCodeProp(prop));
			}
		}
		StringBuffer sbHeader = new StringBuffer();
		for (String header : setHeader) {
			sbHeader.append(sep + header);
		}
		writer.println(sbHeader.toString());

		mDsPropOccur.clear();
		// mPropertyCode.clear();
		for (String ds : datasets) {
			String line = getCodeDataset(ds);
			if (mDsPropOccur.get(ds) == null) {
				mDsPropOccur.put(ds, execSparql(cSparql, ds));
			}
			for (String prop : setHeader) {
				Map<String, Integer> mTemp = mDsPropOccur.get(ds);
				String codeP = null;
				for (String p : mTemp.keySet()) {
					codeP = getCodeProp(p);
					if (codeP.equals(prop)) {
						codeP = mTemp.get(p).toString();
						break;
					} else {
						codeP = "0";
					}
				}
				line += sep + codeP;
			}
			writer.println(line);
		}
		writer.close();
		generateFile(mDatasetCode, dataset_code_file);
		generateFile(mPropertyCode, prop_code_file);
		generateFileMprop(mDsPropOccur, prop_occur_file);
		// plotGraphKNN(fileName);
	}

	private static void loadMaps(String mFile) throws IOException {
		System.out.println("Loading file: " + mFile);
		File f = new File(mFile);
		if (!f.exists()) {
			System.err.println(mFile + " not found.");
			return;
		}
		List<String> lstLines = FileUtils.readLines(f, "UTF-8");
		for (String line : lstLines) {
			String s[] = line.split("\t");
			if (s.length < 2)
				continue;

			if (mFile.toLowerCase().contains("occur")) {
				String ds = s[0].trim();
				String prop = s[1].trim();
				String code = s[2].trim();
				mPropOccur.put(prop, Integer.valueOf(code));
				if (mDsPropOccur.get(ds) == null) {
					mDsPropOccur.put(ds, mPropOccur);
				} else {
					mDsPropOccur.get(ds).putAll(mPropOccur);
				}
				continue;
			}
			String prop = s[0].trim();
			String code = s[1].trim();
			if (mFile.toLowerCase().startsWith("dataset")) {
				mDatasetCode.put(prop, code);
			} else {
				mPropertyCode.put(prop, code);
			}
		}

	}

	private static void generateFile(Map<String, String> mapCode, String fileName)
			throws FileNotFoundException, UnsupportedEncodingException {
		File f = new File(fileName);
		if (f.exists()) {
			System.err.println(fileName + " Already existis.");
			return;
		}

		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		for (Entry<String, String> entry : mapCode.entrySet()) {
			writer.println(entry.getKey() + "\t" + entry.getValue());
		}
		writer.close();
	}

	private static void generateFileMprop(Map<String, Map<String, Integer>> mapCode, String fileName)
			throws FileNotFoundException, UnsupportedEncodingException {
		File f = new File(fileName);
		if (f.exists()) {
			System.out.println(fileName + " Already existis.");
			return;
		}

		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		for (Entry<String, Map<String, Integer>> entry : mapCode.entrySet()) {
			String ds = getCodeDataset(entry.getKey());
			for (Entry<String, Integer> propOccur : entry.getValue().entrySet()) {
				// ds += "\t" + propOccur.getKey() + "\t" + propOccur.getValue();
				writer.println(ds + "\t" + getCodeProp(propOccur.getKey()) + "\t" + propOccur.getValue());
			}
			// writer.println(ds);
		}
		writer.close();
	}

	private static String getCodeDataset(String ds) {
		if (mDatasetCode.containsKey(ds)) {
			return mDatasetCode.get(ds);
		} else {
			String code = "D" + (mDatasetCode.size() + 1);
			mDatasetCode.put(ds, code);
			return code;
		}
	}

	private static String getCodeProp(String prop) {
		if (mPropertyCode.containsKey(prop)) {
			return mPropertyCode.get(prop);
		} else {
			String code = "P" + (mPropertyCode.size() + 1);
			mPropertyCode.put(prop, code);
			return code;
		}
	}

	private static Map<String, Integer> execSparql(String cSparql, String source) {
		final Map<String, Integer> mRet = new HashMap<String, Integer>();
		try {
			TimeOutBlock timeoutBlock = new TimeOutBlock(1200000);
			Runnable block = new Runnable() {
				public void run() {
					if (Util.isEndPoint(source)) {
						// ret.addAll(execQueryEndPoint(cSparql, source));
						mRet.putAll(Util.execQueryEndPointMap(cSparql, source));
					} else {
						mRet.putAll(Util.execQueryRDFRes(cSparql, source));
					}
				}
			};
			timeoutBlock.addBlock(block);// execute the runnable block
		} catch (Throwable e) {
			System.out.println("TIME-OUT-ERROR - dataset/source: " + source);
		}

		return mRet;
	}

}
