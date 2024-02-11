#!/bin/bash

for file in "/Users/felicedeluca/Developer/UofA/mlgd/pipeline/impred_output2"/*.dot
do
  echo "$file"
  python3 metricscomputer.py "$file" "/Users/felicedeluca/Developer/UofA/mlgd/pipeline/impred_output2/measures.txt"
done
