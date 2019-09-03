#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import leavesoverlapremoval

# Main Flow
graph_path = sys.argv[1]
outputpath = sys.argv[2]

G = nx_read_dot(graph_path)
G=nx.Graph(G)

leavesoverlapremoval.remove_leaves_overlap(G)

write_dot(G, outputpath)
