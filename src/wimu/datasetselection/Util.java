package org.wimu.datasetselection;

import java.util.HashSet;
import java.util.Set;

public class Util {
	public static Set<String> getQueries(){
		Set<String> ret = new HashSet<String>();
		ret.add("PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
				"PREFIX dbp: <http://dbpedia.org/resource/>\n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
				"\n" +
				"SELECT ?name ?bandname where {\n" +
				"?person foaf:name ?name .\n" +
				"?band dbo:bandMember ?person .\n" +
				"?band dbo:genre dbp:Punk_rock .\n" +
				"?band foaf:name ?bandname .}");
		ret.add("PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
				"PREFIX dbp: <http://dbpedia.org/resource/>\n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
				"SELECT ?name ?bandname where {?person foaf:name ?name . " +
				"?band dbo:bandMember ?person . " +
				"?band dbo:genre dbp:Punk_rock . " +
				"?band foaf:name ?bandname .}");
//		ret.add("SELECT ?uri ?id WHERE {?uri <http://dbpedia.org/ontology/wikiPageID> ?id. FILTER (?uri = <http://dbpedia.org/resource/Weight_gain>) }");
//		ret.add("SELECT ?p ?o { <http://nasa.dataincubator.org/spacecraft/1968-089A> ?p ?o}");
//		ret.add("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//			    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//			    "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
//			    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
//			    "PREFIX swrc: <http://swrc.ontoware.org/ontology#>\n" +
//			    "PREFIX swc:  <http://data.semanticweb.org/ns/swc/ontology#>\n" +
//			    "\n" +
//			    "SELECT DISTINCT ?author ?phone\n" +
//			    "WHERE {\n" +
//			    "  ?pub swc:isPartOf <http://data.semanticweb.org/conference/eswc/2009/proceedings> .\n" +
//			    "  ?pub swc:hasTopic ?topic .\n" +
//			    "  ?topic rdfs:label ?topicLabel .\n" +
//			    "  FILTER regex( str(?topicLabel), \"ontology engineering\", \"i\" ) .\n" +
//			    "\n" +
//			    "  ?pub swrc:author ?author .\n" +
//			    "  { ?author owl:sameAs ?authorAlt } UNION { ?authorAlt owl:sameAs ?author }\n" +
//			    "\n" +
//			    "  ?authorAlt foaf:phone ?phone\n" +
//			    "}");
//		ret.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//			    "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//			    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
//			    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
//			    "PREFIX eurostat: <http://www4.wiwiss.fu-berlin.de/eurostat/resource/eurostat/>\n" +
//			    "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
//			    "\n" +
//			    "SELECT DISTINCT ?cityName ?ur WHERE {\n" +
//			    "  ?u skos:subject <http://dbpedia.org/resource/Category:Universities_and_colleges_in_Lower_Saxony> ;\n" +
//			    "     dbpedia:city ?c .\n" +
//			    "  ?c owl:sameAs [ rdfs:label ?cityName ;\n" +
//			    "                  eurostat:unemployment_rate_total ?ur ]\n" +
//			    "}\n");
//		ret.add("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
//			    "\n" +
//			    "SELECT DISTINCT ?i WHERE {\n" +
//			    "  <http://www.w3.org/People/Berners-Lee/card#i> foaf:knows ?p .\n" +
//			    "  ?p foaf:interest ?i .\n" +
//			    "}");
//		ret.add("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//			    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//			    "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
//			    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
//			    "PREFIX dc:   <http://purl.org/dc/elements/1.1/>\n" +
//			    "PREFIX movie: <http://data.linkedmdb.org/resource/movie/>\n" +
//			    "PREFIX mo: <http://purl.org/ontology/mo/>\n" +
//			    "\n" +
//			    "SELECT DISTINCT ?movieTitle ?image\n" +
//			    "WHERE {\n" +
//			    "  ?movie movie:director <http://data.linkedmdb.org/resource/director/7156> .\n" +
//			    "  ?movie rdfs:label ?movieTitle .\n" +
//			    "\n" +
//			    "  ?movie movie:music_contributor ?music_contributor .\n" +
//			    "  ?music_contributor owl:sameAs ?artist .\n" +
//			    "  ?artist foaf:made ?record .\n" +
//			    "  ?record mo:release_type mo:soundtrack .\n" +
//			    "  ?record dc:title ?recordTitle .\n" +
//			    "  FILTER ( STR(?recordTitle) = STR(?movieTitle) )\n" +
//			    "\n" +
//			    "  { ?record mo:image ?image . }\n" +
//			    "}");
//		ret.add("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//			    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//			    "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
//			    "PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/>\n" +
//			    "PREFIX tcm:      <http://purl.org/net/tcm/tcm.lifescience.ntu.edu.tw/>\n" +
//			    "\n" +
//			    "SELECT DISTINCT ?diseaseLabel ?altMedicineLabel\n" +
//			    "WHERE {\n" +
//			    "  <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB01273>\n" +
//			    "                                 drugbank:possibleDiseaseTarget ?disease.\n" +
//			    "  ?disease owl:sameAs ?sameDisease.\n" +
//			    "\n" +
//			    "  ?altMedicine tcm:treatment ?sameDisease.\n" +
//			    "  ?altMedicine rdf:type tcm:Medicine.\n" +
//			    "  ?sameDisease rdfs:label ?diseaseLabel.\n" +
//			    "  ?altMedicine rdfs:label ?altMedicineLabel.\n" +
//			    "}");
		return ret;
	}
}
