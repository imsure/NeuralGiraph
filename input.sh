#!/bin/sh

# Generate neural network as input to Giraph job for modeling.

hadoop fs -rm -r neuron_input
hadoop fs -rm -r xml_input4Hadoop
hadoop fs -put xml_input4Hadoop

hadoop jar giraph_neuron_graph-1.0.jar edu.stthomas.gps.NeuronInput xml_input4Hadoop neuron_input
