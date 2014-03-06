package edu.stthomas.gps;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Output4HiveMapper 
extends Mapper<Text, Text, Text, Text> {

	@Override
	public void map(Text key, Text value, Context context) 
			throws IOException, InterruptedException {
		context.write(key, value);
	}
}
