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
neuron types`, etc. The other one is `neuron_type.xml` which is
defined for each type of neurons.

### global.xml

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

### neuron_type.xml

This varies by neuron types. For each type of neurons, there is one
such file associated with that to describe meta data of that type.

For example, for `Excitatory Neurons`, the file is called
`ce_template.xml`, it looks like this:

{% highlight xml %}
<!-- A partition of the neural network, the partition contains "ce" -->
<!-- neurons -->
<partition>
  <!-- Description of the neurons inside the partition -->
  <!-- start_id and end_id will be filled by Python parser -->
  <neuron type="ce" start_id="xxx" end_id="yyy" potential="65">
    <!-- a = factor1 + factor2 * random_number(0~1) -->
    <parameter name="a" factor1="0.02" factor2="0" />
    <!-- b = factor1 - factor2 * random_number(0~1) -->
    <parameter name="b" factor1="0.2" factor2="0" />
    <!-- c = factor1 + factor2 * random_number(0~1)^2 -->
    <parameter name="c" factor1="-65" factor2="15" />
    <!-- d = factor1 - factor2 * random_number(0~1)^2 -->
    <parameter name="d" factor1="8" factor2="6" />
  </neuron>

  <connection>
    <!-- Describes types of neurons "ce" connects to, these are
	 outgoing connection -->
    <to type="ce" probability="1" strength="0.5" />
    <to type="ci" probability="1" strength="0.5" />
    <to type="stn" probability="0.25" strength="0.25" />
    <to type="strd1" probability="0.5" strength="0.2" />
    <to type="strd2" probability="0.5" strength="0.2" />
  </connection>

</partition>
{% endhighlight %}

It define a partition of the neural network that contains only `ce`
neurons, including `neuron type`, `start id`, `end id`, `parameters`
and other types of neurons `ce` connects to, as well as `connection
probability` and `connection strength`.

Note that `start_id` and `end_id` are not assigned with proper values,
this is because these values cannot be determined at this stage, our
`Python parser` will assign these values automatically in the next step.

## Run Python parser to generate XML configurations

Next, these XML templates are provided as inputs to a `Python parser`
which will generate the real XML configurations files for MapReduce.

The Python parser will read the `global.xml` and calculate the
`start id` and `end id` for each type of neurons. It will also read
each template file for neuron types and update the `start id` and `end
id`, and attach the `global` tag into `partition` tag so that the
MapReduce job will know the global neural network configurations while
processing each partition.

## Run MapReduce job to generate the real neural network

Finally, the MapReduce job will take the XML configuration files
generated by Python parser and generate the real neural network in the
format we have describe at [Represent a Neural Network in Hadoop](
{{site.baseurl }}{% post_url 2014-07-07-represent-neural-network %}).

