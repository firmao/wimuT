package org.wimu.datasetselection.bgploadalot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WimuTQuery {
	long timeWimu, timeSquin;
	boolean hasResultsWimu, hasResultsSquin;
	String query, datasetWimu;
	String results, resultSquin;
	boolean resultLODaLOT, resultDBpedia, resultsFromSquin, timeoutError;
	Map<String, Set<String>> datasets;
	Map<String, Integer> mapAppResults = new HashMap<String, Integer>();
	
	public boolean isResultsFromSquin() {
		return resultsFromSquin;
	}
	public void setResultsFromSquin(boolean resultsFromSquin) {
		this.resultsFromSquin = resultsFromSquin;
	}
	public boolean isTimeoutError() {
		return timeoutError;
	}
	public void setTimeoutError(boolean timeoutError) {
		this.timeoutError = timeoutError;
	}
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

	public Map<String, Set<String>> getDatasets() {
		return datasets;
	}

	public void setDatasets(Map<String, Set<String>> datasets) {
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
		
		for (String uri : datasets.keySet()) {
			for (String ds : datasets.get(uri)) {
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
		}
		return ret;
	}
	public Map<String, Integer> getMapAppResults() {
		return this.mapAppResults;
	}
	public void setMapAppResults(Map<String, Integer> mapAppResults) {
		this.mapAppResults.putAll(mapAppResults);
		Util.mAppRes.clear();
	}
}
