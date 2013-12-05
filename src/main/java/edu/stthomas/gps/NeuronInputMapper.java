package edu.stthomas.gps;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * The Mapper class will generate input data for a neuron network based on the metadata provided.
 *
 */
public class NeuronInputMapper extends Mapper<LongWritable, Text, NullWritable, Text>
{
	private Text output = new Text();
	private Random randn = new Random();

	public static final float Excitatory_Prob = (float) 1.0;
	public static final float Inhibitory_Prob = (float) 1.0;

	@Override
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {

		String[] fields = value.toString().split(",");
		int start_id = Integer.parseInt(fields[0]);
		int end_id = Integer.parseInt(fields[1]);
		int total = Integer.parseInt(fields[2]);
		char type = fields[3].charAt(0);

		float eprob = context.getConfiguration().getFloat("EPROB", Excitatory_Prob);
		float iprob = context.getConfiguration().getFloat("IPROB", Inhibitory_Prob);

		/*
		 * Iterate through start neuron id to end neuron id.
		 */
		for (int i = start_id; i <= end_id; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i).append(";");
			NeuronWritable neuron = generateKey(type);
			sb.append(neuron.toString()).append(";");
			
			/*
			 * Go through outgoing nodes, create edges from neuron 'i' to neuron 'j', that is,
			 * synaptic weights that neuron 'i' have to neuron 'j'.
			 */
			if (type == 'e') {
				for (int j = 1; j <= total; j++) {
					if (randn.nextFloat() < eprob) {
						String edge = j+":"+String.format("%.2f", (float)0.5*randn.nextFloat());
						sb.append(edge).append(',');
					}
				}
			} else {
				for (int j = 1; j <= total; j++) {
					if (randn.nextFloat() < iprob) {
						String edge = j+":"+String.format("%.2f",(float)-1*randn.nextFloat());
						sb.append(edge).append(',');
					}
				}
			}

			output.set(sb.toString().substring(0, sb.length()-1));
			context.write(NullWritable.get(), output);
		}
	}

	private NeuronWritable generateKey(char type) {
		NeuronWritable neuron = new NeuronWritable();
		float randf = randn.nextFloat();

		/*
		 * 4 parameters differ as the type of neuron changes
		 */
		if (type == 'e') {
			neuron.param_a = (float) 0.02;
			neuron.param_b = (float) 0.2;
			neuron.param_c = -65 + 15 * randf * randf;
			neuron.param_d = 8 - 6 * randf * randf;
		} else {
			neuron.param_a = (float) (0.02 + 0.08 * randf);
			neuron.param_b = (float) (0.25 - 0.05 * randf);
			neuron.param_c = -65;
			neuron.param_d = 2;
		}

		neuron.potential = -65;
		neuron.recovery = neuron.potential * neuron.param_b;

		neuron.type = type;
		neuron.synaptic_sum = 0;
		neuron.fired = 'N';
		neuron.time = 0;

		return neuron;
	}
}
