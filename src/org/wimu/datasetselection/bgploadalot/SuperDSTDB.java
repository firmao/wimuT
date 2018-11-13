package org.wimu.datasetselection.bgploadalot;

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
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.tdb.TDBFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class SuperDSTDB {

	public static Set<String> executeQuery(Map<String, Set<String>> mUDataset, String cSparql) {
		Set<String> ret = new HashSet<String>();
		Dataset dsNewTDB = TDBFactory.createDataset("tdb/wimuTdb");
		dsNewTDB.begin(ReadWrite.WRITE);
		Model modelNewTDB = dsNewTDB.getDefaultModel();
		for (String uri : mUDataset.keySet()) {
			for (String ds : mUDataset.get(uri)) {
				try {
					System.out.println("dataset: " + ds);
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
						modelNewTDB.add(m);
						if (hdt != null) {
							hdt.close();
						}
					} else {
						Dataset dTDB = TDBFactory.assembleDataset(file.getAbsolutePath());
						dTDB.begin(ReadWrite.READ);
						m = dTDB.getDefaultModel();
						modelNewTDB.add(m);
						dTDB.end();
					}

					// System.out.println("modelNewTDB.size(): " + modelNewTDB.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		dsNewTDB.commit();
		System.out.println("query the modelNewTDB");
		try {
			QueryExecution qe = QueryExecutionFactory.create(cSparql, modelNewTDB);
			ResultSet resultSet = qe.execSelect();
			if (resultSet != null) {
				List<QuerySolution> lQuerySolution = ResultSetFormatter.toList(resultSet);
				for (QuerySolution qSolution : lQuerySolution) {
					final StringBuffer sb = new StringBuffer();
					for (final Iterator<String> varNames = qSolution.varNames(); varNames.hasNext();) {
						final String varName = varNames.next();
						sb.append(qSolution.get(varName).toString() + " ");
					}
					ret.add(sb.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dsNewTDB.end();
		}

		return ret;
	}
}
