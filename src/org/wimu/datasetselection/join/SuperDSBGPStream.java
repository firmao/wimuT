package org.wimu.datasetselection.join;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.tdb.TDBFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class SuperDSBGPStream {
	public static Set<Triple> setTriples = new HashSet<Triple>();
	public static final long lim = 30000000;
	public static final String TDBdir = "tdb/wimuTdb";
	// Dataset dsNewTDB = TDBFactory.createDataset("tdb/wimuTdb");

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
					if (Util.isEndPoint(ds)) {
						for (String sparql : bgpSparql) {
							Set<String> reTriples = Util.triplifyResult(Util.execQueryEndPoint(sparql, ds), sparql);
							try {
								insertTDBString(reTriples, TDBdir);
							} catch (Exception e) {
								e.printStackTrace();
							}
							reTriples.clear();
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
							// SelectBuilder queryBuider = new SelectBuilder();

							String s = "";
							String p = "";
							String o = "";
							String s1 = "";
							String p1 = "";
							String o1 = "";

							if (bgp.getSubject().isVariable()) {
								// queryBuider.addVar(bgp.getSubject().toString());
								s = bgp.getSubject().toString();
								s1 = bgp.getSubject().toString();
							} else {
								// queryBuider.addVar("vSubject");
								s = "?vSubject";
								s1 = "<" + bgp.getSubject().toString() + ">";
							}
							if (bgp.getPredicate().isVariable()) {
								p = bgp.getPredicate().toString();
								p1 = bgp.getPredicate().toString();
								// queryBuider.addVar(bgp.getPredicate().toString());
							} else {
								// queryBuider.addVar("vPredicate=<http://uri.com/br>");
								p = "?vPredicate";
								p1 = "<" + bgp.getPredicate().toString() + ">";
							}
							if (bgp.getObject().isVariable()) {
								o = bgp.getObject().toString();
								o1 = bgp.getObject().toString();
								// queryBuider.addVar(bgp.getObject().toString());
							} else {
								// queryBuider.addVar("vObject=<http://uri.com/br>");
								o = "?vObject";
								o1 = "<" + bgp.getObject().toString() + ">";
							}

							String sparql = "Select " + s + " " + p + " " + o + " Where " + "{" + s1 + " " + p1 + " "
									+ o1 + " }";

							// queryBuider.addWhere(bgp.getSubject(), bgp.getPredicate(), bgp.getObject());

							// Query query = queryBuider.build();
							QueryExecution qe = QueryExecutionFactory.create(sparql, m);
							ResultSet results = qe.execSelect();

							// List<QuerySolution> lQuerySolution = ResultSetFormatter.toList(results);
							while (results.hasNext()) {
								QuerySolution qSolution = results.next();
								final StringBuffer sb = new StringBuffer();
								for (final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext();) {
									final String varName = varNames.next();
									sb.append("<" + qSolution.get(varName).toString() + "> ");
								}
								if (s1.startsWith("<")) {
									sb.append(s1 + " ");
								}
								if (p1.startsWith("<")) {
									sb.append(p1 + " ");
								}
								if (o1.startsWith("<")) {
									sb.append(o1 + " ");
								}
								sb.append(".");
								triples.add(sb.toString());
								if (triples.size() > lim) {
									long startTime = System.currentTimeMillis();
									System.out.println("HDT: Reach the limit: " + lim + " Inserting into TDB");
									try {
										insertTDBString(triples, TDBdir);
									} catch (Exception e) {
										e.printStackTrace();
									}
									long totalT = System.currentTimeMillis() - startTime;
									System.out.println(
											"Inserting TDB " + setTriples.size() + " triples, in totalTime: " + totalT);
									triples.clear();
									triples = new HashSet<String>();
								}
							}
							qe.close();
						}

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
						List<Triple> bgpTriple = new BGPSplit().getBGPTriple(cSparql);
						includeRDFRDB(file, bgpTriple);
//						
					}

					// System.out.println("modelNewTDB.size(): " + modelNewTDB.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try

		{
			ret.addAll(executeQueryTDB(cSparql, TDBdir));
			System.out.println("Printing " + ret.size() + " results:");
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	private static Set<String> executeQueryTDB(String cSparql, String tdbDir) {
		System.out.println("Executing quering on TDB: " + tdbDir);
		Set<String> ret = new HashSet<String>();
		Dataset dataset = TDBFactory.createDataset(tdbDir);
		
		dataset.begin(ReadWrite.READ);
		//Model model = dataset.getDefaultModel();
		Query query = QueryFactory.create(cSparql) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
            	QuerySolution triple = results.next();
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
          } finally { qexec.close() ; }
		dataset.end();

		return ret;
	}

	private static void insertTDBString(Set<String> reTriples, String tdbDir) {
		Dataset dsNewTDB = TDBFactory.createDataset(tdbDir);
		dsNewTDB.begin(ReadWrite.WRITE);
		Model modelNewTDB = dsNewTDB.getDefaultModel();
		Graph graph = modelNewTDB.getGraph();
		for (String triple : reTriples) {
			String str[] = triple.split(" ");
			String s = str[0].trim().replaceAll(" ", "");
			String p = str[1].trim().replaceAll(" ", "");
			String o = str[2].trim().replaceAll(" ", "");
			graph.add(new Triple(NodeFactory.createURI(s), NodeFactory.createURI(p), NodeFactory.createURI(o)));
		}
		dsNewTDB.commit();
		dsNewTDB.end();
	}

	private static void includeRDFRDB(File fUnzip, List<Triple> bgpTriple) {
		for (Triple triple : bgpTriple) {

		}
		try {
			StreamRDF reader = new StreamRDF() {

				@Override
				public void base(String arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void finish() {
					// TODO Auto-generated method stub

				}

				@Override
				public void prefix(String arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void quad(Quad arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void start() {
					// TODO Auto-generated method stub

				}

				@Override
				public void triple(Triple triple) {
					for (Triple bgpTriple : bgpTriple) {
						if (containsMatch(triple, bgpTriple)) {
							setTriples.add(triple);
							if (setTriples.size() > lim) {
								long startTime = System.currentTimeMillis();
								System.out.println("Reach the limit: " + lim + " Inserting to TDB");
								try {
									insertTDB(setTriples, TDBdir);
								} catch (Exception e) {
									e.printStackTrace();
								}
								long totalT = System.currentTimeMillis() - startTime;
								System.out.println(
										"Inserting TDB " + setTriples.size() + " triples, in totalTime: " + totalT);
								setTriples.clear();
								setTriples = new HashSet<Triple>();
							}
						}
					}
				}

				private void insertTDB(Set<Triple> setTriples, String tdbDir) {
					Dataset dsNewTDB = TDBFactory.createDataset(tdbDir);
					dsNewTDB.begin(ReadWrite.WRITE);
					Model modelNewTDB = dsNewTDB.getDefaultModel();
					Graph graph = modelNewTDB.getGraph();
					for (Triple triple : bgpTriple) {
						graph.add(triple);
					}
					dsNewTDB.commit();
					dsNewTDB.end();
				}

				private boolean containsMatch(Triple triple, Triple bgpTriple) {
					if (triple.getSubject().toString().equals(bgpTriple.getSubject().toString())) {
						return true;
					}
					if (triple.getSubject().toString().equals(bgpTriple.getPredicate().toString())) {
						return true;
					}
					if (triple.getSubject().toString().equals(bgpTriple.getObject().toString())) {
						return true;
					}

					if (triple.getPredicate().toString().equals(bgpTriple.getSubject().toString())) {
						return true;
					}
					if (triple.getPredicate().toString().equals(bgpTriple.getPredicate().toString())) {
						return true;
					}
					if (triple.getPredicate().toString().equals(bgpTriple.getObject().toString())) {
						return true;
					}

					if (triple.getObject().toString().equals(bgpTriple.getSubject().toString())) {
						return true;
					}
					if (triple.getObject().toString().equals(bgpTriple.getPredicate().toString())) {
						return true;
					}
					if (triple.getObject().toString().equals(bgpTriple.getObject().toString())) {
						return true;
					}
					return false;
				}
			};
			RDFParserBuilder a = RDFParserBuilder.create();

			if (fUnzip.getName().endsWith(".tql")) {
//				RDFDataMgr.parse(reader, fUnzip.getAbsolutePath(), Lang.NQUADS);
				a.forceLang(Lang.NQUADS);
			} else if (fUnzip.getName().endsWith(".ttl")) {
//				RDFDataMgr.parse(reader, fUnzip.getAbsolutePath(), Lang.TTL);
				a.forceLang(Lang.TTL);
			} else {
//				RDFDataMgr.parse(reader, fUnzip.getAbsolutePath());
				a.forceLang(Lang.RDFXML);
			}
			Scanner in = null;
			try {
				in = new Scanner(fUnzip);
				while (in.hasNextLine()) {
					a.source(new StringReader(in.nextLine()));
					try {
						a.parse(reader);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
				in.close();
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
			}
			fUnzip.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
