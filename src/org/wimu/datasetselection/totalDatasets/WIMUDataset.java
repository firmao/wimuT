package org.wimu.datasetselection.totalDatasets;

public class WIMUDataset {
	private String dataset, hdt;

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getHdt() {
		return hdt;
	}

	public void setHdt(String hdt) {
		this.hdt = hdt;
	}
	
	@Override
	public String toString() {
		if(dataset != null){
			return this.dataset;
		}else{
			return this.hdt;
		}
	}
}
