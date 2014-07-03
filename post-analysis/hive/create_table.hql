drop table neuron_giraph;

create external table neuron_giraph (
id int, type string, time int, recovery float, potential float, fired string, channel int)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE LOCATION '/user/shuo/neuron_output';
