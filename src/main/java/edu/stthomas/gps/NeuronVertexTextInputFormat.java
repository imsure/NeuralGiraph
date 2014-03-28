package edu.stthomas.gps;

import java.io.IOException;
import java.util.*;

import org.apache.giraph.conf.ImmutableClassesGiraphConfigurable;
import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;

/**
 * Vertex input format that supports NeuronVertex.
 */
public class NeuronVertexTextInputFormat extends
TextVertexInputFormat<IntWritable, NeuronWritable, NeuronEdgeWritable> 
implements ImmutableClassesGiraphConfigurable<IntWritable, NeuronWritable,
NeuronEdgeWritable, Writable> {

	/** Configuration. */
	private ImmutableClassesGiraphConfiguration<IntWritable, NeuronWritable,
	NeuronEdgeWritable, Writable> conf;

	@Override
	public TextVertexReader createVertexReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		return new NeuronVertexReader();
	}

	@Override
	public void setConf(ImmutableClassesGiraphConfiguration<IntWritable, NeuronWritable,
			NeuronEdgeWritable, Writable> configuration) {
		this.conf = configuration;
	}

	@Override
	public ImmutableClassesGiraphConfiguration<IntWritable, NeuronWritable,
	NeuronEdgeWritable, Writable> getConf() {
		return conf;
	}

	/**
	 * VertexReader that supports NeuronVertex
	 */
	public class NeuronVertexReader extends 
	TextVertexInputFormat<IntWritable, NeuronWritable, NeuronEdgeWritable>.TextVertexReader {

		public NeuronEdgeWritable edge_val = new NeuronEdgeWritable();
		
		@Override
		public Vertex<IntWritable, NeuronWritable, NeuronEdgeWritable, Writable> getCurrentVertex()
				throws IOException, InterruptedException {

			Vertex<IntWritable, NeuronWritable, NeuronEdgeWritable, Writable> vertex = conf.createVertex();
			String[] tokens = getRecordReader().getCurrentValue().toString().split(";");

			/** Construct vertex ID and value. */
			IntWritable vertex_id = new IntWritable(Integer.parseInt(tokens[0])); // Vertex ID
			NeuronWritable neuron = new NeuronWritable(); // Vertex value
			neuron.type = tokens[1].charAt(0);
			neuron.time = Integer.parseInt(tokens[2]);
			neuron.param_a = Float.parseFloat(tokens[3]);
			neuron.param_b = Float.parseFloat(tokens[4]);
			neuron.param_c = Float.parseFloat(tokens[5]);
			neuron.param_d = Float.parseFloat(tokens[6]);
			neuron.recovery = Float.parseFloat(tokens[7]);
			neuron.potential = Float.parseFloat(tokens[8]);
			neuron.synaptic_sum = Float.parseFloat(tokens[9]);
			neuron.fired = tokens[10].charAt(0);
			
			/** Construct edges. */
			String[] es = tokens[11].split(","); // edges are separated by comma
			List<Edge<IntWritable, NeuronEdgeWritable>> edges = 
					Lists.newArrayListWithCapacity(es.length);

			for (int i = 0; i < es.length; i++) {
				String[] elems = es[i].split(":");
				if (elems.length == 2) {
					int edge_id = Integer.parseInt(elems[0]);
					String[] vals = elems[1].split("-");
					edge_val.setWeight(Float.parseFloat(vals[0]));
					edge_val.setDelay(Integer.parseInt(vals[1]));
					edges.add(EdgeFactory.create(new IntWritable(edge_id), edge_val));
				}
			}

			vertex.initialize(vertex_id, neuron, edges);
			return vertex;
		}

		@Override
		public boolean nextVertex() throws IOException, InterruptedException {
			return getRecordReader().nextKeyValue();
		}
	}
}
