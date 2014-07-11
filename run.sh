#!/bin/sh

# Run Giraph job for neural network modeling

hadoop fs -rm -r neuron_output

hadoop jar giraph_neuron_graph-1.0.jar org.apache.giraph.GiraphRunner -Dgiraph.zkList=m02.gps.stthomas.edu:2181 -Dgiraph.doOutputDuringComputation=true -Dmax_supersteps=500 edu.stthomas.gps.NeuronVertex -vif edu.stthomas.gps.NeuronVertexTextInputFormat -vip neuron_input -of edu.stthomas.gps.NeuronVertexTextOutputFormat -op neuron_output -w 10 

