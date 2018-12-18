package org.wimu.datasetselection.parallelv1;

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
		List<Triple> splitSparql = bgps.getBGPTriple(cSparql);
		System.out.println(cSparql);
		for (Triple bgp : splitSparql) {
			String s = bgp.getSubject().toString();
			String p = bgp.getPredicate().toString();
			String o = bgp.getObject().toString();
			System.out.println(s +" " + p + " " + o);
		}
	}
	
	public static Set<String> getBGPsparql(String cSparql) {
		Set<String> ret = new HashSet<String>();
		BGPSplit bgps = new BGPSplit();
		List<Triple> splitSparql = bgps.getBGPTriple(cSparql);
		//System.out.println(cSparql);
		for (Triple bgp : splitSparql) {
			String s = "";
			String p = "";
			String o = "";
			String s1 = "";
			String p1 = "";
			String o1 = "";
			
			if(bgp.getSubject().isVariable()) {
				s = bgp.getSubject().toString();
				s1 = bgp.getSubject().toString();
			} else {	
				s = "?vSubject";
				s1 = "<" + bgp.getSubject().toString() + ">";
			}
			if(bgp.getPredicate().isVariable()) {
				p = bgp.getPredicate().toString();
				p1 = bgp.getPredicate().toString();
			} else {
				p = "?vPredicate";
				p1 = "<" + bgp.getPredicate().toString() + ">";
			}
			if(bgp.getObject().isVariable()) {
				o = bgp.getObject().toString();
				o1 = bgp.getObject().toString();
			} else {
				o = "?vObject";
				o1 = "<" + bgp.getObject().toString() + ">";
			}
			
			String sparql = "Select " + s + " " + p + " " + o + " Where "
					+ "{" + s1 + " " + p1 + " " + o1 + " }";
			
			ret.add(sparql);
		}
		return ret;
	}

	@Override
	public void visit(final OpBGP opBGP) {
		triples.addAll(opBGP.getPattern().getList());
	}

	public List<Triple> getBGPTriple(String cSparql) {
		Query query = QueryFactory.create(cSparql);
		Op op = Algebra.compile(query);
		OpWalker.walk(op, this);
		return triples;
	}

}