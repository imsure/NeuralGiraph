#!/bin/sh

hadoop fs -rm -r neuron_meta
hadoop fs -rm -r neuron_input
hadoop fs -put neuron_meta

hadoop jar giraph_neuron_graph-1.0.jar edu.stthomas.gps.NeuronInput -DEPROB=0.2 -DIPROB=0.3 neuron_meta neuron_input
