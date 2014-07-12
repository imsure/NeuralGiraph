---
layout: post
title:  "Represent a Neural Network in Hadoop"
date:   2014-07-12 21:58:57
categories: documents
---

## A neural network is a graph structure

From computer science perspective, a neural network is a graph
structure.

One common way to represent a graph is via an adjacency matrix. As we
have seen the below figure in the [previous post]({{ site.baseurl }}{% post_url 2014-07-11-neuralmodel %}),
the neural network is represented as a `N*N` square matrix, where a
cell value *M<sub>ij</sub>* indicates and edge from vertex n<sub>i</sub>
to vertex n<sub>j</sub>.

Using matrix is straightforward, but the matrix we are seeing is
sparse, that is, a lot of cells are empty, and it is not algorithmically efficient to use a matrix
representation to process the neural network.

For computer scientists, it is more natural to represent a graph with
adjacency lists, in which a vertex is associated with neighbors that
can be reached via outgoing edges. 

![matrix two channels]({{ site.baseurl }}/assets/matrix-two-channels.svg)
 
