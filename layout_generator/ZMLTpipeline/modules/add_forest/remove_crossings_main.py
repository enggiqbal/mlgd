# Author
# Felice De Luca
# https://www.github.com/felicedeluca

# This script removes the crossings from the given tree
# it searches for a crossing, splits the graph in 3 components removing
# the crossing edges and then scales the smaller component till there is no more
# crossing.


import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import tree_crossings_remover

parameters = list(sys.argv)

# Main Flow
graph_path = parameters[1]

G=nx_read_dot(graph_path)
G=nx.Graph(G)

G = tree_crossings_remover.remove_crossings(G)

write_dot(G, graph_path)




