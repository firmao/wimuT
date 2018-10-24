package org.wimu.datasetselection.join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;

public class BGPSplit extends OpVisitorBase {
	final List<Triple> triples = new ArrayList<Triple>();
	public static void main(String[] args) {
		String cSparql = "SELECT ?president ?party ?page WHERE {\n"
				+ "   ?president <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/President> .\n"
				+ "   ?president <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/United_States> .\n"
				+ "   ?president <http://dbpedia.org/ontology/party> ?party .\n"
				+ "   ?x <http://data.nytimes.com/elements/topicPage> ?page .\n"
				+ "   ?x <http://www.w3.org/2002/07/owl#sameAs> ?president .\n" + "}";

		BGPSplit bgps = new BGPSplit();
		List<Triple> splitSparql = bgps.getSplitSparql(cSparql);
		for (Triple bgp : splitSparql) {
			System.out.println("+++" + bgp);
		}
	}

	@Override
	public void visit(final OpBGP opBGP) {
		triples.addAll(opBGP.getPattern().getList());
	}

	public List<Triple> getSplitSparql(String cSparql) {
		Query query = QueryFactory.create(cSparql);
		Op op = Algebra.compile(query);
		OpWalker.walk(op, this);
		return triples;
	}

}
