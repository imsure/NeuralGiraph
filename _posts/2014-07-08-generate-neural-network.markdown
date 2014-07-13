---
layout: post
title:  "Generate Neural Network with MapReduce"
date:   2014-07-08 21:58:57
categories: documents
---

As we've seen in [Overview]({{ site.baseurl }}{% post_url 2014-07-05-overview %}), 
the first step is to generate a neural network for modeling. This is
not an easy task, though. A neural network has many parameters to be
concerned, in our earlier implementations, we actually hard-coded many
parameters in the code and that made customizing the neural network
very difficult. We finally managed to put all configurations in to
some XML templates. 

![alt basic work flow]({{ site.baseurl }}/assets/work-flow.svg)

## Define the neural network with XML templates

Basically, there are two types of templates, one is `global.xml` which
is used to describe the meta data associated with the neural network,
such as `total number of neurons in one channel`, `number of
channels`, `connections between channels` and `range partitioning by
neuron types`, etc.

{% highlight xml %}
<!-- total number of neurons, number of channels, maximum number of -->
<!-- neurons in each partition -->
<global total="2572" channel="2" unit="1000">
  <!-- how channels are connected -->
  <channel_connection from="stn" diffuse_prob="0.5" diffuse_weight="0.35">
    <to type="gpe"></to>
    <to type="gpi"></to>
  </channel_connection>

  <!-- range partitioning of channel 1 -->
  <range type="ce" start="1" end="800" />
  <range type="ci" start="801" end="1000" />
  <range type="tc" start="1001" end="1011" />
  <range type="stn" start="1012" end="1031" />
  <range type="strd1" start="1032" end="1989" />
  <range type="strd2" start="1990" end="2468" />
  <range type="gpe" start="2469" end="2528" />
  <range type="gpi" start="2529" end="2572" />
</global>
{% endhighlight %}

Above is an example of `global.xml`. It defines a neural network with
`two` channels, within each channel, there are `2572` neurons. In each
partition of the network stored on `HDFS` (Hadoop Distributed File
System), the maximum number of neurons is `1000`, such that each
partition has a reasonable size for MapReduce to process in
parallel. It also specifies the channel connection: from each
channel's `stn` neurons to other channels `gpe` and `gpi` neurons, the
diffusion probability is `50%` and diffusion weight is `0.35`. The
`range` tag defines the `start` and `end` ID for each type of neurons.



