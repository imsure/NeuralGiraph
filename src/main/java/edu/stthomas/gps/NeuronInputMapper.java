package edu.stthomas.gps;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

import org.xml.sax.InputSource;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * The Mapper class is used to generate input data for 
 * a neuron network based on the metadata provided.
 *
 */
public class NeuronInputMapper extends Mapper<NullWritable, Text, NullWritable, Text>
{
	private Text output = new Text();
	private Text filename = new Text();
	private Random randn = new Random();

	private int start_id_channel1;
	private int end_id_channel1;
	private int total_in_onechannel; // total number of neurons in one channel
	private int num_channels; // number of channels
	private String type; // the current type neuron the map is processing
	private float potential; // membrane potential (mv)

	// Channel connections
	private ArrayList<ChannelConnection> channel_conns;

	// Mapping neuron type to its range.
	private Map<String, int[]> type_ranges;

	/* diffuse projections listed in Table 1B, spanned all 
       channels and the connection probability was divided 
       among each of those" - Thibeault & Srinivasa, 2013 */
	private float StnDiffuseProb;
	private float DiffuseWeight = (float) 0.35;

	@Override
	public void setup(Context context) 
			throws IOException, InterruptedException {
		InputSplit split = context.getInputSplit();
		Path path = ((FileSplit) split).getPath();
		this.filename.set(path.toString());
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
	public void map(NullWritable key, Text value, Context context) 
			throws IOException, InterruptedException {

		String xml = value.toString();

		// Mapping neuron type to connection probability & strength
		Map<String, float[]> conns;

		NeuronWritable neuron;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = docFactory.newDocumentBuilder();

			InputSource is = new InputSource(new StringReader(xml));
			Document doc = dBuilder.parse(is);

			Element root = doc.getDocumentElement(); // Extract root element
			root.normalize(); // Normalize the tree merge text nodes.

			/* 
			 * Extract the only one 'neuron' tag
			 */
			Element neuron_tag = (Element) doc.getElementsByTagName("neuron").item(0);
			this.type = neuron_tag.getAttribute("type");
			this.start_id_channel1 = Integer.parseInt( neuron_tag.getAttribute("start_id") );
			this.end_id_channel1 = Integer.parseInt( neuron_tag.getAttribute("end_id") );
			this.potential = Float.parseFloat( neuron_tag.getAttribute("potential") );
			NodeList parameters = neuron_tag.getElementsByTagName("parameter");

			// Get the connections mapping.
			conns = this.getConnenctions(root);

			/*
			 * Extract channel connections and ID range for each type from 'global' tag.
			 */
			Element global = (Element) root.getElementsByTagName("global").item(0);
			this.num_channels = Integer.parseInt( global.getAttribute("channel") );
			this.total_in_onechannel = Integer.parseInt( global.getAttribute("total") );
			channel_conns = this.getChannelConnections(global);
			type_ranges = this.getRanges(global);

			/*
			 * Build up partition of the neural network.
			 * Iterate over channels, inside each channel, iteration over neuron IDs.
			 */
			for (int j = 1; j <= this.num_channels; ++j) {
				int channel = j;
				// ID range for the current 'channel'
				int[] range = this.getRange(this.type, channel); 

				for (int i = range[0]; i <= range[1]; ++i) {
					StringBuilder sb = new StringBuilder();
					sb.append(i).append(';'); // Neuron ID
					neuron = this.getNeuronWritable(channel, parameters);
					sb.append(neuron.toString()).append(';');

					// Construct edges inside the 'channel'
					for (Map.Entry<String, float[]> entry : conns.entrySet()) {
						String target = entry.getKey();
						float conn_prob = entry.getValue()[0];
						float conn_strength = entry.getValue()[1];
						constructEdges( target, conn_prob, conn_strength, channel, sb );
					}
	
					buildChannelConnection( channel, sb );
					
					
					output.set( sb.toString().substring(0, sb.length()-1) ); // remove the trailing ','
					context.write(NullWritable.get(), output);
				}
			}
		} catch (Exception e) {
			output.set(e.toString());
			context.write(NullWritable.get(), output);
		}

	}

	/**
	 * Return the range of for a specific type of neuron in a specific channel.
	 * 
	 * @param type neuron type
	 * @param channel channel number
	 * @return range array, start index: 0, end index: 1
	 */
	private int[] getRange(String type, int channel) {
		int[] range = new int[2];

		int start = this.type_ranges.get(type)[0];
		int end = this.type_ranges.get(type)[1];

		start += (channel - 1) * this.total_in_onechannel;
		end += (channel - 1) * this.total_in_onechannel;

		range[0] = start;
		range[1] = end;

		return range;
	}

	/**
	 * Build connections between channels. (from 'channel' to other channels)
	 * 
	 * @param channel the source channel
	 * @param sb StringBuilder to hold the connections
	 */
	private void buildChannelConnection( int channel, StringBuilder sb ) {
		for (int i = 0; i < this.channel_conns.size(); ++i) {
			ChannelConnection cc = this.channel_conns.get(i);
			
			// check if 'this.type' is a source channel
			if ( this.type.equals(cc.getSourceChannel()) ) { 
				ArrayList<String> targets = cc.getTargetChannels();
				float diffuse_prob = cc.getDiffuseProb();
				float diffuse_weight = cc.getDiffuseWeight();
				
				for (int j = 1; j <= this.num_channels; ++j) {					
					if (j != channel) { // skip the current channel
						for (int k = 0; k < targets.size(); ++k) {
							String target = targets.get( k );
							int[] range = this.getRange( target, j );
							for (int z = range[0]; z <= range[1]; ++z) {
								if (this.randn.nextFloat() <= diffuse_prob) {
									String edge = z + ":" + String.format("%.2f", 
											diffuse_weight*randn.nextFloat());
									sb.append(edge).append(',');
								}
							}
						}
					}
				}
				
				break;
			}
		}
	}
	
	/**
	 * Construct edges from this.type to 'target', in 'channel'.
	 * 
	 * @param target target type
	 * @param prob connection probability
	 * @param strength connection strength
	 * @param sb StringBuilder to hold the edges
	 */
	private void constructEdges( String target, float prob,
			float strength, int channel, StringBuilder sb ) {
		int[] range = this.getRange(target, channel);

		// construct edges inside the given channel
		for (int j = range[0]; j <= range[1]; j++) {
			if (randn.nextFloat() <= prob) {
				String edge = j+":"+String.format("%.2f", strength*randn.nextFloat());
				sb.append(edge).append(',');
			}
		}
	} 

	/**
	 * Return a NeuronWritable given a channel number
	 * 
	 * @param channel the channel number where the neuron is
	 * @param params the parameter list got from xml configuration.
	 * @return a NeuronWritable that holds internal data of a neuron.
	 */
	private NeuronWritable getNeuronWritable( int channel, NodeList params ) {
		NeuronWritable neuron = new NeuronWritable();
		float randf1 = randn.nextFloat();
		float randf2 = randn.nextFloat();
		float a, b, c, d, f1, f2;

		for (int i = 0; i < params.getLength(); ++i) {
			Element param = (Element) params.item(i);
			char name = param.getAttribute("name").charAt(0);
			f1 = Float.parseFloat(param.getAttribute("factor1"));
			f2 = Float.parseFloat(param.getAttribute("factor2"));
			switch (name) {
			case 'a':
				neuron.param_a = f1 + f2 * randf1;
				break;
			case 'b':
				neuron.param_b = f1 - f2 * randf2;
				break;
			case 'c':
				neuron.param_c = f1 + f2 * randf1 * randf1;
				break;
			case 'd':
				neuron.param_d = f1 - f2 * randf2 * randf2;
				break;
			}
		}

		neuron.potential = this.potential;
		neuron.recovery = neuron.potential * neuron.param_b;
		neuron.type.set(this.type);
		neuron.synaptic_sum = 0; // initial synaptic summation: 0
		neuron.fired = 'N'; // initial firing status: Not fired
		neuron.time = 0; // initial time step: 0 ms
		neuron.channel = channel;

		return neuron;
	}

	/**
	 * Extract 'connection' tag from root element.
	 * 
	 * @param root root element of the xml input.
	 * @return connections mapping from neuron type to connection probability & strength.
	 */
	private Map<String, float[]> getConnenctions( Element root ) {
		Map<String, float[]> conns = new HashMap<String, float[]>();

		Element connection = (Element) root.getElementsByTagName("connection").item(0);
		NodeList tos = connection.getElementsByTagName("to");
		for (int i = 0; i < tos.getLength(); ++i) {
			Element to = (Element) tos.item(i);
			String type = to.getAttribute("type");
			float[] prob_strength = new float[2];
			prob_strength[0] = Float.parseFloat(to.getAttribute("probability"));
			prob_strength[1] = Float.parseFloat(to.getAttribute("strength"));

			conns.put(type, prob_strength);
		}

		return conns;
	}

	/**
	 * Extract "connection_channel" tag from 'global' tag.
	 * 
	 * @param global 'global' tag
	 * @return channel connection mapping
	 */
	private ArrayList<ChannelConnection> getChannelConnections( Element global ) {
		ArrayList<ChannelConnection> channel_conns = new ArrayList<ChannelConnection>();

		NodeList conns = global.getElementsByTagName("channel_connection");
		for (int i = 0; i < conns.getLength(); ++i) {
			ChannelConnection cc = new ChannelConnection();
			Element conn = (Element) conns.item(i);

			cc.setSourceChannel( conn.getAttribute("from") );
			cc.setDiffuseProb( Float.parseFloat(conn.getAttribute("diffuse_prob")) );
			cc.setDiffuseWeight( Float.parseFloat(conn.getAttribute("diffuse_weight")) );

			NodeList tos = conn.getElementsByTagName("to");
			for (int j = 0; j < tos.getLength(); ++j) {
				Element to = (Element) tos.item(j);
				cc.addTargetChannel(to.getAttribute("type"));
			}

			channel_conns.add(cc);
		}

		return channel_conns;
	}

	private Map<String, int[]> getRanges( Element global ) {
		Map<String, int[]> type_ranges = new HashMap<String, int[]>();

		NodeList ranges = global.getElementsByTagName("range");
		for (int i = 0; i < ranges.getLength(); ++i) {
			int[] start_end = new int[2];
			Element range = (Element) ranges.item(i);
			start_end[0] = Integer.parseInt( range.getAttribute("start") );
			start_end[1] = Integer.parseInt( range.getAttribute("end") );
			type_ranges.put( range.getAttribute("type"), start_end );
		}

		return type_ranges;
	}
}
