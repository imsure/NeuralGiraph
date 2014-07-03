select type, channel, count(id) from neuron_giraph where fired='Y' group by type, channel;
