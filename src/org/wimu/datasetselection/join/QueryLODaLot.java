package org.wimu.datasetselection.join;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class QueryLODaLot {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		// String query = "SELECT DISTINCT ?s ?p WHERE {
		// <http://dbpedia.org/resource/Leipzig> ?s ?p }";
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "Select ?s ?o where { ?s owl:sameAs ?o }";

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
//		String query = "select ?o2 where {\n" + 
//				"<http://dbpedia.org/resource/Germany> <http://dbpedia.org/ontology/capital> ?o .\n" + 
//				"?o <http://dbpedia.org/ontology/populationTotal> ?o2\n" + 
//				"}";

		try {
			String sIRI = "http://dbpedia.org/property/attivitÃi\"botanico\" @it";
			sIRI = sIRI.replaceAll(" ", "");
//			IRIFactory iriFactory = IRIFactory.iriImplementation();
//			IRI iri = iriFactory.create(sIRI);
			String rExp = "/^[a-z](?:[-a-z0-9\\+\\.])*:(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?(?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*)(?::[0-9]*)?(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?$/i";
			System.out.println(sIRI.trim().replaceAll(rExp, ""));
		} catch (Exception e) {
			e.printStackTrace();
		}

//		Set<String> res = execQuery(query);
//		long totalTime = System.currentTimeMillis() - start;
//		File f = writeNtFile(res, "retTriplesLODaLOT.nt");
//		System.out.println("File generated: " + f.getAbsolutePath());
//		System.out.println("TotalTime: " + totalTime);
	}

	public static Set<String> execQuery(String qSparql) throws IOException {
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
		Util.updateCount(Approach.SPARQL_A_LOT, ret.size());

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
}
