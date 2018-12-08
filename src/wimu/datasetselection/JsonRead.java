package org.wimu.datasetselection.parallelv1;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonRead {

	public static void main(String[] args) throws FileNotFoundException {
		String jSonURL = "https://lod-cloud.net/lod-data.json";
		try {
			URL url = new URL(jSonURL);
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(url.openStream());
			} catch (Exception e) {
				Thread.sleep(5000);
				reader = new InputStreamReader(url.openStream());
			}
			Gson gson = new Gson();
			Type t = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> map = gson.fromJson(reader, t);

			double dCount = 0.0;
			int lines = 0;
			for (Entry<String, Object> entry : map.entrySet()) {
				lines++;
				Map<String, Object> m1 = (Map<String, Object>) map.get(entry.getKey());
				//System.out.println(m1.get("triples"));
				String numTriples = m1.get("triples").toString().replaceAll("triples", "");
				numTriples = numTriples.replaceAll(" ", "");
				if(!StringUtils.isNumericSpace(numTriples)) continue;
				if(numTriples.isEmpty()) continue;
				dCount += Double.valueOf(numTriples);
				
			}
			System.out.println("Total triples: " + dCount);
			System.out.println("Lines: " + lines);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
