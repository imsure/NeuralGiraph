import xml.etree.ElementTree as ET
import os
from shutil import rmtree

template_path = './xml_templates'
output_path = './xml_input4Hadoop'

def create_output(neuron_type, start_id, end_id, num_per_partition, global_root):
    template = neuron_type + '_template.xml'
    tree = ET.parse( os.path.join(template_path, template) )
    root = tree.getroot()

    for i in range( 0, (end_id-start_id)/num_per_partition+1 ):
        start = start_id + i * num_per_partition
        end = start + num_per_partition - 1
        if end > end_id: # handle the corner case
            end = end_id

        output_fname = neuron_type + '_' + str(start) + 'to' + str(end) + '.xml'
        path = os.path.join( output_path, output_fname )

        neuron = root.find( 'neuron' )
        neuron.set( 'start_id', str(start) )
        neuron.set( 'end_id', str(end) )

        root.append( global_root )
        tree.write( path )
    

if __name__ == '__main__':

    if os.path.isdir( output_path ):
        rmtree( output_path )

    os.mkdir( output_path )
    
    tree = ET.parse( os.path.join(template_path, 'global.xml') )
    root = tree.getroot()

    total_num_neurons = int( root.attrib['total'] )
    num_channels = int( root.attrib['channel'] )
    num_neuron_partition = int( root.attrib['unit'] )

    print 'Total number of neurons:', total_num_neurons
    print 'Number of channels:', num_channels
    print 'Maximum number of neurons in each partition:', num_neuron_partition

    for range_info in root.iter('range'):
        neuron_type = range_info.attrib['type']
        start_id_ch1 = int( range_info.attrib['start'] )
        end_id_ch1 = int( range_info.attrib['end'] )

        create_output( neuron_type, start_id_ch1, end_id_ch1, num_neuron_partition, root )

