package org.wimu.datasetselection.bgploadalot;

import java.util.Map;
import java.util.Set;

public class WimuResult {
	long size;
	String bestDataset, query;
	String result;
	Map<String, Set<String>> datasets;
	boolean resultLODaLOT, resultDBpedia;
	
	public boolean isResultLODaLOT() {
		return resultLODaLOT;
	}
	public void setResultLODaLOT(boolean resultLODaLOT) {
		this.resultLODaLOT = resultLODaLOT;
	}
	public boolean isResultDBpedia() {
		return resultDBpedia;
	}
	public void setResultDBpedia(boolean resultDBpedia) {
		this.resultDBpedia = resultDBpedia;
	}
	public Map<String, Set<String>> getDatasets() {
		return datasets;
	}
	public void setDatasets(Map<String, Set<String>> datasets) {
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
	public void setAll(WimuResult wRes) {
		this.setBestDataset(wRes.getBestDataset());
		this.setResult(wRes.getResult());
		this.setDatasets(wRes.getDatasets());
		this.setSize(wRes.getSize());
		this.setQuery(wRes.getQuery());
		this.setResultLODaLOT(wRes.isResultLODaLOT());
		this.setResultDBpedia(wRes.isResultDBpedia());
	}
}
