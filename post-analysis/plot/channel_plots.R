library('ggplot2')
library('grid')

# set the current working directory
setwd('/home/imsure/Desktop/HelloWorld/neural_project')

firings = read.csv('firings_noinput2channel.csv', header=F, sep='\t')
# assign names to data frame
names(firings) = c('timeStep', 'neuronID', 'neuronType')

# filter data for only channel 1
firings = subset(firings, neuronID<=2500)

# firing plot
png('channels_firing.png', width=1000, height=800)
firings.plot = ggplot(firings, aes(x=timeStep, y=neuronID, color=neuronType)) +
  geom_point() + 
  ggtitle('Firing Pattern') +
  xlab('Time Step (ms)') + ylab('Neuron ID Fired')

firings.plot = firings.plot + theme(axis.title=element_text(size=20, face='bold'),
                                    axis.text=element_text(size=13, color='blue', face='bold'),
                                    plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                                    legend.title=element_blank(),
                                    legend.text=element_text(face = "bold", size=18),
                                    legend.position = 'bottom',
                                    legend.key=element_rect(fill=NA),
                                    legend.key.size=unit(3, "cm"),
                                    legend.key.height=unit(.8, "cm")) +
  guides(colour = guide_legend(override.aes = list(size=8)))

firings.plot
dev.off()

png('gpe_frate.png', width=1000, height=500)
gpe.frate = read.csv('gpe_fr.csv', header=F, sep='\t')
names(gpe.frate) = c('timeStep', 'channel', 'firingRate')
gpe.frate$channel = as.factor(gpe.frate$channel)

gpe.frate.plot = ggplot(gpe.frate, aes(x=timeStep, y=firingRate, color=channel)) +
  geom_line(size=.3) + 
  ggtitle('Firing Rate for GPe') +
  xlab('Time Step (ms)') + ylab('Firing Rate')

gpe.frate.plot
dev.off()
