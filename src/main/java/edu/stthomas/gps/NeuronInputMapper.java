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
		
		// Channel connections. Key is source type, Value is destination types.
		Map<String, ArrayList<String>> channel_conns;
		
		// Mapping neuron type to its range.
		Map<String, int[]> type_ranges;
		
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
			
			for (int i = this.start_id_channel1; i <= this.end_id_channel1; ++i) {
				neuron = this.getNeuronWritable(1, parameters);
				output.set(neuron.toString());
				context.write(NullWritable.get(), output);
			}
		} catch (Exception e) {
			output.set(e.toString());
			context.write(NullWritable.get(), output);
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
	private Map<String, ArrayList<String>> getChannelConnections( Element global ) {
		Map<String, ArrayList<String>> channel_conns = new HashMap<String, ArrayList<String>>();
		
		NodeList conns = global.getElementsByTagName("channel_connections");
		for (int i = 0; i < conns.getLength(); ++i) {
			Element conn = (Element) conns.item(i);
			String source = conn.getAttribute("from");
			ArrayList<String> targets = new ArrayList<String>();
			
			NodeList tos = conn.getElementsByTagName("to");
			for (int j = 0; j < tos.getLength(); ++j) {
				Element to = (Element) tos.item(j);
				targets.add(to.getAttribute("type"));
			}
			
			channel_conns.put(source, targets);
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
