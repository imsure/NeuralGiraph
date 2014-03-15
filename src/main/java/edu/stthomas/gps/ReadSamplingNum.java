package edu.stthomas.gps;

import java.io.*;
import java.util.*;
import org.apache.hadoop.io.IOUtils;

/**
 * This class takes input from Hadoop's distributed cache in which
 * there are 1000 numbers randomly generated from 1 ~ 100,000 and store
 * them into a hash map for use.
 * 
 * @author imsure
 *
 */
public class ReadSamplingNum {

	private HashMap<Integer, String> samplings = new HashMap<Integer, String>();

	public void init(File file) throws IOException {

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = in.readLine()) != null) {
				samplings.put(Integer.parseInt(line), null);
			}
		} finally {
			IOUtils.closeStream(in);
		}
	}
	
	public HashMap<Integer, String> getSampleIDs() {
		return this.samplings;
	}
}
