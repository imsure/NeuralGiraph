package edu.stthomas.gps;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Output4HiveReducer 
extends Reducer<Text, Text, NullWritable, Text> {

	private Text record = new Text();
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		for (Text value : values) {
			record.set(key.toString()+";"+value.toString());
			context.write(NullWritable.get(), record);
		}
	}
}
