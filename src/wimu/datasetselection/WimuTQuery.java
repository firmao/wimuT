package org.wimu.datasetselection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class WimuTQuery {
	long timeWimu, timeSquin;
	boolean hasResultsWimu, hasResultsSquin;
	String query, datasetWimu;
	String results, resultSquin;
	public String getResults() {
		return results;
	}

	public String getResultSquin() {
		return resultSquin;
	}

	public void setResultSquin(String resultSquin) {
		this.resultSquin = resultSquin;
	}

	public void setResults(String results) {
		this.results = results;
	}

	Map<String, String> datasets;

	public Map<String, String> getDatasets() {
		return datasets;
	}

	public void setDatasets(Map<String, String> datasets) {
		this.datasets = datasets;
	}

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

	public long getSumSizeDatasets() {
		long ret = 0;
		if(datasets == null){
			return ret;
		}
		for (String ds : datasets.values()) {
			try {
				ds = Util.getURLFileName(new URL(ds));
				if (ds != null) {
					File f = new File(ds);
					if (f.exists()) {
						ret += f.length();
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
}
