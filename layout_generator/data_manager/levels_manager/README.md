# Forest extractor

Given two graphs T1 and T2 this script extract from T2 the subgraph not included in T1.

The input are the paths to two dot files in order T1 and T2

The output is a dot file containing the subgraph of T2 not included in T1
with suffix '_forest.dot'

example of usage:

python3 levels_extractor.py "t1.dot" "t2.dot" 
