package edu.stthomas.gps;

import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Driver class of MapReduce job for generating the neural network.
 * 
 * @author imsure
 *
 */
public class NeuronInput extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", 
					getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(-1);
		}
		
		String input = args[0];
		String output = args[1];
		
		Job job = new Job(getConf());
		
		job.setJarByClass(this.getClass());
		job.setJobName("Neural Network Generation for Giraph");
		
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
		
		job.setInputFormatClass(WholeFileInputFormat.class);
		
		job.setMapperClass(NeuronInputMapper.class);
		job.setNumReduceTasks(0); // This is a mapper only job.
				
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : -1;
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int res = ToolRunner.run(conf, new NeuronInput(), args);
		System.exit(res);
	}
}
