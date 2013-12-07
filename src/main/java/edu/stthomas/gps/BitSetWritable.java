package edu.stthomas.gps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.Writable;

public class BitSetWritable implements Writable {

	private BitSet set;

	public BitSetWritable() {
		set = new BitSet();
		// default constructor
	}

	public BitSetWritable(BitSet set) {
		this.set = set;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(set.length());
		for (int i = 0; i < set.length(); i++) {
			out.writeBoolean(set.get(i));
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int len = in.readInt();
		for (int i = 0; i < len; i++) {
			set.set(i, in.readBoolean());
		}
	}

	public BitSet getBitSet() {
		return this.set;
	}

	@Override
	public String toString() {
		return this.set.toString();
	}
}