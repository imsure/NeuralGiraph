## Large-scale neural network modeling using Giraph

We use [Giraph](http://giraph.apache.org) to model large-scale neural network on the
[Hadoop](hadoop.apache.org) cluster deployed
at the [University of St. Thomas](http://www.stthomas.edu/) by
[Department of Graduate Programs in Software](http://www.stthomas.edu/gradsoftware/).

*Note*: The code has been fully tested on CDH4.4, but it is not working on CDH5 yet.

### Software Packages needed:

+ Maven 3
+ CDH4
+ Giraph
+ Java 1.6
+ Python 2.7 or higher
+ R

### Generate XML configuration files

`python xml_templates_parser.py`

A directory called 'xml_input4Hadoop' will be generated under the project home.

### Compile & upload to Hadoop cluster

Modify *upload.sh* first with appropriate user name and Hadoop cluster name.

`mvn install`

This command will compile the project and upload necessary files to Hadoop cluster
in the directory *giraph* under $HOME

### Neural network generation with MapReduce

ssh into your Hadoop cluster:

`cd giraph`
`chmod +x *.sh`
`./input.sh`

The neural network will be generated in *neuron_input* on HDFS.

### Run Giraph job for modeling

Delete _logs files generated at the previous step, otherwise Giraph will take it as
input and an error would occur.

`hadoop fs -rm -r neuron_input/_logs`

Run Giraph job

`./run.sh`

### Post analysis



