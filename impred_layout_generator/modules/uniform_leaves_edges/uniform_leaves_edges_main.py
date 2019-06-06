import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

import uniform_leaves_edges as uniformer

tree_path = sys.argv[1]

# Main Graph
tree_path_name = os.path.basename(tree_path).split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(tree_path)


G = uniformer.unify_leaves_edges_leghths(G)



G = nx.Graph(G)
write_dot(G, tree_path)
