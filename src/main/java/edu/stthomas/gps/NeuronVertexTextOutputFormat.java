package edu.stthomas.gps;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;

import java.io.*;

public class NeuronVertexTextOutputFormat 
extends TextVertexOutputFormat<IntWritable, NeuronWritable, FloatWritable> {

	private Text neuron_id = new Text();
	private Text neuron_value = new Text();
	
	private class NeuronVertexTextWriter extends TextVertexWriter {
		@Override
		public void writeVertex(Vertex<IntWritable, NeuronWritable, FloatWritable, ?> vertex)
				throws IOException, InterruptedException {
			neuron_id.set(vertex.getId().toString());
			neuron_value.set(vertex.getValue().toString());
			getRecordWriter().write(neuron_id, neuron_value);
		}
	}

	@Override
	public TextVertexWriter createVertexWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new NeuronVertexTextWriter();
	}
}
