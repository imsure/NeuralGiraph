package edu.stthomas.gps;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.giraph.aggregators.DoubleMaxAggregator;
import org.apache.giraph.aggregators.DoubleMinAggregator;
import org.apache.giraph.aggregators.LongSumAggregator;
import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.VertexReader;
import org.apache.giraph.io.formats.AdjacencyListTextVertexInputFormat;
import org.apache.giraph.io.formats.GeneratedVertexInputFormat;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.giraph.master.DefaultMasterCompute;
import org.apache.giraph.worker.WorkerContext;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;

/**
 * A neuronal modeling implementation using Giraph.
 * @author imsure
 *
 */
public class NeuronVertex extends Vertex<IntWritable, 
NeuronWritable, FloatWritable, FloatWritable> {

	private Random randn = new Random();
	private FloatWritable weight = new FloatWritable();

	/** Number of supersteps for this test */
	public static final int MAX_SUPERSTEPS = 35;
	/** Logger */
	private static final Logger LOG =
			Logger.getLogger(NeuronVertex.class);

	private double getGaussian() {
		return randn.nextGaussian();
	}

	private void neuronEvolution(float current, NeuronWritable neuron) {
		current += neuron.synaptic_sum;
		// Update the membrane potential. Step 0.5 ms for numerical stability. 
		neuron.potential += 0.5 * (0.04*neuron.potential*neuron.potential + 5*neuron.potential
				+ 140 - neuron.recovery + current);
		neuron.potential += 0.5 * (0.04*neuron.potential*neuron.potential + 5*neuron.potential
				+ 140 - neuron.recovery + current);
		// Update membrane recovery variable.
		neuron.recovery += neuron.param_a * (neuron.param_b*neuron.potential - neuron.recovery);

		// Update number of iteration
		neuron.time += 1;
		neuron.synaptic_sum = (float) 0.0;
		neuron.fired = 'N'; // Reset firing status
	}

	@Override
	public void compute(Iterable<FloatWritable> messages) {

		int max_supersteps = Integer.parseInt(this.getConf().get("max_supersteps", "50"));

		if (getSuperstep() < max_supersteps) {
			/** Sum up the messages from last super step. */
			float weight_sum = 0;
			for (FloatWritable message: messages) {
				weight_sum += message.get();
			}

			/** Update the vertex state for the current iteration. */
			NeuronWritable neuron = getValue();
			neuron.synaptic_sum = weight_sum; // Update synaptic weight summation

			float current;
			// Generate thalamic input.
			if (neuron.type == 'e') {
				current = 5 * (float)this.getGaussian();
			} else {
				current = 2 * (float)this.getGaussian();
			}
			this.neuronEvolution(current, neuron); 	// Start Neuron Evolution

			/** If a neuron fired, send the messages to its outgoing neurons. */
			if (neuron.potential >= 30.0) {
				for (Edge<IntWritable, FloatWritable> edge : getEdges()) {
					weight.set(edge.getValue().get());
					if (LOG.isDebugEnabled()) {
						LOG.debug("Vertex " + getId() + " sent to " +
								edge.getTargetVertexId() + " = " + weight.get());
					}

					sendMessage(edge.getTargetVertexId(), weight);
				}

				neuron.potential = neuron.param_c; // Reset the membrane potential (voltage)
				neuron.recovery += neuron.param_d; // Reset the membrane recovery variable
				neuron.fired = 'Y'; // Indicate the neuron fired at this iteration.
			}

			setValue(neuron); // Update the value of the vertex.

		} else {
			voteToHalt();
		}
	}
}
