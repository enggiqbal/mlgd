#!/bin/bash

### script to run an mpi job using 12-core or less (using only one 12-core node)### Set the job name
#PBS -N lastfmreyan

### Specify the PI group for this job
#PBS -W group_list=kobourov

### Request email when job begins and ends - commented out in this case
# PBS -m bea
### Specify email address to use for notification - commented out in this case
#PBS -M felicedeluca@email.arizona.edu

### Set the queue for this job as windfall
#PBS -q standard
### Set the number of nodes, cores and memory that will be used for this job
#PBS -l select=5:ncpus=10:mem=60gb

### Specify "wallclock time" required for this job, hhh:mm:ss
#PBS -l walltime=60:00:0

### Specify total cpu time required for this job, hhh:mm:ss
### total cputime = walltime * ncpus #PBS -l cput=60:00:00
### Load required modules/libraries if needed (openmpi example)
### Use "module avail" command to list all available modules
module load singularity

cd dev/ZMLTforHPC/

# run the program
singularity exec ../gdocker.simg bash  run_pipeline.sh

exit
