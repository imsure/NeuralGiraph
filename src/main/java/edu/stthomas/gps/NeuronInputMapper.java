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
 * The Mapper class is used to generate input data for 
 * a neuron network based on the metadata provided.
 *
 */
public class NeuronInputMapper extends Mapper<LongWritable, Text, NullWritable, Text>
{
	private Text output = new Text();
	private Random randn = new Random();
	
	private int start_id_channel1;
	private int end_id_channel1;
	private int total_in_onechannel; // total number of neurons in one channel
	private int num_channels; // number of channels
	private String type; // the current type neuron the map is processing
	
    /* diffuse projections listed in Table 1B, spanned all 
       channels and the connection probability was divided 
       among each of those" - Thibeault & Srinivasa, 2013 */
	private float StnDiffuseProb;
	private final float DiffuseWeight = (float) 0.35;
	
	@Override
	public void setup(Context context) {
	}

	/**
	 * Each map method takes a line of metadata about a specific type of neuron.
	 * The meta data is define as:
	 * [start id (channel 1), end id (channel 1), total number of neurons in one channel, 
	 * number of channel, neuron type, {connections:neuron type:probabilty of connection~weight}]
	 * 
	 * Examples like:
	 * 1,800,2579,2,ce,ce:1~0.5,ci:1~0.5,stn:0.5~0.5,strd1:0.5~0.5,strd2:0.5~0.5
	 * 801,1000,2579,2,ci,ce:1~-1,ci:1~-1
	 */
	@Override
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {

		// store connection information about the current type of neuron to its outgoing neurons. 
		// key: type of the outgoing neurons 
		// value: probability and weight of the connection.
		HashMap<String, String> connections = new HashMap<String, String>();

		String[] fields = value.toString().split(",");

		total_in_onechannel = Integer.parseInt(fields[2]); // total number of neurons in one channel
		num_channels = Integer.parseInt(fields[3]); // number of channels
		start_id_channel1 = Integer.parseInt(fields[0]) ;
		end_id_channel1 = Integer.parseInt(fields[1]);
		type = fields[4]; // the current type neuron the map is processing

		this.StnDiffuseProb = (float) 0.5 / num_channels;
		
		/* 
		 * Build a map between type of neuron and probability and weight of the connection.
		 */
		if (fields.length > 5) {
			for (int i = 5; i < fields.length; ++i) {
				String[] elems = fields[i].split(":");
				// System.err.println(type+":"+elems[0]);
				connections.put(elems[0], elems[1]);
			}
		}

		/*
		 * Build a neural network that consist multiple channels.
		 * 
		 * In each channel, iterate through start neuron id to end neuron id to build the input data
		 * for each neuron.
		 */

		for (int channel = 1; channel <= num_channels; ++channel) {
			// start ID of the current type of neuron for the given 'channel'
			int start = start_id_channel1 + total_in_onechannel * (channel - 1); 
			// end ID of the current type of neuron for the given 'channel'
			int end = end_id_channel1 + total_in_onechannel * (channel - 1); 
			
			for (int i = start; i <= end; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(i).append(";"); // neuron ID
				NeuronWritable neuron = generateNeuron(type);
				sb.append(neuron.toString()).append(";");

				/*
				 * Go through outgoing nodes, create edges from neuron 'i' to neuron 'j', that is,
				 * synaptic weights that neuron 'i' have to neuron 'j'.
				 */
				for (Map.Entry<String, String> entry : connections.entrySet()) {
					String outgoing_neuron_type = entry.getKey();
					String prob_weight = entry.getValue();

					buildConnection(outgoing_neuron_type, prob_weight, sb, channel);
				}

				output.set(sb.toString().substring(0, sb.length()-1)); // remove the trailing ','
				context.write(NullWritable.get(), output);
			}
		}
	}

	/**
	 * Given a neuron type, return the range of its ID.
	 * It is ugly now because we have to hardcode the range here, but we will
	 * find a way to express it in the input data finally.  
	 * 
	 * @param type
	 * @param channel
	 * @return range of ID
	 */
	private int[] getRangeByTypeAndChannel(String type, int channel) {
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
			range[1] = 1031;
		} else if (type.equals("strd1")) {
			range[0] = 1032;
			range[1] = 1989;
		} else if (type.equals("strd2")) {
			range[0] = 1990;
			range[1] = 2468;
		} else if (type.equals("gpe")) {
			range[0] = 2469;
			range[1] = 2528;
		} else if (type.equals("gpi")) {
			range[0] = 2529;
			range[1] = 2572;
		}

		range[0] += (channel - 1) * this.total_in_onechannel;
		range[1] += (channel - 1) * this.total_in_onechannel;

		return range;
	}

	/**
	 * Build connections from the current type of neurons to its outgoing neighbors and
	 * build connections from the current channel to other channels (only if the type of neuron
	 * is 'stn'). 
	 * 
	 * @param outgoing_type outgoing neuron type
	 * @param prob_weight connection probability and synaptic weight
	 * @param sb StringBuilder used to hold the connections
	 * @param channel the current channel
	 */
	private void buildConnection(String outgoing_type, String prob_weight, 
			StringBuilder sb, int current_channel) {
		int[] range = getRangeByTypeAndChannel(outgoing_type, current_channel);
		String[] elems = prob_weight.split("~");
		float prob = Float.parseFloat(elems[0]);
		float weight = Float.parseFloat(elems[1]);

		// Build connections inside the given channel
		for (int j = range[0]; j <= range[1]; j++) {
			if (randn.nextFloat() <= prob) {
				String edge = j+":"+String.format("%.2f",weight*randn.nextFloat());
				sb.append(edge).append(',');
			}
		}
		
		// Build connection between channels only when current
		// neuron type is 'stn'. connection is from 'stn' in the current channel
		// to 'gpe' and 'gpi' of all other channels.
		if (this.type.equals("stn")) {
			for (int channel = 1; channel <= this.num_channels; ++channel) {
				if (channel != current_channel) { // ignore the current channel
					
					/* connections to gpe */
					int[] range_gpe = getRangeByTypeAndChannel("gpe", channel);
					for (int j = range_gpe[0]; j <= range_gpe[1]; j++) {
						if (randn.nextFloat() <= this.StnDiffuseProb) {
							String edge = j+":"+String.format("%.2f", 
									this.DiffuseWeight*randn.nextFloat());
							sb.append(edge).append(',');
						}
					}
					
					/* connections to gpi */
					int[] range_gpi = getRangeByTypeAndChannel("gpi", channel);
					for (int j = range_gpi[0]; j <= range_gpi[1]; j++) {
						if (randn.nextFloat() <= this.StnDiffuseProb) {
							String edge = j+":"+String.format("%.2f", 
									this.DiffuseWeight*randn.nextFloat());
							sb.append(edge).append(',');
						}
					}
				}
			}
		}
	}

	/**
	 * Generate a neuron (parameters and its internal state).
	 * 
	 * @param type neuron type
	 * @return an instance of NeuronWritable
	 */
	private NeuronWritable generateNeuron(String type) {
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
			neuron.param_d = 8 - 6 * randf2 * randf2;
		} else if (type.equals("ci")) {
			neuron.param_a = (float) (0.2 + 0.08 * randf1);
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
			neuron.param_a = (float) 0.01 + (float) 0.01 * randf1;
			neuron.param_b = (float) 0.275 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 2;
		} else if (type.equals("strd2")) {
			neuron.param_a = (float) 0.01 + (float) 0.01 * randf1;
			neuron.param_b = (float) 0.275 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 2;
		} else if (type.equals("gpe")) {
			neuron.param_a = (float) 0.005 + (float) 0.001 * randf1;
			neuron.param_b = (float) 0.585 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 4;
		} else if (type.equals("gpi")) {
			neuron.param_a = (float) 0.005 + (float) 0.001 * randf1;
			neuron.param_b = (float) 0.32 - (float) 0.05 * randf2;
			neuron.param_c = -65;
			neuron.param_d = 2;
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
