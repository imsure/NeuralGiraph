#!/bin/bash

declare -a runtimes
declare -a fr_mean
declare -a fr_std
declare -a fr_mean_e
declare -a fr_mean_i

round()
{
    echo $(printf %.$2f $(echo "scale=$2;(((10^$2)*$1)+0.5)/(10^$2)" | bc))
};


loop=0
num_runs=0
work_dir=./

while [ $loop -le $num_runs ]
do
    echo "################# Starting Loop" $loop "###########################"

    # Generate input
    $work_dir/input.sh
    hadoop fs -rm -r neuron_input/_logs

    # Run Giraph job to evolve the neural netork for 500 ms.
    start=$(date +%s)
    $work_dir/run_intermediate_output.sh
    end=$(date +%s)
    runtimes[$loop]=$(( $end - $start ))

    # Run MR job for transforming the Giraph output for hive
    $work_dir/output4hive.sh

    # Create hive table for post-analysis
    hive -f $work_dir/hive/create_table.q

    # Execute hive queries for computing mean and std
    # of the average firing rate for all neurons
    stats_all=$(hive -f $work_dir/hive/firing_rate_stats.q)
    array_all=($stats_all)
    fr_mean[$loop]=$(round ${array_all[0]} 2)
    fr_std[$loop]=$(round ${array_all[1]} 2)

    # Execute hive queries for computing mean and std
    # of the average firing rate for excitatory neurons
    stats_e=$(hive -f $work_dir/hive/firing_rate_stats_e.q)
    array_e=($stats_e)
    fr_mean_e[$loop]=$(round ${array_e[0]} 2)
    fr_std_e[$loop]=$(round ${array_e[1]} 2)

    
    # Execute hive queries for computing mean and std
    # of the average firing rate for inhibitory neurons
    stats_i=$(hive -f $work_dir/hive/firing_rate_stats_i.q)
    array_i=($stats_i)
    fr_mean_i[$loop]=$(round ${array_i[0]} 2)
    fr_std_i[$loop]=$(round ${array_i[1]} 2)

    printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" \
	$loop ${runtimes[$loop]} ${fr_mean[$loop]} ${fr_std[$loop]} \
	${fr_mean_e[$loop]} ${fr_std_e[$loop]} ${fr_mean_i[$loop]} ${fr_std_i[$loop]}
    echo "################# Ending Loop" $loop "###########################"
    loop=$((loop+1))
done

loop=0
printf "Loop\tRuntime\tMean\tStd\tMean_E\tStd_E\tMean_I\tStd_I\n" >> small.stats
while [ $loop -le $num_runs ]
do
    #echo "Loop" $loop ":"
    #echo "runtime(s):" ${runtimes[$loop]}
    #echo "mean firing rate:" ${fr_mean[$loop]}
    #echo "std firing rate:" ${fr_std[$loop]}

    printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" \
	$loop ${runtimes[$loop]} ${fr_mean[$loop]} ${fr_std[$loop]} \
	${fr_mean_e[$loop]} ${fr_std_e[$loop]} ${fr_mean_i[$loop]} ${fr_std_i[$loop]} >> small.stats

    loop=$((loop+1))
done
