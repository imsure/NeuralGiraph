###################################################
# Draw graphs as Dr. Jackson suggested:
#   * rerun the jobs for 40 ms simulation
#   * put bar chart and line chart in the same plot
#   * same legend colors for both
###################################################

library('ggplot2')
library('reshape')
library('grid')
library('gridExtra')

multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

# set the current working directory
setwd('/home/imsure/Desktop/HelloWorld/neural_project')

runtime = read.csv('runtime_stats_neuron_hadoop.csv', as.is=T, stringsAsFactors=F) 

png('plot-40ms.png', width=1000, height=1500)

# reorder the name of each version in descending order by total runtime.
runtime$version = reorder(runtime$version, -runtime$total)

bar.runtime = ggplot(data=runtime, aes(x=version, y=total, fill=version)) +
  geom_bar(stat='identity', width=.7) + # a bar chart, 'stat' indicates we use real name as x value, not count
  xlab('Implementation') + ylab('Runtime (s)') +
  ggtitle('A: Running time of 40ms simulation')

# Calulate the percentage difference between other implementations
# and the basic implementation.
runtime.diff = NULL
base = runtime['total'][1,] # use runtime of the Basic implementation as a base for calculating the difference 
for (i in 1:length(runtime['total'][,1])) {
  runtime.diff[i] = round((runtime['total'][i,] - base)/base * 100)
}

runtime.diff = as.character(runtime.diff) # covert to chars
runtime.diff = paste(runtime.diff, '%', sep='') # add a trailing %

# set discrete x scale with labels as runtime.diff
bar.runtime = bar.runtime + 
  scale_x_discrete(breaks=runtime$version, labels=runtime.diff)

bar.runtime = bar.runtime + geom_hline(yintercept=base, 
                                       linetype='dotted',
                                       size=.8)

bar.runtime <- bar.runtime + theme(axis.title=element_text(size=20, face='bold'),
                                   axis.text=element_text(size=13, color='blue', face='bold'),
                                   plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                                   legend.title=element_blank(),
                                   legend.text=element_text(face = "bold", size=18),
                                   #legend.position=c(0.8, 0.75),
                                   legend.position = 'bottom',
                                   legend.key=element_rect(fill=NA),
                                   legend.key.size=unit(1, "cm"),
                                   legend.key.height=unit(0.8, "cm"))

#bar.runtime = bar.runtime + opts(legend.key.height=unit(3,"line"))
bar.runtime

# Form a new data frame for drawing line plot.
runtime.trend = data.frame(time=numeric(40*6), version=character(240), value=numeric(240),
                           stringsAsFactors=F)
for (i in 1:6) {
  for (j in 1:40) {
    runtime.trend$time[(i-1)*40 + j] = j
    runtime.trend$version[(i-1)*40 + j] = runtime$version[i]
    col = paste('X', j, sep='')
    runtime.trend$value[(i-1)*40 + j] = runtime[col][i,]
  }
}

runtime.line <- ggplot(runtime.trend, aes(x=time, y=value, color=version)) +
  layer(geom='point', size=3) + layer(geom='line', size=1.2) +
  ggtitle('C: Comparison of the running time for 40 iterations') +
  xlab('Iteration (1ms simulation)') + ylab('Runtime (s)')

runtime.line <- runtime.line + theme(axis.title=element_text(size=20, face='bold'),
                                     plot.title=element_text(hjust=0, face='bold', 
                                                             vjust=1, size=16),
                                     plot.title=element_text(hjust=0, face='bold', size=16),
                                     legend.position='none',
                                     axis.text=element_text(size=13, color='blue', face='bold')) # no legend

runtime.line

bar.runtime2 = ggplot(data=runtime, aes(x=version, y=last20ms, fill=version)) +
  geom_bar(stat='identity', width=.7) + # a bar chart, 'stat' indicates we use real name as x value, not count
  xlab('Implementation') + ylab('Runtime (s)') +
  ggtitle('B: Running time of 20ms ~ 40ms')

# Calulate the percentage difference between other implementations
# and the basic implementation.
runtime.diff2 = NULL
base = runtime['last20ms'][1,] # use runtime of the Basic implementation as a base for calculating the difference 
for (i in 1:length(runtime['last20ms'][,1])) {
  runtime.diff2[i] = round((runtime['last20ms'][i,] - base)/base * 100)
}

runtime.diff2 = as.character(runtime.diff2) # covert to chars
runtime.diff2 = paste(runtime.diff2, '%', sep='') # add a trailing %

# set discrete x scale with labels as runtime.diff
bar.runtime2 = bar.runtime2 + 
  scale_x_discrete(breaks=runtime$version, labels=runtime.diff2)

bar.runtime2 = bar.runtime2 + geom_hline(yintercept=base, 
                                       linetype='dotted',
                                       size=.8)

bar.runtime2 <- bar.runtime2 + theme(axis.title=element_text(size=20, face='bold'),
                                   axis.text=element_text(size=13, color='blue', face='bold'),
                                   plot.title=element_text(hjust=0, face='bold', vjust=1, size=16),
                                   legend.title=element_blank(),
                                   legend.text=element_text(face = "bold", size=18),
                                   #legend.position=c(0.8, 0.75),
                                   legend.position = 'none',
                                   legend.key=element_rect(fill=NA))


multiplot(bar.runtime, bar.runtime2, runtime.line, cols=1)
dev.off()