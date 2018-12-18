package org.wimu.datasetselection.parallelv1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.QuerySolution;

public class Util {
	public static Map<String, Set<String>> mUriDataset = new HashMap<String, Set<String>>();
	public static Map<String, Integer> mAppRes = new HashMap<String, Integer>();
	public static Map<String, Long> mAppResTime = new HashMap<String, Long>();
	public static Map<Integer, Integer> mFedBench = new HashMap<Integer, Integer>();
	
	public static void loadFileMap(String mFile) throws IOException {
		System.out.println("Loading file: " + mFile);
		
		mFedBench.put(1, 309);
		mFedBench.put(2, 185);
		mFedBench.put(3, 162);
		mFedBench.put(4, 50);
		mFedBench.put(5, 10);
		mFedBench.put(6, 11);
		mFedBench.put(7, 1024);
		mFedBench.put(8, 22);
		mFedBench.put(9, 1);
		mFedBench.put(10, 3);
		mFedBench.put(11, 239);
		
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
			String uri = s[0];
			String urlDataset = s[1];
			mUriDataset.put(uri, new HashSet<String>());
			mUriDataset.get(uri).add(urlDataset);
		}
	}

	public static Set<String> getQueries() {
		Set<String> ret = new HashSet<String>();
		ret.add("PREFIX dbo: <http://dbpedia.org/ontology/>\n" + "PREFIX dbp: <http://dbpedia.org/resource/>\n"
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "\n" + "SELECT ?name ?bandname where {\n"
				+ "?person foaf:name ?name .\n" + "?band dbo:bandMember ?person .\n"
				+ "?band dbo:genre dbp:Punk_rock .\n" + "?band foaf:name ?bandname .}");
		ret.add("PREFIX dbo: <http://dbpedia.org/ontology/>\n" + "PREFIX dbp: <http://dbpedia.org/resource/>\n"
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
				+ "SELECT ?name ?bandname where {?person foaf:name ?name . " + "?band dbo:bandMember ?person . "
				+ "?band dbo:genre dbp:Punk_rock . " + "?band foaf:name ?bandname .}");
		// ret.add("SELECT ?uri ?id WHERE {?uri
		// <http://dbpedia.org/ontology/wikiPageID> ?id. FILTER (?uri =
		// <http://dbpedia.org/resource/Weight_gain>) }");
		// ret.add("SELECT ?p ?o {
		// <http://nasa.dataincubator.org/spacecraft/1968-089A> ?p ?o}");
		// ret.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
		// +
		// "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		// "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		// "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
		// "PREFIX swrc: <http://swrc.ontoware.org/ontology#>\n" +
		// "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#>\n" +
		// "\n" +
		// "SELECT DISTINCT ?author ?phone\n" +
		// "WHERE {\n" +
		// " ?pub swc:isPartOf
		// <http://data.semanticweb.org/conference/eswc/2009/proceedings> .\n" +
		// " ?pub swc:hasTopic ?topic .\n" +
		// " ?topic rdfs:label ?topicLabel .\n" +
		// " FILTER regex( str(?topicLabel), \"ontology engineering\", \"i\" )
		// .\n" +
		// "\n" +
		// " ?pub swrc:author ?author .\n" +
		// " { ?author owl:sameAs ?authorAlt } UNION { ?authorAlt owl:sameAs
		// ?author }\n" +
		// "\n" +
		// " ?authorAlt foaf:phone ?phone\n" +
		// "}");
		// ret.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		// "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		// "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
		// "PREFIX eurostat:
		// <http://www4.wiwiss.fu-berlin.de/eurostat/resource/eurostat/>\n" +
		// "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
		// "\n" +
		// "SELECT DISTINCT ?cityName ?ur WHERE {\n" +
		// " ?u skos:subject
		// <http://dbpedia.org/resource/Category:Universities_and_colleges_in_Lower_Saxony>
		// ;\n" +
		// " dbpedia:city ?c .\n" +
		// " ?c owl:sameAs [ rdfs:label ?cityName ;\n" +
		// " eurostat:unemployment_rate_total ?ur ]\n" +
		// "}\n");
		// ret.add("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
		// "\n" +
		// "SELECT DISTINCT ?i WHERE {\n" +
		// " <http://www.w3.org/People/Berners-Lee/card#i> foaf:knows ?p .\n" +
		// " ?p foaf:interest ?i .\n" +
		// "}");
		// ret.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
		// +
		// "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		// "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		// "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
		// "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
		// "PREFIX movie: <http://data.linkedmdb.org/resource/movie/>\n" +
		// "PREFIX mo: <http://purl.org/ontology/mo/>\n" +
		// "\n" +
		// "SELECT DISTINCT ?movieTitle ?image\n" +
		// "WHERE {\n" +
		// " ?movie movie:director
		// <http://data.linkedmdb.org/resource/director/7156> .\n" +
		// " ?movie rdfs:label ?movieTitle .\n" +
		// "\n" +
		// " ?movie movie:music_contributor ?music_contributor .\n" +
		// " ?music_contributor owl:sameAs ?artist .\n" +
		// " ?artist foaf:made ?record .\n" +
		// " ?record mo:release_type mo:soundtrack .\n" +
		// " ?record dc:title ?recordTitle .\n" +
		// " FILTER ( STR(?recordTitle) = STR(?movieTitle) )\n" +
		// "\n" +
		// " { ?record mo:image ?image . }\n" +
		// "}");
		// ret.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
		// +
		// "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		// "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		// "PREFIX drugbank:
		// <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/>\n" +
		// "PREFIX tcm: <http://purl.org/net/tcm/tcm.lifescience.ntu.edu.tw/>\n"
		// +
		// "\n" +
		// "SELECT DISTINCT ?diseaseLabel ?altMedicineLabel\n" +
		// "WHERE {\n" +
		// "
		// <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB01273>\n"
		// +
		// " drugbank:possibleDiseaseTarget ?disease.\n" +
		// " ?disease owl:sameAs ?sameDisease.\n" +
		// "\n" +
		// " ?altMedicine tcm:treatment ?sameDisease.\n" +
		// " ?altMedicine rdf:type tcm:Medicine.\n" +
		// " ?sameDisease rdfs:label ?diseaseLabel.\n" +
		// " ?altMedicine rdfs:label ?altMedicineLabel.\n" +
		// "}");
		return ret;
	}

	public static Set<String> getSampleQueries() {
		Set<String> ret = new HashSet<String>();
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://linkedgeodata.org/triplify/node412946829> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://acm.rkbexplorer.com/id/93601> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://citeseer.rkbexplorer.com/id/resource-CSP25940-be11bb484cf705c86b1b6280eff943b9> ?s ?p }");
		// ret.add("SELECT DISTINCT ?s ?p WHERE {
		// <http://oai.rkbexplorer.com/id/opus.bath.ac.uk/person-78b3db1160c89486987cdcc0a7c3909e-4795f9a6c2ef498a5350b52ddd3dcd93>
		// ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://mpii.de/yago/resource/The_Last_Ninja> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://wordnet.rkbexplorer.com/id/wordsense-mess_around-verb-1> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://dbpedia.org/resource/Leipzig> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://dbpedia.org/resource/Berlin> ?s ?p }");
		ret.add("SELECT DISTINCT ?s ?p	WHERE { <http://citeseer.rkbexplorer.com/id/resource-CS116606> ?s ?p }");
		return ret;
	}

	public static List<String> getSampleQueries(File file) {
		List<String> ret = new ArrayList<String>();
		try {
			List<String> lstLines = FileUtils.readLines(file, "UTF-8");
			String query = "";
			for (String line : lstLines) {
				// if(!line.equals("§")){
				if (!line.startsWith("#------")) {
					query += line + "\n";
				} else {
					ret.add(query);
					query = "";
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static String getTopTable() {
		// BuildMyString.com generated code. Please enjoy your string
		// responsibly.
		String sb = "<style type=\"text/css\">" + ".tg  {border-collapse:collapse;border-spacing:0;margin:0px auto;}"
				+ ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}"
				+ ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}"
				+ ".tg .tg-88nc{font-weight:bold;border-color:inherit;text-align:center}"
				+ ".tg .tg-c3ow{border-color:inherit;text-align:center;vertical-align:top}"
				+ ".tg .tg-uys7{border-color:inherit;text-align:center}"
				+ ".tg .tg-7btt{font-weight:bold;border-color:inherit;text-align:center;vertical-align:top}"
				+ ".tg-sort-header::-moz-selection{background:0 0}.tg-sort-header::selection{background:0 0}.tg-sort-header{cursor:pointer}.tg-sort-header:after{content:'';float:right;margin-top:7px;border-width:0 5px 5px;border-style:solid;border-color:#404040 transparent;visibility:hidden}.tg-sort-header:hover:after{visibility:visible}.tg-sort-asc:after,.tg-sort-asc:hover:after,.tg-sort-desc:after{visibility:visible;opacity:.4}.tg-sort-desc:after{border-bottom:none;border-width:5px 5px 0}@media screen and (max-width: 767px) {.tg {width: auto !important;}.tg col {width: auto !important;}.tg-wrap {overflow-x: auto;-webkit-overflow-scrolling: touch;margin: auto 0px;}}</style>"
				+ "<div class=\"tg-wrap\"><table id=\"tg-xWBNy\" class=\"tg\">" + "  <tr>"
				+ "    <th class=\"tg-7btt\" rowspan=\"2\">Query</th>"
				+ "    <th class=\"tg-7btt\" colspan=\"2\">Time to Source/Dataset-Selection</th>"
				+ "    <th class=\"tg-7btt\" colspan=\"2\">Output / query results</th>" + "  </tr>" + "  <tr>"
				+ "    <td class=\"tg-88nc\">Wimu</td>" + "    <td class=\"tg-88nc\">SQUIN</td>"
				+ "    <td class=\"tg-7btt\">Wimu</td>" + "    <td class=\"tg-7btt\">SQUIN</td>"
				+ "    <td class=\"tg-7btt\">DataseWimu</td>" + "    <td class=\"tg-7btt\">Query</td>" + "  </tr>";
		return sb;
	}

	public static String getEndTable() {
		// BuildMyString.com generated code. Please enjoy your string
		// responsibly.
		String sb = "</table></div>"
				+ "<script charset=\"utf-8\">var TGSort=window.TGSort||function(n){\"use strict\";function r(n){return n.length}function t(n,t){if(n)for(var e=0,a=r(n);a>e;++e)t(n[e],e)}function e(n){return n.split(\"\").reverse().join(\"\")}function a(n){var e=n[0];return t(n,function(n){for(;!n.startsWith(e);)e=e.substring(0,r(e)-1)}),r(e)}function o(n,r){return-1!=n.map(r).indexOf(!0)}function u(n,r){return function(t){var e=\"\";return t.replace(n,function(n,t,a){return e=t.replace(r,\"\")+\".\"+(a||\"\").substring(1)}),l(e)}}function i(n){var t=l(n);return!isNaN(t)&&r(\"\"+t)+1>=r(n)?t:NaN}function s(n){var e=[];return t([i,m,g],function(t){var a;r(e)||o(a=n.map(t),isNaN)||(e=a)}),e}function c(n){var t=s(n);if(!r(t)){var o=a(n),u=a(n.map(e)),i=n.map(function(n){return n.substring(o,r(n)-u)});t=s(i)}return t}function f(n){var r=n.map(Date.parse);return o(r,isNaN)?[]:r}function v(n,r){r(n),t(n.childNodes,function(n){v(n,r)})}function d(n){var r,t=[],e=[];return v(n,function(n){var a=n.nodeName;\"TR\"==a?(r=[],t.push(r),e.push(n)):(\"TD\"==a||\"TH\"==a)&&r.push(n)}),[t,e]}function p(n){if(\"TABLE\"==n.nodeName){for(var e=d(n),a=e[0],o=e[1],u=r(a),i=u>1&&r(a[0])<r(a[1])?1:0,s=i+1,v=a[i],p=r(v),l=[],m=[],g=[],h=s;u>h;++h){for(var N=0;p>N;++N){r(m)<p&&m.push([]);var T=a[h][N],C=T.textContent||T.innerText||\"\";m[N].push(C.trim())}g.push(h-s)}var L=\"tg-sort-asc\",E=\"tg-sort-desc\",b=function(){for(var n=0;p>n;++n){var r=v[n].classList;r.remove(L),r.remove(E),l[n]=0}};t(v,function(n,t){l[t]=0;var e=n.classList;e.add(\"tg-sort-header\"),n.addEventListener(\"click\",function(){function n(n,r){var t=d[n],e=d[r];return t>e?a:e>t?-a:a*(n-r)}var a=l[t];b(),a=1==a?-1:+!a,a&&e.add(a>0?L:E),l[t]=a;var i=m[t],v=function(n,r){return a*i[n].localeCompare(i[r])||a*(n-r)},d=c(i);(r(d)||r(d=f(i)))&&(v=n);var p=g.slice();p.sort(v);for(var h=null,N=s;u>N;++N)h=o[N].parentNode,h.removeChild(o[N]);for(var N=s;u>N;++N)h.appendChild(o[s+p[N-s]])})})}}var l=parseFloat,m=u(/^(?:\\s*)([+-]?(?:\\d+)(?:,\\d{3})*)(\\.\\d*)?$/g,/,/g),g=u(/^(?:\\s*)([+-]?(?:\\d+)(?:\\.\\d{3})*)(,\\d*)?$/g,/\\./g);n.addEventListener(\"DOMContentLoaded\",function(){for(var t=n.getElementsByClassName(\"tg\"),e=0;e<r(t);++e)try{p(t[e])}catch(a){}})}(document);</script>";

		return sb;
	}

	public static List<WimuTQuery> executeAllQueries(List<String> setQueries) {
		final List<WimuTQuery> ret = new ArrayList<WimuTQuery>();
		System.out.println("### Executing SQUIN AND WimuT ###");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				long start = System.currentTimeMillis();
				//boolean hasResultsSquin = Squin.execSquin(query);
				String rSquin = Traversal.execTraversal(query);
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setResultSquin(rSquin);
				wQuery.setHasResultsSquin(rSquin.contains("http"));
				wQuery.setTimeSquin(totalTime);

				start = System.currentTimeMillis();
				//WimuResult wRes = WimuSelection.execQuery(query, false);
				final WimuResult wRes = new WimuResult();
				
				long timeout = 600000; //10 minutes
				wRes.setAll(WimuSelection.execQuery(query, false, timeout));
				
				// WimuResult wRes = WimuSelection.execQueryParallel(query, false);
				totalTime = System.currentTimeMillis() - start;
				if(wRes.getResult() != null) {
					wQuery.setHasResultsWimu(wRes.getResult().contains("http"));
				}
				wQuery.setResultLODaLOT(wRes.isResultLODaLOT());
				wQuery.setResultDBpedia(wRes.isResultDBpedia());
				wQuery.setTimeWimu(totalTime);
				wQuery.setQuery(query);
				wQuery.setDatasetWimu(wRes.getBestDataset());
				wQuery.setDatasets(wRes.getDatasets());
				if((wQuery.hasResultsWimu()==false) && (wQuery.hasResultsSquin()==true)) {
					System.out.println("### WimuT getting results using SQUIN algorithm. ###");
					wQuery.setResultsFromSquin(true);
					wQuery.setResults(wQuery.getResultSquin());
				}else {
					wQuery.setResults(wRes.getResult());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			wQuery.setMapAppResults(Util.mAppRes);
			wQuery.setMapAppTime(Util.mAppResTime);
			ret.add(wQuery);
			System.out.println("Query " + ret.size() + " of " + setQueries.size() + " done");
			//wQuery.getMapAppResults().keySet().forEach( app -> {
			for (String app : wQuery.getMapAppResults().keySet()) {
				System.out.println("#------ Approach: " + app + "---NumResults:" + wQuery.getMapAppResults().get(app));
			}  
			//});
		}
		// });
		return ret;
	}

	public static List<WimuTQuery> executeQueriesWimuT(List<String> setQueries) {
		final List<WimuTQuery> ret = new ArrayList<WimuTQuery>();

		System.out.println("###### ONLY WIMU_T #########");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				wQuery.setHasResultsSquin(false);
				wQuery.setTimeSquin(0);
				long start = System.currentTimeMillis();
				final WimuResult wRes = new WimuResult();
				long timeout = 600000; //10 minutes
				wRes.setAll(WimuSelection.execQuery(query, false, timeout));
				long totalTime = System.currentTimeMillis() - start;
				
				if(wRes.getResult() != null) {
					wQuery.setHasResultsWimu(wRes.getResult().contains("<http"));
				}
				wQuery.setResultLODaLOT(wRes.isResultLODaLOT());
				wQuery.setResultDBpedia(wRes.isResultDBpedia());
				wQuery.setTimeWimu(totalTime);
				wQuery.setQuery(query);
				wQuery.setDatasetWimu(wRes.getBestDataset());
				wQuery.setDatasets(wRes.getDatasets());
				wQuery.setResults(wRes.getResult());
			} catch (Exception e) {
				e.printStackTrace();
			}
			ret.add(wQuery);
			System.out.println("Query " + ret.size() + " of " + setQueries.size() + " done");
		}
		// });
		return ret;
	}

	public static List<WimuTQuery> executeQueriesTraversal(List<String> setQueries) {
		final List<WimuTQuery> ret = new ArrayList<WimuTQuery>();

		System.out.println("####### ONLY Traversal based on SQUIN ############");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				long start = System.currentTimeMillis();
				//boolean hasResultsSquin = Squin.execSquin(query);
				String rSquin = Traversal.execTraversal(query);
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setResultSquin(rSquin);
				wQuery.setHasResultsSquin(rSquin.contains("<http"));
				wQuery.setTimeSquin(totalTime);

				wQuery.setHasResultsWimu(false);
				wQuery.setTimeWimu(0);
				wQuery.setQuery(query);
				wQuery.setDatasetWimu("");
				wQuery.setDatasets(new HashMap<String, Set<String>>());
			} catch (Exception e) {
				e.printStackTrace();
			}
			ret.add(wQuery);
			System.out.println("Query " + ret.size() + " of " + setQueries.size() + " done");
		}
		// });
		return ret;
	}

	public static void writeFile(List<WimuTQuery> res, String fileName) {
		try {
			String newFileName = fileName.substring(0,fileName.length() - 4);
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			PrintWriter writerIdQuery = new PrintWriter(newFileName + "_idQuery.txt", "UTF-8");
			
			int indQ = 0;
			writer.println("idQuery\tdataset(s) name\tsize(bytes)\t" + "numberOfDatasets\t" + "Traversal\t"
					+ "WimuTResults\tHasResultsLODaLOT\tHasResultsDBpedia"
					+ "\tTimeoutError\tMoreWimuTraversal"
					+ "\tFedBenchValue\tnumResENDPOINT\tTimeEndPoint\tnumResSPARQL_A_LOT\tTimeSPARQL_A_LOT\tnumResTRAVERSALT\tTimeTraversal\tnumResWIMU_DUMP\tTimeWimuT");
			
//			if(MemoryControl.thereIsResultsInDisk) {
//				res = MemoryControl.copyResFromDisk();
//			}
			
			for (WimuTQuery wQuery : res) {
				writer.println((++indQ) + "\t" + wQuery.getDatasets() + "\t"
						+ wQuery.getSumSizeDatasets() + "\t"
						+ ((wQuery.getDatasets() != null) ? wQuery.getDatasets().size() : 0) + "\t"
						+ wQuery.hasResultsSquin()  
						+ "\t" + wQuery.hasResultsWimu()
						+ "\t" + wQuery.isResultLODaLOT()
						+ "\t" + wQuery.isResultDBpedia()
						+ "\t" + wQuery.isTimeoutError()
						+ "\t" + wQuery.isResultsFromSquin()
						+ "\t" + mFedBench.get(indQ)
						+ "\t" + wQuery.getMapAppResults().get(Approach.ENDPOINT)
						+ "\t" + wQuery.getMapAppTime().get(Approach.ENDPOINT)
						+ "\t" + wQuery.getMapAppResults().get(Approach.SPARQL_A_LOT)
						+ "\t" + wQuery.getMapAppTime().get(Approach.SPARQL_A_LOT)
						+ "\t" + wQuery.getMapAppResults().get(Approach.TRAVERSAL)
						+ "\t" + wQuery.getTimeSquin()
						+ "\t" + wQuery.getMapAppResults().get(Approach.WIMU_DUMP)
						+ "\t" + wQuery.getTimeWimu()
						);
				
				writerIdQuery.println("#------"+ indQ + "\n");
				//wQuery.getMapAppResults().keySet().forEach( app -> {
				for (String app : wQuery.getMapAppResults().keySet()) {
					writerIdQuery.println("#------ Approach: " + app + "---NumResults:" + wQuery.getMapAppResults().get(app)); 
				}
				//});
				writerIdQuery.println("#------\n" + wQuery.getQuery());
				String resNameDir = "results_" + fileName.substring(0,fileName.length() - 4);
				File f = new File(resNameDir);
				f.mkdir();
				
				PrintWriter writerWimuResults = new PrintWriter(f.getAbsolutePath() + "/result_wimu_q"+indQ+".txt", "UTF-8");
				writerWimuResults.println(wQuery.getResults());
				writerWimuResults.close();
				
				PrintWriter writerSquinResults = new PrintWriter(f.getAbsolutePath() + "/result_squin_q"+indQ+".txt", "UTF-8");
				writerSquinResults.println(wQuery.getResultSquin());
				writerSquinResults.close();
			}
			writer.close();
			writerIdQuery.println("#------");
			writerIdQuery.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(Map<String, Set<String>> mURIs, String fileName) {
		try {
			System.out.println("Writing file URI->Dataset: " + fileName);
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			mURIs.forEach((uri, dataset) -> {
				String sDs = "";
				for (String ds : dataset) {
					sDs += "\t" + ds;
				}
				writer.println(uri + sDs);
			});
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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

	/*
	 * @cSparql SPARQL query.
	 */
	public static Map<String, Set<String>> getDatasetsTotal(String cSparql) throws InterruptedException, IOException {
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();

		Set<String> uris = extractUrls(cSparql);

		for (String uri : uris) {
			Set<String> dsWIMUs = getDsWIMUs(uri);
			if (dsWIMUs != null) {
					ret.put(uri, dsWIMUs);
			}
		}
		return ret;
	}
	
	public static Set<String> getDsWIMUs(String uri) throws InterruptedException, IOException {
		Set<String> sRet = new HashSet<String>();

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
				if (sRet.size() > 10)
					break;
				sRet.add(wDs.getDataset());
				if (sRet.size() < 1) {
					sRet.add(wDs.getHdt());
				}
			}
			mUriDataset.put(uri, sRet);
		} catch (Exception e) {
			System.err.println("No dataset for the URI: " + uri);
		}
		System.out.println("URI: " + uri + " NumberDatasets: " + sRet.size());
		return sRet;
	}

	public static Set<String> extractUrls(String qSparql) throws UnsupportedEncodingException {
		Set<String> containedUrls = new HashSet<String>();
		
		String fixSparql = replacePrefixes(qSparql);
		
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(fixSparql);
		
		while (urlMatcher.find()) {
			containedUrls.add(fixSparql.substring(urlMatcher.start(0), urlMatcher.end(0)));
		}

		return containedUrls;
	}
	
	public static String replacePrefixes(String query) throws UnsupportedEncodingException{
		PrefixMapping pmap = PrefixMapping.Factory.create();
//		Map<String, String> mPrefixURI = getMapPrefix(query);
//		pmap.setNsPrefixes(mPrefixURI);
	    pmap.setNsPrefixes(PrefixMapping.Extended);
		Prologue prog = new Prologue();
	    prog.setPrefixMapping(pmap);
	    Query q = QueryFactory.parse(new Query(prog), query, null, null);
	    //Set Prefix Mapping
	    q.setPrefixMapping(pmap);
	    //remove PrefixMapping so the prefixes will get replaced by the full uris
	    q.setPrefixMapping(null);       
	    return q.serialize();
	}

	public static Set<String> execQueryEndPoint(String cSparql, String endPoint) {
		System.out.println("Query endPoint: " + endPoint);
		final Set<String> ret = new HashSet<String>();
		final long offsetSize = 9999;
		long offset = 0;
		do {
			String sSparql = cSparql;
			int indOffset = cSparql.toLowerCase().indexOf("offset");
			int indLimit = cSparql.toLowerCase().indexOf("limit");
			if((indLimit < 0) && (indOffset < 0)) {
				sSparql = cSparql += " offset " + offset + " limit " + offsetSize;
			}
			com.hp.hpl.jena.query.Query query = com.hp.hpl.jena.query.QueryFactory.create(sSparql);
			com.hp.hpl.jena.query.QueryExecution qexec = com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(endPoint, query);
			try {

				com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
				//com.hp.hpl.jena.query.ResultSet copyResults = com.hp.hpl.jena.query.ResultSetFactory.copyResults(results);
				//int count = com.hp.hpl.jena.query.ResultSetFormatter.consume(results);
				
				//Util.updateCount(Approach.ENDPOINT, count);
				//if(count > 0) {System.out.println("MapAppRes: " + Util.mAppRes);}
				//ret = "EndPoint: " + endPoint + ": " + Util.mAppRes.get(Approach.ENDPOINT);
				List<QuerySolution> lst = com.hp.hpl.jena.query.ResultSetFormatter.toList(results);
				for (QuerySolution qSolution : lst) {
					final StringBuffer sb = new StringBuffer();
					for ( final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext(); ) {
		                final String varName = varNames.next();
		                sb.append(qSolution.get(varName).toString() + " ");
		            }
					ret.add(sb.toString() + "\n");
				}
				
				if((indLimit > 0) || (indOffset > 0)) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			} finally {
				qexec.close();
			}
			//System.out.print(offset);
			offset += offsetSize;
		} while (true);
		
		return ret;
	}
	
	public static Set<String> execQueryEndPoint(String cSparql, String endPoint, boolean noOffset) {
		System.out.println("Query endPoint: " + endPoint);
		final Set<String> ret = new HashSet<String>();
		final long offsetSize = 9999;
		long offset = 0;
		do {
			String sSparql = cSparql;
			int indOffset = cSparql.toLowerCase().indexOf("offset");
			int indLimit = cSparql.toLowerCase().indexOf("limit");
			if((indLimit < 0) && (indOffset < 0)) {
				sSparql = cSparql += " offset " + offset + " limit " + offsetSize;
			}
			com.hp.hpl.jena.query.Query query = com.hp.hpl.jena.query.QueryFactory.create(sSparql);
			com.hp.hpl.jena.query.QueryExecution qexec = com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(endPoint, query);
			try {

				com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
				List<QuerySolution> lst = com.hp.hpl.jena.query.ResultSetFormatter.toList(results);
				for (QuerySolution qSolution : lst) {
					final StringBuffer sb = new StringBuffer();
					for ( final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext(); ) {
		                final String varName = varNames.next();
		                sb.append(qSolution.get(varName).toString() + " ");
		            }
					ret.add(sb.toString() + "\n");
				}
				
				if((indLimit > 0) || (indOffset > 0)) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			} finally {
				qexec.close();
			}
			//System.out.print(offset);
			offset += offsetSize;
		} while (true);
		
		return ret;
	}

	public static List<WimuTQuery> executeQueriesLODaLOT(List<String> setQueries) {
		final List<WimuTQuery> ret = new ArrayList<WimuTQuery>();
		System.out.println("### Executing only using SPARQL LOD-A-LOT API ###");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				long start = System.currentTimeMillis();
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setResultSquin("");
				wQuery.setHasResultsSquin(false);
				wQuery.setTimeSquin(totalTime);

				start = System.currentTimeMillis();
				try {
					// 10 minutes.
			        TimeOutBlock timeoutBlock = new TimeOutBlock(600000);
			        Runnable block=new Runnable() {
			            @Override
			            public void run() {
			            	try {
								//wRes.setAll(WimuSelection.execQuery(query, false));
			            		Util.updateCount(Approach.SPARQL_A_LOT, 0, 0);
			            		Set<String> resLodAlot = QueryLODaLot.execQuery(query);
			            		String retLODs = "";
			            		for (String retLOD : resLodAlot) {
									retLODs += retLOD;
								}
			            		wQuery.setResults(retLODs);
							} catch (Exception e) {
								e.printStackTrace();
							}
			            }
			        };
			        timeoutBlock.addBlock(block);// execute the runnable block 
			    } catch (Throwable e) {
			        System.out.println("TIME-OUT-ERROR: " + e.getMessage());
			        wQuery.setTimeoutError(true);
			    }
				
				// WimuResult wRes = WimuSelection.execQueryParallel(query, false);
				totalTime = System.currentTimeMillis() - start;
				wQuery.setHasResultsWimu(false);
				wQuery.setMapAppResults(Util.mAppRes);
				if(wQuery.getResults() != null) {
					wQuery.setResultLODaLOT(wQuery.getResults().length() > 1);
				}
				wQuery.setResultDBpedia(false);
				wQuery.setTimeWimu(totalTime);
				wQuery.setQuery(query);
				wQuery.setDatasetWimu(null);
				wQuery.setDatasets(null);
				wQuery.setResultsFromSquin(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ret.add(wQuery);
			System.out.println("Query " + ret.size() + " of " + setQueries.size() + " done");
		}
		// });
		return ret;
	}

	public static void updateCount(String appType, int count, long time) {
		if(mAppRes.containsKey(appType)) {
			count += Util.mAppRes.get(appType);	
		}
		mAppRes.put(appType, count);
		updateTime(appType, time);
	}
	
	public static void updateTime(String appType, long time) {
		if(mAppResTime.containsKey(appType)) {
			time += Util.mAppResTime.get(appType);	
		}
		mAppResTime.put(appType, time);
	}
	
	public static String forceDerefURIs(String qSparql) throws UnsupportedEncodingException {
		Set<String> uris = extractUrls(qSparql);
		String ret = qSparql;
		
		/*
		 * Take a look in this paper:
		 * https://pdfs.semanticscholar.org/01bd/8485943f914fec7f4b8ec01f4dfc1b633cc3.pdf
		 */
		for (String uri : uris) {
			if(!isGoodURL(uri)) {
				String newURI = makeDerefURI(uri);
				ret = ret.replaceAll(uri, newURI);
			}
		}
		
		return ret;
	}

	private static boolean isGoodURL(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if ((responseCode == 200) || (responseCode == 400)) {
				return true;
			} else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * Just give a URI from WIMU where returns the CBD of the URI.
	 */
	private static String makeDerefURI(String uri) throws UnsupportedEncodingException {
		String dURI = URLDecoder.decode(uri, "UTF-8");
		String ret = "http://wimu.aksw.org/Find?cbd="+dURI+"&ds=LODaLOT";
		
		return ret;
	}
	
	public static boolean isValidTriple(String triple) {
		String sTriple[] = triple.split(" ");
		String s = sTriple[0];
		String p = sTriple[1];
		String o = sTriple[2];

		if ((s.length() < 5) || (p.length() < 5) || (o.length() < 5)) {
			return false;
		}

		if ((!s.startsWith("<http")) || (!p.startsWith("<http"))) {
			return false;
		}

		if (s.contains(" ") || p.contains(" ") || o.contains(" ")) {
			return false;
		}

		return true;
	}
	
	public static String formatURI(String sURI) {
		String nURI = sURI.trim().replaceAll(" ", "");

		if (nURI.length() < 5) {
			return "<http//wimu.error.treatment>";
		}

		if (!nURI.contains("http")) {
			return "<http//wimu.error.treatment>";
		}

		if ((!nURI.endsWith(">")) && (!nURI.contains("http"))) {
			return "<http//wimu.error.treatment>";
		}
		return nURI;
	}

	public static boolean ask(String cSparql, Model model) {
//		long start = System.currentTimeMillis();
//		BGPSplit bgps = new BGPSplit();
//		List<Triple> lstTp = bgps.getBGPTriple(cSparql);
//		for (Triple triple : lstTp) {
//			//String szQuery = " ASK { <u:John> <u:parentOf> <u:Mary> } ";
//			String szQuery = " ASK { "+ formatTriple(triple) +" } ";
//			    
//			Query query = QueryFactory.create(szQuery) ;
//			QueryExecution qexec = QueryExecutionFactory.create(query, model);
//			boolean b = qexec.execAsk();
//			if(b) {
//				long total = System.currentTimeMillis() - start;
//				System.out.println("Time to ASK: " + total);
//				return b;
//			}
//		}
//		long total = System.currentTimeMillis() - start;
//		System.out.println("Time to ASK: " + total);
//		return false;
		return true;
	}

	private static String formatTriple(Triple triple) {
		String ret = "";
		
		if(!triple.getSubject().isVariable()) {
			ret += "<" + triple.getSubject().getURI().toString() + "> "; 
		} else {
			ret += triple.getSubject().toString().replaceAll("@", "") + " ";
		}
		
		if(!triple.getPredicate().isVariable()) {
			ret += "<" + triple.getPredicate().getURI().toString() + "> "; 
		} else {
			ret += triple.getPredicate().toString().replaceAll("@", "") + " ";
		}
		
		if(!triple.getObject().isVariable()) {
			ret += "<" + triple.getObject().getURI().toString() + "> "; 
		} else {
			ret += triple.getObject().toString().replaceAll("@", "") + " ";
		}
		
		return ret;
	}
}
