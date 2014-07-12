---
layout: post
title:  "Introduction"
date:   2014-07-11 21:58:57
categories: documents
---

## Why are we doing this?

Neural network is a large and complex system. Being able to simualte
large-scale neural network is essential for us to understand how brain
works. With the emergence of open source big data platform such as
Hadoop, we are able to achieve this with a low cost.

## How are we doing this?

We are running simulations on a Hadoop cluster deployed at the
[University of St. Thomas](http://www.stthomas.edu/), by Department of
[Graduate Programs in Software](http://www.stthomas.edu/gradsoftware/).

Basically, from computer science perspective, a neural network is a
graph structure, so we are focusing on both neural modeling as well as
distributed graph algorithms. We have been working on graph algorithms
using MapReduce and [Giraph]. Finally we settled with [Giraph] because
it is a effective model for large-scale iterative graph processing.

## Basic work flow

![basic work flow](/assets/work-flow.svg)

*phase one: generating neural network*

The neural network to be processed is configurable by a set of XML
templates. These templates then go through a Python parser which will
create the configurations in XML format and feed it to a MapReduce
job to generate the neural network in parallel. 

*phase two: modeling with [Giraph]*

The neural network generated in phase one is then passed to a [Giraph]
job for simulation.

*phase three: post analysis*

The results output by phase two are then used for post analysis, such
as firing rate computation and firing plots.

That's it ^_^


[Giraph]: http://giraph.apache.org/
