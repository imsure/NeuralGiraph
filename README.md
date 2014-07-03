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


### Neural network generation with MapReduce

### Run Giraph job for modeling

### Post analysis



