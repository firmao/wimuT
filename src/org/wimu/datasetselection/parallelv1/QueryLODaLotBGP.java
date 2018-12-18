package org.wimu.datasetselection.parallelv1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.QueryExecutionBase;

public class QueryLODaLotBGP {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		String query = "SELECT DISTINCT ?s ?p WHERE { <http://dbpedia.org/resource/Leipzig> ?s ?p }";
		//String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "Select ?s ?o where { ?s owl:sameAs ?o }";

//		String query = "SELECT DISTINCT  ?subject ?predicate ?object\n" + 
//				"WHERE\n" + 
//				"  {   { <http://dbpedia.org/resource/Barnaby_Rudge> ?predicate ?object }\n" + 
//				"    UNION\n" + 
//				"      { ?subject ?predicate <http://dbpedia.org/resource/Barnaby_Rudge> }\n" + 
//				"    FILTER ( ( lang(?object) = \"en\" ) || ( lang(?object) = \"\" ) )\n" + 
//				"  }\n" + 
//				"ORDER BY ?predicate ?subject\n" + 
//				"OFFSET  0\n" + 
//				"LIMIT   200";

//		String query = "PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" + 
//				"PREFIX  :     <http://dbpedia.org/resource/>\n" + 
//				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" + 
//				"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
//				"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
//				"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX  dbpedia: <http://dbpedia.org/>\n" + 
//				"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
//				"PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
//				"\n" + 
//				"SELECT  ?a ?b ?c\n" + 
//				"WHERE\n" + 
//				"  { :Peugeot ?b ?c .\n" + 
//				"    ?a foaf:name \"Peugeot S.A.\"@en\n" + 
//				"  }";
//		String query = "SELECT ?president ?party ?page WHERE {\n" + 
//				"   ?president <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/President> .\n" + 
//				"   ?president <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/United_States> .\n" + 
//				"   ?president <http://dbpedia.org/ontology/party> ?party .\n" + 
//				"   ?x <http://data.nytimes.com/elements/topicPage> ?page .\n" + 
//				"   ?x <http://www.w3.org/2002/07/owl#sameAs> ?president .\n" + 
//				"}";


		Set<String> res = execQuery(query);
		//Set<String> res = execQuery_old(query);
		long totalTime = System.currentTimeMillis() - start;
		File f = writeFileTest(res, "retTriplesLODaLOT.nt");
		System.out.println("File generated: " + f.getAbsolutePath());
		System.out.println("TotalTime: " + totalTime);
	}

	public static Set<String> execQuery(String qSparql) throws IOException {
		long start = System.currentTimeMillis();
		Set<String> ret = new HashSet<String>();
		Set<String> triples = new HashSet<String>();
		//Set<String> uris = Util.extractUrls(qSparql);
		System.out.println("Querying https://hdt.lod.labs.vu.nl with BGPs");
		
		List<Triple> bgpTriple = new BGPSplit().getBGPTriple(qSparql);
		System.out.println("BGP size: " + bgpTriple.size());
		for (Triple bgp : bgpTriple) {
			System.out.println("BGP: " + bgp.toString());
			triples.addAll(getData(bgp));
			System.out.println("Triples: " + triples.size());
		}
		System.out.println("Including in Jena: " + triples.size());
		ret.addAll(executeQueryJena(qSparql, triples));
		if (ret.size() > 0) {
			System.out.println("mAppRes sparqlALOT: " + Util.mAppRes + " ret.size=" + ret.size());
		}
		long totalTime = System.currentTimeMillis() - start;
		Util.updateCount(Approach.SPARQL_A_LOT, ret.size(), totalTime);

		return ret;
	}

	public static Set<String> execQuery_old(String qSparql) throws IOException {
		long start = System.currentTimeMillis();
		Set<String> ret = new HashSet<String>();
		Set<String> triples = new HashSet<String>();
		Set<String> uris = Util.extractUrls(qSparql);
		System.out.println("Querying https://hdt.lod.labs.vu.nl with all results");
		for (String uri : uris) {
			Set<String> cbd = getCBD_LOD_a_lot(uri);
			triples.addAll(cbd);
		}
		System.out.println("Including in Jena: " + triples.size());
		ret.addAll(executeQueryJena(qSparql, triples));
		if (ret.size() > 0) {
			System.out.println("mAppRes sparqlALOT: " + Util.mAppRes + " ret.size=" + ret.size());
		}
		
		long totalTime = System.currentTimeMillis() - start;
		Util.updateCount(Approach.SPARQL_A_LOT, ret.size(), totalTime);

		return ret;
	}
	
	public static boolean execQuery(String qSparql, boolean onlyCheckIfQueryWorks) throws IOException {
		Set<String> triples = new HashSet<String>();
		Set<String> uris = Util.extractUrls(qSparql);
		System.out.println("Querying https://hdt.lod.labs.vu.nl ### OLD ###");
		for (String uri : uris) {
			// Set<String> cbd = getCBD_LOD_a_lot(uri);
			Set<String> cbd = getCBD_LOD_a_lot_old(uri);
			triples.addAll(cbd);
		}

		return (triples.size() > 0);
	}

	public static Set<String> executeQueryJena(String qSparql, Set<String> triples) throws IOException {
		Set<String> ret = new HashSet<String>();
		File fTriples = writeNtFile(triples, "cbdLODaLOT.nt");

		Model model = ModelFactory.createDefaultModel();
		QueryExecutionBase qe = null;
		ResultSet resultSet = null;
		model.read(fTriples.getAbsolutePath(), "N-TRIPLE");
		qe = (QueryExecutionBase) QueryExecutionFactory.create(qSparql, model);
		resultSet = qe.execSelect();
		while (resultSet.hasNext()) {
			QuerySolution triple = resultSet.next();
			Iterator<String> itVars = triple.varNames();
			String sTriple = "";
			while (itVars.hasNext()) {
				String vName = itVars.next();
				if (triple.get(vName).isLiteral()) {
					sTriple += "\"" + triple.get(vName).asLiteral().getString() + "\"^^<"
							+ triple.get(vName).asLiteral().getDatatypeURI() + "> ";
				} else {
					sTriple += "<" + triple.get(vName) + "> ";
				}
			}
			ret.add(sTriple + ".");
		}
		fTriples.delete();
		return ret;
	}

	public static File writeNtFile(Set<String> triples, String fName) throws IOException {
		File fRet = new File(fName);
		fRet.createNewFile();
		PrintWriter writer = new PrintWriter(fRet.getAbsolutePath(), "UTF-8");
//		int maxTriples = 9998;
//		int count = 0;
		for (String triple : triples) {
			if(Util.isValidTriple(triple)) {
				writer.println(triple);
			}
//			if((++count) > maxTriples) { break; }
		}

		writer.close();
		return fRet;
	}

	public static File writeFileTest(Set<String> triples, String fName) throws IOException {
		File fRet = new File(fName);
		fRet.createNewFile();
		PrintWriter writer = new PrintWriter(fRet.getAbsolutePath(), "UTF-8");
		for (String triple : triples) {			
				writer.println(triple);
		}
		writer.close();
		return fRet;
	}
	
	/*
	 * Get CBD from LOD-A-LOT https://hdt.lod.labs.vu.nl/triple
	 */
	private static Set<String> getCBD_LOD_a_lot(String uri) throws IOException {
		Set<String> ret = new HashSet<String>();
		ret.addAll(getData(uri, "s"));
		ret.addAll(getData(uri, "p"));
		ret.addAll(getData(uri, "o"));
		return ret;
	}

	/*
	 * Get CBD from LOD-A-LOT https://hdt.lod.labs.vu.nl/triple
	 */
	private static Set<String> getCBD_LOD_a_lot_old(String uri) throws IOException {
		Set<String> ret = new HashSet<String>();
		ret.addAll(getData(uri, "s"));
		// ret.addAll(getData(uri, "p"));
		ret.addAll(getData(uri, "o"));
		return ret;
	}

	/*
	 * Get data from LOD-A-LOT https://hdt.lod.labs.vu.nl/triple
	 */
	private static Set<String> getData(String uri, String field) throws IOException {
		Set<String> ret = new HashSet<String>();
		int page = 0;
		int pageSize = 1000;
		do {
			try {
				++page;
				URL url = new URL(
						"https://hdt.lod.labs.vu.nl/triple?g=%3Chttps%3A//hdt.lod.labs.vu.nl/graph/LOD-a-lot%3E&"
								+ field + "=%3C" + URLEncoder.encode(uri, "UTF-8") + "%3E&page_size=" + pageSize
								+ "&page=" + page);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

				String triple;
				while ((triple = in.readLine()) != null) {
					String sTriple[] = triple.split(" ");
					String s = Util.formatURI(sTriple[0]);
					String p = Util.formatURI(sTriple[1]);
					String o = Util.formatURI(sTriple[2]);
					// System.out.println(field + "-: " + triple);
					// ret.add(triple.replaceAll("<https://hdt.lod.labs.vu.nl/graph/LOD-a-lot> ",
					// ""));

					if(o.startsWith("<http")) {
						triple = s + " " + p + " " + o + " .";
						ret.add(triple);
					} else {
						ret.add(triple.replaceAll("<https://hdt.lod.labs.vu.nl/graph/LOD-a-lot> ", ""));
					}
				}
				in.close();
			} catch (Exception e) {
				// e.printStackTrace();
				break;
			}
		} while (true);
		return ret;
	}
	
	private static Set<String> getData(Triple bgp) throws IOException {
		Set<String> ret = new HashSet<String>();
		int page = 0;
		int pageSize = 1000;
		String sURL = "";
		do {
			try {
				++page;
				String mStr = "";
				if(!bgp.getSubject().isVariable()) {
					mStr += "&s=%3C" + URLEncoder.encode(bgp.getSubject().toString(), "UTF-8");
				}
				if(!bgp.getPredicate().isVariable()) {
					mStr += "&p=%3C" + URLEncoder.encode(bgp.getPredicate().toString(), "UTF-8");					
				}
				if(!bgp.getObject().isVariable()) {
					mStr += "&o=%3C" + URLEncoder.encode(bgp.getObject().toString(), "UTF-8");
				}
				
				sURL = "https://hdt.lod.labs.vu.nl/triple?g=%3Chttps%3A//hdt.lod.labs.vu.nl/graph/LOD-a-lot%3E" + mStr + "%3E&page_size=" + pageSize + "&page=" + page;
				
				URL url = new URL(sURL);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

				String triple;
				while ((triple = in.readLine()) != null) {
					String sTriple[] = triple.split(" ");
					String s = Util.formatURI(sTriple[0]);
					String p = Util.formatURI(sTriple[1]);
					String o = Util.formatURI(sTriple[2]);
					// System.out.println(field + "-: " + triple);
					// ret.add(triple.replaceAll("<https://hdt.lod.labs.vu.nl/graph/LOD-a-lot> ",
					// ""));

					if(o.startsWith("<http")) {
						triple = s + " " + p + " " + o + " .";
						ret.add(triple);
					} else {
						ret.add(triple.replaceAll("<https://hdt.lod.labs.vu.nl/graph/LOD-a-lot> ", ""));
					}
				}
				in.close();
			} catch (Exception e) {
				// e.printStackTrace();
				break;
			}
		} while (true);
		return ret;
	}
}
