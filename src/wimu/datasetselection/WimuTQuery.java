package org.wimu.datasetselection;

public class WimuTQuery {
	long timeWimu, timeSquin;
	boolean hasResultsWimu, hasResultsSquin;
	String query, datasetWimu;
	public String getDatasetWimu() {
		return datasetWimu;
	}
	public void setDatasetWimu(String datasetWimu) {
		this.datasetWimu = datasetWimu;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public long getTimeWimu() {
		return timeWimu;
	}
	public void setTimeWimu(long timeWimu) {
		this.timeWimu = timeWimu;
	}
	public long getTimeSquin() {
		return timeSquin;
	}
	public void setTimeSquin(long timeSquin) {
		this.timeSquin = timeSquin;
	}
	public boolean hasResultsWimu() {
		return hasResultsWimu;
	}
	public void setHasResultsWimu(boolean hasResultsWimu) {
		this.hasResultsWimu = hasResultsWimu;
	}
	public boolean hasResultsSquin() {
		return hasResultsSquin;
	}
	public void setHasResultsSquin(boolean hasResultsSquin) {
		this.hasResultsSquin = hasResultsSquin;
	}
}
