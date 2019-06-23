#!/bin/bash

for file in $PWD/*.dot
do
  echo "$file"
  python3 /extra/hossain/MLGD/mlgd/measurement/metricscomputer.py "$file" "$PWD/measures.txt"
done


