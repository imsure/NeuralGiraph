package edu.stthomas.gps;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import java.util.*;

/**
 * The Mapper class will generate input data for a neuron network based on the metadata provided.
 *
 */
public class NeuronInputMapper extends Mapper<LongWritable, Text, NullWritable, Text>
{
	private Text output = new Text();
	private Random randn = new Random();
	
	@Override
	public void setup(Context context) {
	}
	
	@Override
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {

		HashMap<String, String> type2prob = new HashMap<String, String>();
		
		String[] fields = value.toString().split(",");
		int start_id = Integer.parseInt(fields[0]);
		int end_id = Integer.parseInt(fields[1]);
		int total = Integer.parseInt(fields[2]);
		String type = fields[3];

		/* 
		 * Build a map between type of neuron and probability and weight of the connection.
		 */
		if (fields.length > 4) {
			for (int i = 4; i < fields.length; ++i) {
				String[] elems = fields[i].split(":");
				System.err.println(type+":"+elems[0]);
				type2prob.put(elems[0], elems[1]);
			}
		}
		
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
			for (Map.Entry<String, String> entry : type2prob.entrySet()) {
				String neuron_type = entry.getKey();
				String prob_weight = entry.getValue();
				
				buildConnection(neuron_type, prob_weight, sb);
			}

			output.set(sb.toString().substring(0, sb.length()-1)); // remove the trailing ','
			context.write(NullWritable.get(), output);
		}
	}

	private int[] getRangeByType(String type) {
		int[] range = new int[2];
		
		if (type.equals("ce")) {
			range[0] = 1;
			range[1] = 800;
		} else if (type.equals("ci")) {
			range[0] = 801;
			range[1] = 1000;
		} else if (type.equals("tc")) {
			range[0] = 1001;
			range[1] = 1011;
		} else if (type.equals("stn")) {
			range[0] = 1012;
			range[1] = 1016;
		} else if (type.equals("strd1")) {
			range[0] = 1017;
			range[1] = 1495;
		} else if (type.equals("strd2")) {
			range[0] = 1496;
			range[1] = 1974;
		} else if (type.equals("gpe")) {
			range[0] = 1975;
			range[1] = 1989;
		} else if (type.equals("gpi")) {
			range[0] = 1990;
			range[1] = 2000;
		}
		
		
		return range;
	}
	
	private void buildConnection(String type, String prob_weight, StringBuilder sb) {
		int[] range = this.getRangeByType(type);
		String[] elems = prob_weight.split("~");
		float prob = Float.parseFloat(elems[0]);
		float weight = Float.parseFloat(elems[1]);
		
		for (int j = range[0]; j <= range[1]; j++) {
			if (randn.nextFloat() <= prob) {
				String edge = j+":"+String.format("%.2f",weight*randn.nextFloat());
				sb.append(edge).append(',');
			}
		}
	}
	
	private NeuronWritable generateKey(String type) {
		NeuronWritable neuron = new NeuronWritable();
		float randf1 = randn.nextFloat();
		float randf2 = randn.nextFloat();

		/*
		 * 4 parameters differ as the type of neuron changes
		 */
		if (type.equals("ce")) {
			neuron.param_a = (float) 0.02;
			neuron.param_b = (float) 0.2;
			neuron.param_c = -65 + 15 * randf1 * randf1;
			neuron.param_d = 2 * randf2 * randf2;
		} else if (type.equals("ci")) {
			neuron.param_a = (float) (0.02 + 0.08 * randf1);
			neuron.param_b = (float) (0.25 - 0.05 * randf2);
			neuron.param_c = -65;
			neuron.param_d = 2;
		} else if (type.equals("tc")) {
			neuron.param_a = (float) 0.002;
			neuron.param_b = (float) 0.25;
			neuron.param_c = -65 + 15 * randf1 * randf1;
			neuron.param_d = (float) 0.05 * randf2 * randf2;
		} else if (type.equals("stn")) {
			neuron.param_a = (float) 0.005;
			neuron.param_b = (float) 0.265;
			neuron.param_c = -65 + 15 * randf1 * randf1;
			neuron.param_d = 2 * randf2 * randf2;
		} else if (type.equals("strd1")) {
			neuron.param_a = (float) 0.02 + (float) 0.08 * randf1;
			neuron.param_b = (float) 0.25 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 8;
		} else if (type.equals("strd2")) {
			neuron.param_a = (float) 0.02 + (float) 0.08 * randf1;
			neuron.param_b = (float) 0.25 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 8;
		} else if (type.equals("gpe")) {
			neuron.param_a = (float) 0.005 + (float) 0.08 * randf1;
			neuron.param_b = (float) 0.585 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 4;
		} else if (type.equals("gpi")) {
			neuron.param_a = (float) 0.005 + (float) 0.08 * randf1;
			neuron.param_b = (float) 0.585 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 4;
		}

		neuron.potential = -65;
		neuron.recovery = neuron.potential * neuron.param_b;

		neuron.type.set(type);
		neuron.synaptic_sum = 0;
		neuron.fired = 'N';
		neuron.time = 0;

		return neuron;
	}
}
