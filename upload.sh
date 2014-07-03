#!/bin/sh

scp ./target/giraph_neuron_graph-1.0.jar shuo@hc.gps.stthomas.edu:/home/shuo/giraph
scp -r ./xml_input4Hadoop/ shuo@hc.gps.stthomas.edu:/home/shuo/giraph
scp input.sh run.sh shuo@hc.gps.stthomas.edu:/home/shuo/giraph
