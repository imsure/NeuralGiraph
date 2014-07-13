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

```javascript

var s = "JavaScript syntax highlighting";
alert(s);

```





