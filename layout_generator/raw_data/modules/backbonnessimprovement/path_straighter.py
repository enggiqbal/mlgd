import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

import treestraightner


# Main Flow

graphpath = sys.argv[1]
outputpath = sys.argv[2]

# Main Graph
input_graph_name = os.path.basename(graphpath)
graph_name = input_graph_name.split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(graphpath)
nx.set_edge_attributes(G, 'red', 'color')

treestraightner.rectify_Tree(G)



G = nx.Graph(G)
write_dot(G, outputpath)
