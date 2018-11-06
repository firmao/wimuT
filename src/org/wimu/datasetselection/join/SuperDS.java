package org.wimu.datasetselection.join;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.tdb.TDBFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class SuperDS {
	
	public static void main(String args[]) {
		File file = new File("geo_coordinates_en.ttl");
		String qSparql = "Select * where {?s ?p ?o} limit 5";
		Model model = ModelFactory.createDefaultModel();
		model.read(file.getAbsolutePath(), "N-TRIPLE");
		QueryExecutionBase qe = (QueryExecutionBase) QueryExecutionFactory.create(qSparql, model);
		ResultSet resultSet = qe.execSelect();
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
			System.out.println(sTriple + ".");
		}
	}

	public static Set<String> executeQuery(Map<String, Set<String>> mUDataset, String cSparql) {
		Set<String> ret = new HashSet<String>();
//		Dataset dsNewTDB = TDBFactory.createDataset("tdb/wimuTdb");
//		dsNewTDB.begin(ReadWrite.WRITE);
		// Model modelNewTDB = dsNewTDB.getDefaultModel();
		Set<String> bgpSparql = BGPSplit.getBGPsparql(cSparql);
		Set<String> triples = new HashSet<String>();
		for (String uri : mUDataset.keySet()) {
			for (String ds : mUDataset.get(uri)) {
				try {
					System.out.println("dataset: " + ds);
					if(Util.isEndPoint(ds)) {
						for (String sparql : bgpSparql) {
							Set<String> reTriples = Util.triplifyResult(Util.execQueryEndPoint(sparql, ds), sparql); 
							triples.addAll(reTriples);
						}
						System.out.println("ENDPOINT: " + triples.size());
						continue;
					}
					
					if ((ds != null) && ds.contains("https://hdt.lod.labs.vu.nl")) {
						for (String sparql : bgpSparql) {
							System.out.println("SPARQL_BGP: " + sparql);
							Set<String> reTriples = Util.triplifyResult(QueryLODaLot.execQuery(sparql), sparql); 
							triples.addAll(reTriples);
						}
						System.out.println("LODALOT: " + triples.size());	
						continue;
					}
					
					Model m = null;
					URL url = new URL(ds);
					File file = new File(Util.getURLFileName(url));
					if (!file.exists()) {
						FileUtils.copyURLToFile(url, file);
					}
					file = WimuSelection.unconpress(file);
					if (file.getName().endsWith("hdt")) {
						if (ds.startsWith("http")) {
							url = new URL(ds);
							file = new File(Util.getURLFileName(url));
							if (!file.exists()) {
								FileUtils.copyURLToFile(url, file);
							}
						} else {
							file = new File(ds);
						}
						file = WimuSelection.unconpress(file);
						HDT hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
						HDTGraph graph = new HDTGraph(hdt);
						m = new ModelCom(graph);
						// modelNewTDB.add(m);

						List<Triple> bgpTriple = new BGPSplit().getBGPTriple(cSparql);
						for (Triple bgp : bgpTriple) {
							System.out.println("BGP: " + bgp);
							//SelectBuilder queryBuider = new SelectBuilder();
							
							String s = "";
							String p = "";
							String o = "";
							String s1 = "";
							String p1 = "";
							String o1 = "";
							
							if(bgp.getSubject().isVariable()) {
								//queryBuider.addVar(bgp.getSubject().toString());
								s = bgp.getSubject().toString();
								s1 = bgp.getSubject().toString();
							} else {
								//queryBuider.addVar("vSubject");	
								s = "?vSubject";
								s1 = "<" + bgp.getSubject().toString() + ">";
							}
							if(bgp.getPredicate().isVariable()) {
								p = bgp.getPredicate().toString();
								p1 = bgp.getPredicate().toString();
								//queryBuider.addVar(bgp.getPredicate().toString());
							} else {
								//queryBuider.addVar("vPredicate=<http://uri.com/br>");
								p = "?vPredicate";
								p1 = "<" + bgp.getPredicate().toString() + ">";
							}
							if(bgp.getObject().isVariable()) {
								o = bgp.getObject().toString();
								o1 = bgp.getObject().toString();
								//queryBuider.addVar(bgp.getObject().toString());
							} else {
								//queryBuider.addVar("vObject=<http://uri.com/br>");
								o = "?vObject";
								o1 = "<" + bgp.getObject().toString() + ">";
							}
							
							String sparql = "Select " + s + " " + p + " " + o + " Where "
									+ "{" + s1 + " " + p1 + " " + o1 + " }";
							
							//queryBuider.addWhere(bgp.getSubject(), bgp.getPredicate(), bgp.getObject());
							
							//Query query = queryBuider.build();
							QueryExecution qe = QueryExecutionFactory.create(sparql, m);
							ResultSet results = qe.execSelect();

							//List<QuerySolution> lQuerySolution = ResultSetFormatter.toList(results);
							while(results.hasNext()) {
								QuerySolution qSolution = results.next();
								final StringBuffer sb = new StringBuffer();
								for (final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext();) {
									final String varName = varNames.next();
									sb.append("<" + qSolution.get(varName).toString() + "> ");
								}
								if(s1.startsWith("<")) {
									sb.append(s1 + " ");
								}
								if(p1.startsWith("<")) {
									sb.append(p1 + " ");
								}
								if(o1.startsWith("<")) {
									sb.append(o1 + " ");
								}
								sb.append(".");
								triples.add(sb.toString());
							}
							qe.close();
							
							System.out.println("HDTTRIPLES: " + triples.size());
//							if(triples.size() > 0) {
//								System.out.println(Util.triplifyResult(triples, sparql));
//							}
						}

						// triples.addAll(Util.executeQueries(bgps, m));
						
						
						
						if (hdt != null) {
							hdt.close();
						}
					} else {
						if (ds.startsWith("http")) {
							url = new URL(ds);
							file = new File(Util.getURLFileName(url));
							if (!file.exists()) {
								FileUtils.copyURLToFile(url, file);
							}
						} else {
							file = new File(ds);
						}
						file = WimuSelection.unconpress(file);
						
//						Dataset dTDB = TDBFactory.assembleDataset(file.getAbsolutePath());
//						if(dTDB == null) {
//							System.out.println("Error File RDF: " + file.getAbsolutePath());
//							continue;
//						}
//						dTDB.begin(ReadWrite.READ);
//						m = dTDB.getDefaultModel();
						
						Model model = ModelFactory.createDefaultModel();
						model.read(file.getAbsolutePath(), "N-TRIPLE");
						
						List<Triple> bgpTriple = new BGPSplit().getBGPTriple(cSparql);
						for (Triple bgp : bgpTriple) {
							System.out.println("BGP: " + bgp);
							//SelectBuilder queryBuider = new SelectBuilder();
							
							String s = "";
							String p = "";
							String o = "";
							String s1 = "";
							String p1 = "";
							String o1 = "";
							
							if(bgp.getSubject().isVariable()) {
								//queryBuider.addVar(bgp.getSubject().toString());
								s = bgp.getSubject().toString();
								s1 = bgp.getSubject().toString();
							} else {
								//queryBuider.addVar("vSubject");	
								s = "?vSubject";
								s1 = "<" + bgp.getSubject().toString() + ">";
							}
							if(bgp.getPredicate().isVariable()) {
								p = bgp.getPredicate().toString();
								p1 = bgp.getPredicate().toString();
								//queryBuider.addVar(bgp.getPredicate().toString());
							} else {
								//queryBuider.addVar("vPredicate=<http://uri.com/br>");
								p = "?vPredicate";
								p1 = "<" + bgp.getPredicate().toString() + ">";
							}
							if(bgp.getObject().isVariable()) {
								o = bgp.getObject().toString();
								o1 = bgp.getObject().toString();
								//queryBuider.addVar(bgp.getObject().toString());
							} else {
								//queryBuider.addVar("vObject=<http://uri.com/br>");
								o = "?vObject";
								o1 = "<" + bgp.getObject().toString() + ">";
							}
							
							String sparql = "Select " + s + " " + p + " " + o + " Where "
									+ "{" + s1 + " " + p1 + " " + o1 + " } limit 1000";
							
							//queryBuider.addWhere(bgp.getSubject(), bgp.getPredicate(), bgp.getObject());
							
							//Query query = queryBuider.build();
							QueryExecution qe = QueryExecutionFactory.create(sparql, model);
							ResultSet results = qe.execSelect();

							//List<QuerySolution> lQuerySolution = ResultSetFormatter.toList(results);
							while(results.hasNext()) {
								QuerySolution qSolution = results.next();
								final StringBuffer sb = new StringBuffer();
								for (final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext();) {
									final String varName = varNames.next();
									sb.append("<" + qSolution.get(varName).toString() + "> ");
								}
								if(s1.startsWith("<")) {
									sb.append(s1 + " ");
								}
								if(p1.startsWith("<")) {
									sb.append(p1 + " ");
								}
								if(o1.startsWith("<")) {
									sb.append(o1 + " ");
								}
								sb.append(".");
								triples.add(sb.toString());
							}
							qe.close();
							System.out.println("RDF normal: " + triples.size());
						}
						//dTDB.end();
					}

					// System.out.println("modelNewTDB.size(): " + modelNewTDB.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			
			Set<String> nTriples = Util.filterValidTriples(triples);
			ret.addAll(QueryLODaLot.executeQueryJena(cSparql, nTriples));
			System.out.println("Printing "+ ret.size() +" results:");
			for (String elem : ret) {
				System.out.println(elem);
			}
			
//			System.out.println("Writing file with " + nTriples.size() + " triples");
//			File file = QueryLODaLot.writeNtFile(nTriples, "cbdLODaLOT.nt");
//			System.out.println("Putting in a TDB file system.");
//			Dataset dTDB = TDBFactory.assembleDataset(file.getAbsolutePath());
//			dTDB.begin(ReadWrite.READ);
//			Model m = dTDB.getDefaultModel();
//			QueryExecution qe = QueryExecutionFactory.create(cSparql, m);
//			ResultSet results = qe.execSelect();
//			while(results.hasNext()) {
//				QuerySolution qSolution = results.next();
//				System.out.println("Reading the results: " + qSolution);
//				final StringBuffer sb = new StringBuffer();
//				for (final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext();) {
//					final String varName = varNames.next();
//					sb.append("<" + qSolution.get(varName).toString() + "> ");
//				}
//				ret.add(sb.toString());
//			}
//			qe.close();
//			file.delete();
//			dTDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
}
