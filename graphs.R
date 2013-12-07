## 'cat' must be used in interactive mode, meanning that you must enter
## the interactive mode of R and then run: R> source("graphs.R")

cat("enter the number of neurons: ")
neuron.number = as.integer(readLines(n=1))

cat("enter the totol time of simulation in ms: ")
simulation.time = as.integer(readLines(n=1))

jpeg('./output/firing.jpg', width = 1650, height = 1000)
firing = read.table('./output/firings.txt', header=FALSE, sep='\t', col.names=c('time','id'))

plot(firing$time, firing$id, xlim=c(1, simulation.time), ylim=c(1, neuron.number), main='Neuron Firings',
     xlab='Time (ms)', ylab='Neuron ID', col='blue', pch=19, cex=.5)
dev.off()

## jpeg('./output/potentials.jpg')
## voltage = read.table('./output/potentials.txt', header=FALSE, sep='\t', col.names=c('id','time','vol'))

## vs = subset(voltage, id==1)
## plot(vs$time, vs$vol, xlim=c(1, 100), ylim=c(-110, 25), main='Membrane Potential Evolution for Neuron 1',
##      xlab='Time (ms)', ylab='Membrane Voltage', col='blue', type='n')

## lines(vs$time, vs$vol, col='blue')
## dev.off()

## jpeg('./output/recoveries.jpg')
## recovery = read.table('./output/recoveries.txt', header=FALSE, sep='\t', col.names=c('id','time','rec'))

## rs = subset(recovery, id==1)
## plot(rs$time, rs$rec, xlim=c(1, 100), main='Recovery Variable Evolution for Neuron 1',
##      xlab='Time (ms)', ylab='Recovery Variable', col='blue', type='n')
## lines(rs$time, rs$rec, col='blue')
## dev.off()
