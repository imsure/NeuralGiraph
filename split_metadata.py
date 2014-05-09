#!/usr/bin/env python

meta_input = open('neuron_meta_channels.txt')
for line in meta_input.readlines():
    elems = line.split(',')
    neuron_type = elems[4]
    fname = neuron_type + '.txt'
    
    fp = open(fname, 'w+')
    fp.write(line)
    fp.close()