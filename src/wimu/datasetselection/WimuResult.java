package org.wimu.datasetselection;

import java.util.Map;

import org.apache.jena.query.ResultSet;


public class WimuResult {
	long size;
	String bestDataset, query;
	String result;
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
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
