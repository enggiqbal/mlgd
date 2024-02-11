# ImPred

Improves the layout of a given drawing without changing the number of crossings
(tested only for zero crossings drawings)

The jar file takes as input:

--inputgraph = graph file in dot format
--edgerepulsion = double value for edge repulsion force
--nodenodeattraction = double value for node-node attraction force
--edgenoderepulsion = double value for edge-node  repulsion force
--iterations = integer value for number of iterations

example:

java -jar ImPred.jar --inputgraph='Tn.dot' --edgerepulsion=10 --nodenodeattraction=10 --edgenoderepulsion=10 --iterations=300
