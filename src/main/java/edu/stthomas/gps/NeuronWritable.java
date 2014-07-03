package edu.stthomas.gps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import java.util.*;

/**
 * NeuronWritable holds a neuron's internal data.
 * 
 * @author imsure
 *
 */

public class NeuronWritable implements Writable {

	public Text type;
	public int time; // In simulation, it is actually the number iteration
	public float param_a, param_b, param_c, param_d;
	public float recovery;
	public float potential;
	public float synaptic_sum;
	public char fired;
	public int channel; // the channel this neuron belongs to
	
	public NeuronWritable() {
		type = new Text();
	}
	
	public void write(DataOutput out) throws IOException {
		type.write(out);
		out.writeInt(time);
		out.writeFloat(param_a);
		out.writeFloat(param_b);
		out.writeFloat(param_c);
		out.writeFloat(param_d);
		out.writeFloat(recovery);
		out.writeFloat(potential);
		out.writeFloat(synaptic_sum);
		out.writeChar(fired);
		out.writeInt(channel);
	}
	
	public void readFields(DataInput in) throws IOException {
		type.readFields(in);
		time = in.readInt();
		param_a = in.readFloat();
		param_b = in.readFloat();
		param_c = in.readFloat();
		param_d = in.readFloat();
		recovery = in.readFloat();
		potential = in.readFloat();
		synaptic_sum = in.readFloat();
		fired = in.readChar();
		channel = in.readInt();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(';');
		sb.append(time).append(';');
		sb.append(String.format("%.2f", param_a)).append(';');
		sb.append(String.format("%.2f", param_b)).append(';');
		sb.append(String.format("%.2f", param_c)).append(';');
		sb.append(String.format("%.2f", param_d)).append(';');
		sb.append(String.format("%.2f", recovery)).append(';');
		sb.append(String.format("%.2f", potential)).append(';');
		sb.append(String.format("%.2f", synaptic_sum)).append(';');
		sb.append(fired).append(';');
		sb.append(channel);
		
		return sb.toString();
	}
	
	/**
	 * Concatenate the fields of a NeuronWritable into a string for Giraph's output.
	 * Note: drop those unused fields to save space since we need to output these data
	 * in each iteration of Giraph.
	 * 
	 * @return the string representation of the neuron
	 */
	public String toString2() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append('\t');
		sb.append(time).append('\t');
		sb.append(String.format("%.2f", recovery)).append('\t');
		sb.append(String.format("%.2f", potential)).append('\t');
		sb.append(fired).append('\t');
		sb.append(channel);
		
		return sb.toString();
	}
}
