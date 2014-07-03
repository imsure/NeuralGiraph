library('ggplot2')
library('reshape')

# set the current working directory
setwd('/home/imsure/Desktop/HelloWorld/neural_project')

###########################################
# Plot the Running time of 20ms simulation in MapReduce 
###########################################

names <- c('Basic', 'Schimmy', 'Mapper-side Schimmy', 
           'Basic+IMC', 'Mapper-side Schimmy+IMC', 'Giraph')

mr.20ms <- c(1671, 1680, 1455, 1088, 729, 217)
full.firing <- c(125, 135, 117, 58, 39, 11)
half.firing <- c(86, 92, 76, 55, 38, 8)
no.firing <- c(52, 55, 26, 51, 26, 5)

mr <- data.frame(names, mr.20ms, full.firing, half.firing, no.firing)
mr$names = reorder(mr$names, -mr.20ms)

png('1.png', width=600, height=350)

#mr$names <- sort(mr$names)

bar.mr.20ms <- ggplot(data=mr, aes(x=names, y=mr.20ms, fill=names)) + 
  geom_bar(stat='identity', width=.8) + # use the real value, not the count
  xlab('Implementation') + ylab('Runtime (s)') +
  ggtitle('Running time of 20ms simulation')

# Calulate the percentage difference between other implementations
# and the basic implementation
mr.20ms.change = NULL
for (i in 1:6) {
  mr.20ms.change[i] = round((mr.20ms[i] - mr.20ms[1]) / mr.20ms[1] * 100)
}

mr.20ms.change <- as.character(mr.20ms.change)
mr.20ms.change <- paste(mr.20ms.change, '%', sep='') # add a trailing '%'

# set the new scales
bar.mr.20ms <- bar.mr.20ms + 
  scale_x_discrete(breaks=names, labels=mr.20ms.change)

bar.mr.20ms <- bar.mr.20ms + theme(axis.title=element_text(size=14, face='bold'),
                    axis.text=element_text(size=12, color='blue'),
                    plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                    legend.title=element_blank(),
                    legend.position=c(0.8,0.8))

#multiplot(bar.mr.20ms, runtime.cmp.line, cols=1)

bar.mr.20ms
dev.off()
#bar.mr.20ms + theme(axis.text.x=element_blank())
#bar.mr.20ms <- bar.mr.20ms + theme_bw()

###########################################
# End of Plotting the Running time of 20ms simulation in MapReduce 
###########################################

###########################################
# Plot Comparison of the runtime under different
# firing modes among implementations 
###########################################

program <- c('Basic', 'Basic', 'Basic', 'Schimmy', 'Schimmy', 'Schimmy', 
              'Mapper-side Schimmy', 'Mapper-side Schimmy', 'Mapper-side Schimmy',
              'Basic+IMC', 'Basic+IMC', 'Basic+IMC', 'Mapper-side Schimmy+IMC',
              'Mapper-side Schimmy+IMC', 'Mapper-side Schimmy+IMC',
              'Giraph', 'Giraph', 'Giraph')

firing.type <- c('full firing', 'half firing', 'no firing',
                 'full firing', 'half firing', 'no firing',
                 'full firing', 'half firing', 'no firing',
                 'full firing', 'half firing', 'no firing',
                 'full firing', 'half firing', 'no firing',
                 'full firing', 'half firing', 'no firing')

runtime <- c(125, 86, 52, 135, 92, 55, 117, 76, 26,
             58, 55, 51, 39, 38, 26, 11, 8, 5)

mr.firing <- data.frame(program, firing.type, runtime, stringsAsFactors = FALSE)
# specify 'program' as a factor
mr.firing$program <- factor(mr.firing$program)
# reorder the factor for plotting
mr.firing$program <- reorder(mr.firing$program, -mr.firing$runtime)

png('2.png', width=800, height=400)
bar.firing <- ggplot(mr.firing, aes(x=program, y=runtime, fill=firing.type))
bar.firing <- bar.firing + layer(geom='bar',
                                 stat='identity',
                                 position='dodge')
bar.firing <- bar.firing + xlab('Implementation') + ylab('Runtime (s)') +
  ggtitle('Comparison of the runtime under different firing modes')

bar.firing <- bar.firing + theme(axis.title=element_text(size=12, vjust=0.2, face='bold'),
                                 axis.text=element_text(size=9, color='blue', face='bold'),
                                 plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                                 legend.title=element_blank())

bar.firing
dev.off()
#ggsave(bar.firing, file='3.png', width=40, height=20)

###########################################
# End of Plotting Comparison of the runtime under different
# firing modes among implementations 
###########################################

###################################################
# Plotting the Comparison of the running time for 20 iterations
###################################################

png('3.png', width=700, height=400)
time <- c(1:20)
runtime.basic <- c(48,52,52,50,46,52,54,53,83,69,72,80,124,122,125,127,
                   125,125,125,122)
runtime.schimmy <-c(55,57,55,57,57,59,56,62,93,73,82,109,
                    137,137,137,137,138,138,137,137)
runtime.basic.imc <- c(49,53,53,52,49,50,53,46,50,53,54,
                       59,51,55,53,56,53,53,59,58)
runtime.mapper.schimmy <- c(26,25,26,27,30,31,33,41,78,60,
                            54,92,120,117,120,119,118,119,119,118)
runtime.mapper.schimmy.imc <- c(26,26,25,28,29,28,31,36,38,37,38,38,40,
                                40,38,39,41,41,39,41)
runtime.giraph <- c(4.8,4.3,4.4,4.5,4.5,5.3,8.6,10.5,10.8,10.5,
                    10.5,10.6,10.7,10.8,10.8,10.5,10.4,10.7,10.3,10.8)

runtime.cmp <- data.frame(time, runtime.basic, runtime.schimmy,
                          runtime.basic.imc, runtime.mapper.schimmy,
                          runtime.mapper.schimmy.imc, runtime.giraph)
#ggplot(runtime.cmp, aes(time)) +
#  geom_line(aes(y=runtime.basic)) +
#  geom_line(aes(y=runtime.schimmy))
runtime.cmp.melt <- melt(runtime.cmp, id='time')
runtime.cmp.line <- ggplot(runtime.cmp.melt, aes(x=time, y=value, color=variable)) +
  layer(geom='point') + layer(geom='line') +
  ggtitle('Comparison of the running time for 20 iterations') +
  xlab('Iteraion (1ms simulation)') + ylab('Runtime (s)')

#runtime.cmp.line <- runtime.cmp.line + scale_fill_discrete(name='variable')

names.reorder <- c('Basic', 'Schimmy', 'Basic+IMC', 'Mapper-side Schimmy', 
                   'Mapper-side Schimmy+IMC', 'Giraph')
# change the labels of the legend, a bit awkward, but, ...
runtime.cmp.line <- runtime.cmp.line + scale_color_discrete(name='variable', 
                                                            breaks=c('runtime.basic', 'runtime.schimmy',
                                                                     'runtime.basic.imc', 'runtime.mapper.schimmy',
                                                                     'runtime.mapper.schimmy.imc', 'runtime.giraph'), 
                                                            labels=names.reorder)

runtime.cmp.line <- runtime.cmp.line + theme(axis.title=element_text(size=14, face='bold'),
                                             axis.text=element_text(face='bold'),
                                             plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                                             legend.title=element_blank(),
                                             legend.position=c(0.15, 0.75),
                                             legend.key=element_rect(fill=NA)) +
  guides(colour = guide_legend(override.aes = list(size = 10))) # adjust the legend

dev.off()

###################################################
# End of Plotting the Comparison of the running time for 20 iterations
###################################################

###########################################
# Plot correlation between running time and
# number of worker for Giraph implementation
###########################################
png('4.png', width=600, height=300)

worker.num <- seq(from=60, to=110, by=10)
giraph.runtime <- c(236, 232, 220, 228, 223, 217) 
giraph.worker.scale <- data.frame(worker.num, giraph.runtime)

ggplot(giraph.worker.scale, aes(x=worker.num, y=giraph.runtime)) + 
  layer(geom='point', shape=20, color='red', size=3) + layer(geom='smooth', method='lm') +
  scale_x_continuous(breaks=worker.num) + 
  xlab('Number of workers') + ylab('Runtime (s)') +
  ggtitle('Scalability of workers (100,000 neurons, 2.3 Billion edges)') +
  theme(axis.title=element_text(size=14, face='bold'),
        axis.text=element_text(face='bold'),
        plot.title=element_text(hjust=0, face='bold', vjust=1, size=16))

dev.off()
###########################################
# End of Plotting correlation between running time and
# number of worker for Giraph implementation
###########################################

##################################################
# Plot edge scalability
##################################################
png('5.png', width=600, height=300)

edge.num <- c(100000000, 400000000, 800000000, 1200000000, 2300000000, 4600000000)
edge.runtime <- c(92, 103, 124, 144, 220, 413)

giraph.edge.scale <- data.frame(edge.num, edge.runtime)
edge.num.billion <- c('0.1', '0.4', '0.8', '1.2', '2.3', '4.6')

ggplot(giraph.edge.scale, aes(x=edge.num, y=edge.runtime)) + 
  layer(geom='point', shape=20, color='red', size=3) + layer(geom='line') +
  geom_smooth(method='lm') +
  xlab('Billions of edges') + ylab('Runtime (s)') +
  ggtitle('Scalability of edges') +
  scale_x_continuous(breaks=edge.num, labels=edge.num.billion) +
  theme(axis.title=element_text(size=14, face='bold'),
        axis.text=element_text(face='bold'),
        plot.title=element_text(hjust=0, face='bold', vjust=1, size=16))

dev.off()