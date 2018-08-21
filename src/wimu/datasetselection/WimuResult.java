package org.wimu.datasetselection;

import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


public class WimuResult {
	long size;
	String bestDataset, query;
	org.apache.jena.query.ResultSet result;
	Map<String, String> datasets;
	
	public Map<String, String> getDatasets() {
		return datasets;
	}
	public void setDatasets(Map<String, String> datasets) {
		this.datasets = datasets;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getBestDataset() {
		return bestDataset;
	}
	public void setBestDataset(String bestDataset) {
		this.bestDataset = bestDataset;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public org.apache.jena.query.ResultSet getResult() {
		return result;
	}
	public void setResult(org.apache.jena.query.ResultSet result) {
		this.result = result;
	}
	public void printResults() {
		System.out.println(org.apache.jena.query.ResultSetFormatter.asText(this.result));
	}
}
