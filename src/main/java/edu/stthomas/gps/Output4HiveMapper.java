package edu.stthomas.gps;

import java.io.*;
import java.util.HashMap;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Output4HiveMapper 
extends Mapper<Text, Text, Text, Text> {

	private ReadSamplingNum sample = new ReadSamplingNum();
	private HashMap<Integer, String> sampledIDs = new HashMap<Integer, String>();
	
	@Override
	public void setup(Context context) 
			throws IOException, InterruptedException {
		sample.init(new File("random_1000.txt"));
		sampledIDs = sample.getSampleIDs();
	}
	
	@Override
	public void map(Text key, Text value, Context context) 
			throws IOException, InterruptedException {
		int id = Integer.parseInt(key.toString());
		
		if (this.sampledIDs.containsKey(id)) {
			context.write(key, value);
		}
	}
}
