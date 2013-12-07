package edu.stthomas.gps;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class FiringPattern extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		/*
		 * The expected command-line arguments are the paths containing
		 * input and output data. Terminate the job if the number of
		 * command-line arguments is not exactly 2.
		 */
		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n",
					getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(-1);
		}
		
		Job job = new Job(getConf());
		job.setJarByClass(FiringPattern.class);
		job.setJobName("Firing Pattern");
		// Change the default input format class to KeyValueTextInputFormat since
		// the second job takes the output of the Giraph job as its input.
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setMapperClass(FiringPatternMapper.class);
		job.setReducerClass(FiringPatternReducer.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		
		job.setNumReduceTasks(1);
		
		return job.waitForCompletion(true) ? 0 : 1;
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new FiringPattern(), args);
		System.exit(res);
	}
}
