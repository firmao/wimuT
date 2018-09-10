package org.wimu.datasetselection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

import com.google.gson.Gson;

public class Util {
	public static Map<String, String> mUriDataset = new HashMap<String, String>();

	public static void loadFileMap(String mFile) throws IOException {
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
			String uri = s[0];
			String urlDataset = s[1];
			mUriDataset.put(uri, urlDataset);

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

	public static Set<String> getSampleQueries(String fName) {
		Set<String> ret = new HashSet<String>();
		try {
			List<String> lstLines = FileUtils.readLines(new File(fName), "UTF-8");
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
			// TODO Auto-generated catch block
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

	public static Set<WimuTQuery> executeAllQueries(Set<String> setQueries) {
		final Set<WimuTQuery> ret = new HashSet<WimuTQuery>();
		System.out.println("### Executing SQUIN AND WimuT ###");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				long start = System.currentTimeMillis();
				//boolean hasResultsSquin = Squin.execSquin(query);
				String rSquin = Squin.execSquin(query);
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setResultSquin(rSquin);
				wQuery.setHasResultsSquin(rSquin.contains("<http"));
				wQuery.setTimeSquin(totalTime);

				start = System.currentTimeMillis();
				WimuResult wRes = WimuSelection.execQuery(query, false);
				// WimuResult wRes = WimuSelection.execQueryParallel(query, false);
				totalTime = System.currentTimeMillis() - start;
				wQuery.setHasResultsWimu(wRes.getResult().contains("<http"));
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

	public static Set<WimuTQuery> executeAllQueriesWimuT(Set<String> setQueries) {
		final Set<WimuTQuery> ret = new HashSet<WimuTQuery>();

		System.out.println("###### ONLY WIMU_T #########");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				wQuery.setHasResultsSquin(false);
				wQuery.setTimeSquin(0);
				long start = System.currentTimeMillis();
				WimuResult wRes = WimuSelection.execQuery(query, false);
				// WimuResult wRes = WimuSelection.execQueryParallel(query, false);
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setHasResultsWimu(wRes.getResult().contains("<http"));
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

	public static Set<WimuTQuery> executeAllQueriesSquin(Set<String> setQueries) {
		final Set<WimuTQuery> ret = new HashSet<WimuTQuery>();

		System.out.println("####### ONLY SQUIN ############");
		// setQueries.parallelStream().forEach(query -> {
		for (String query : setQueries) {
			WimuTQuery wQuery = new WimuTQuery();
			try {
				long start = System.currentTimeMillis();
				//boolean hasResultsSquin = Squin.execSquin(query);
				String rSquin = Squin.execSquin(query);
				long totalTime = System.currentTimeMillis() - start;
				wQuery.setResultSquin(rSquin);
				wQuery.setHasResultsSquin(rSquin.contains("<http"));
				wQuery.setTimeSquin(totalTime);

				wQuery.setHasResultsWimu(false);
				wQuery.setTimeWimu(0);
				wQuery.setQuery(query);
				wQuery.setDatasetWimu("");
				wQuery.setDatasets(new HashMap<String, String>());
			} catch (Exception e) {
				e.printStackTrace();
			}
			ret.add(wQuery);
			System.out.println("Query " + ret.size() + " of " + setQueries.size() + " done");
		}
		// });
		return ret;
	}

	public static void writeFile(Set<WimuTQuery> res, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			PrintWriter writerIdQuery = new PrintWriter("idQuery.tsv", "UTF-8");
			
			int indQ = 0;
			writer.println("idQuery\ttime(ms)\tdataset(s) name\tsize(bytes)\t" + "numberOfDatasets\t" + "SquinResults\t"
					+ "TimeSquin\t" + "WimuTResults");
			for (WimuTQuery wQuery : res) {
				writer.println((++indQ) + "\t" + wQuery.getTimeWimu() + "\t" + wQuery.getDatasets() + "\t"
						+ wQuery.getSumSizeDatasets() + "\t"
						+ ((wQuery.getDatasets() != null) ? wQuery.getDatasets().size() : 0) + "\t"
						+ wQuery.hasResultsSquin() + "\t" + wQuery.getTimeSquin() + "\t" + wQuery.hasResultsWimu());
				
				writerIdQuery.println("#---"+ indQ + "\t" + wQuery.getQuery());
				
				File f = new File("results");
				f.mkdir();
				
				PrintWriter writerWimuResults = new PrintWriter("results/result_wimu_q"+indQ+".tsv", "UTF-8");
				writerWimuResults.println(wQuery.getResults());
				writerWimuResults.close();
				
				PrintWriter writerSquinResults = new PrintWriter("results/result_squin_q"+indQ+".tsv", "UTF-8");
				writerSquinResults.println(wQuery.getResultSquin());
				writerSquinResults.close();
			}
			writer.close();
			writerIdQuery.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(Map<String, String> mURIs, String fileName) {
		try {
			System.out.println("Writing file URI->Dataset: " + fileName);
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			mURIs.forEach((uri, dataset) -> {
				writer.println(uri + "\t" + dataset);
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
	 * @includePrefix URI Lookup also with the prefix.
	 */
	public static Map<String, String> getDatasets(String cSparql) throws InterruptedException, IOException {
		Map<String, String> ret = new HashMap<String, String>();

//		String[] uris = null;
//		if (includePrefix) {
//			uris = cSparql.split("<");
//		} else {
//			uris = cSparql.substring(cSparql.toLowerCase().indexOf("select")).split("<");
//		}

		Set<String> uris = extractUrls(cSparql);

		String dsWIMU = null;
		for (String uri : uris) {
			dsWIMU = getDsWIMU(uri);
			if (dsWIMU != null) {
				ret.put(uri, dsWIMU);
			}
		}
		return ret;
	}

	public static String getDsWIMU(String uri) throws InterruptedException, IOException {
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
			mUriDataset.put(uri, sRet);
		} catch (Exception e) {
			System.err.println("No dataset for the URI: " + uri);
		}

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

	private static Map<String, String> getMapPrefix(String query) throws UnsupportedEncodingException {
		Map<String, String> mPrefixURI = new HashMap<String, String>();
		String s[] = query.split("\n");
		for (String line : s) {
			if(line.startsWith("PREFIX")) {
				String uri = line.substring(line.indexOf("<")+1, line.indexOf(">"));
				uri = URLEncoder.encode(uri, "UTF-8");
				String prefixName = line.substring(7, line.indexOf(":"));
				mPrefixURI.put(uri.trim(), prefixName.trim());
			}
		}
		return mPrefixURI;
	}
}
