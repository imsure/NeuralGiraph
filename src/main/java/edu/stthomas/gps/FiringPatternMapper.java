package edu.stthomas.gps;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class FiringPatternMapper extends Mapper<Text, Text, IntWritable, Text> {
	
	private IntWritable firing_time = new IntWritable();
	
	/** key is neuron ID, value is neuron structure */
	@Override
	public void map(Text key, Text value, Context context)
			throws IOException, InterruptedException {
		String firing_sequence = value.toString().split(";")[9];
		for (int i = 1; i < firing_sequence.length(); i++) {
			if (firing_sequence.charAt(i-1) == 'Y') {
				firing_time.set(i);
				/** Emit firing time and neuron id. */
				context.write(firing_time, key);
			}
		}
	}
}
