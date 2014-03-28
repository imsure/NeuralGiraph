package edu.stthomas.gps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import java.util.*;

/**
 * A writable wrapper for the edge between two neurons.
 * Unlike previous implementation, we put a random delay (in ms) between
 * each pair of connected neurons. 
 * 
 * @author imsure
 *
 */
public class NeuronEdgeWritable implements Writable {

	private float weight; // synaptic weight between two neurons
	private int delay;
	private int delay_counter;
	
	public NeuronEdgeWritable() {}
	
	public void write(DataOutput out) throws IOException {
		out.writeFloat(weight);
		out.writeInt(delay);
		out.writeInt(delay_counter);
	}
	
	public void readFields(DataInput in) throws IOException {
		weight = in.readFloat();
		delay = in.readInt();
		delay_counter = in.readInt();
	}
	
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
		this.delay_counter = delay;
	}
	
	public float getWeight() {
		return this.weight;
	}
	
	public void decDelay() {
		this.delay_counter--;
	}
	
	public int getDelayCounter() {
		return this.delay_counter;
	}
	
	public void resetDelayCounter() {
		this.delay_counter = this.delay;
	}
	
	@Override
	public String toString() {
		return "" + weight + "," + delay + "," + delay_counter;
	}
}
