---
layout: post
title:  "Neural Model"
date:   2014-07-11 21:58:57
categories: documents
---

## Simple Model of Spiking Neurons

We are using a
[simple model of spiking neurons](http://www.izhikevich.org/publications/spikes.htm)
presented by [Izhikevich](http://www.izhikevich.org/). The model was
used to simulate tens of thousands of spiking cortical neurons in real
time.

## How model works

![spiking model]({{ site.baseurl }}/assets/spiking-model.svg)

A neuron would receive synaptic currents from its incoming neighbors,
these currents adding up together would trigger a voltage change in
the membrane, either positive or negative. If the voltage reaches to a
threshold value, the neuron would send synaptic currents to its
outgoing neighbors.

We extend the model to support:

 - more types of neurons
 - multiple channels in the neural network


| ![firing-1000]({{ site.baseurl }}/assets/plots-1000-network.svg)


## A Neural Network is a Graph Structure



