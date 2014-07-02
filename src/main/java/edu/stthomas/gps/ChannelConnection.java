package edu.stthomas.gps;

import java.util.ArrayList;

/**
 * Hold information about each channel connection.
 * 
 * @author imsure
 *
 */
public class ChannelConnection {

	private String source;
	private ArrayList<String> targets;
	private float diffuseProb;
	private float diffuseWeight;
	
	public ChannelConnection() {
		targets = new ArrayList<String>();
	}
	
	public void setSourceChannel( String source ) {
		this.source = source;
	}
	
	public String getSourceChannel() {
		return this.source;
	}
	
	public void addTargetChannel( String target ) {
		targets.add(target);
	}
	
	public ArrayList<String> getTargetChannels() {
		return this.targets;
	}
	
	public void setDiffuseProb( float prob ) {
		this.diffuseProb = prob;
	}
	
	public float getDiffuseProb() {
		return this.diffuseProb;
	}
	
	public void setDiffuseWeight( float weight ) {
		this.diffuseWeight = weight;
	}
	
	public float getDiffuseWeight() {
		return this.diffuseWeight;
	}
}
