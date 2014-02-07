#!/bin/sh

hadoop fs -rm -r giraph_firing

hadoop jar giraph_neuron_graph-1.0.jar edu.stthomas.gps.FiringPattern neuron_output giraph_firing
hadoop fs -get giraph_firing/part-r-00000 firings.txt
