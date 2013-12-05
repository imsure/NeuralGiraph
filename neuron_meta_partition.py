#!/usr/bin/env python

"""
Generate meta data files for project "Neuronal Modeling using Hadoop".
The meta data files serve as input for a MapReduce (mapper-only) program
to generate the actual input data which is a graph representation
of the neuron network for the project. Each meta data file corresponds to
a MapReduce output. E.g, neuron_meta0.txt --> part-m-00000
neuron_meta299.txt --> part-m-00299

The meta data file is text file with a single line:
start neuron id, end neuron id, total number of neurons, type of neurons

To generate these files, we only need to provide:
* The total number of neurons
* Neuron types
* Proportion of each type of neurons
* Number of neurons we want to put in one files.
"""

import os
import shutil

Total = 1000 # The total number of neurons
TypeE = 'e' # Excititory
TypeI = 'i' # Inhibitory
NumE = 800
NumI = 200
NumEPerFile = 400
NumIPerFile = 200
FileNamePrefix = 'neuron_meta'
InputDir = 'neuron_meta'

def partition(start, neuron_type, total_by_type, NumPerFile):

    start_id = 0
    end_id = 0

    file_name = ''
    file_content = ''

    num_partition = total_by_type / NumPerFile
        
    remaining = total_by_type - NumPerFile*num_partition

    n = 0
    while n < num_partition:
        start_id = start + n * NumPerFile + 1
        end_id = start + (n+1) * NumPerFile

        file_name = FileNamePrefix + str(n) + neuron_type + '.txt'
        fobj = open(os.path.join(InputDir, file_name), 'w')
        fobj.write(str(start_id)+','+str(end_id)+','+str(Total)+','+neuron_type+'\n')
        n += 1


if __name__ == '__main__':
    if os.path.exists(InputDir):
        shutil.rmtree(InputDir)

    os.mkdir(InputDir)
    partition(0, TypeE, NumE, NumEPerFile)
    partition(NumE, TypeI, NumI, NumIPerFile)