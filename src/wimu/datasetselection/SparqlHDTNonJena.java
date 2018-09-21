package org.wimu.datasetselection;

import java.io.File;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class SparqlHDTNonJena {

	public static void main(String[] args) {
		try {
			File file = new File("test.hdt");
			HDT hdt = HDTManager.mapHDT(file.getAbsolutePath(), null);
			HDTGraph graph = new HDTGraph(hdt);
			
			ExtendedIterator<Triple> it = graph.find();
			System.out.println("Triples: " + graph.getHDT().getTriples().getNumberOfElements());
			while(it.hasNext()) {
				Triple t = it.next();
				System.out.println("s: " + t.getSubject());
				System.out.println("p: " + t.getPredicate());
				System.out.println("o: " + t.getObject());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
