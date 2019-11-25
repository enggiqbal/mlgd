#Author
#Felice De Luca
#https://github.com/felicedeluca

"""
Fetch from a given graph the attributes of the vertices and
copies them to the new graph.
Fetched properties are given as input.
The graph with the fetched properties overrides the old one.

standard parameters:

pos width height pos label weigth

#Author
#Felice De Luca
#https://github.com/felicedeluca
"""

import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

parameters = list(sys.argv)

# Main Flow
graph_path = parameters[1]
tree_path = parameters[2]
outputpath = tree_path

properties_to_fetch = ["label", "weight", "fontsize", "level", "width","height","label"]
if len(parameters) > 3:
    properties_to_fetch = parameters[3].split(",")

# Fetching parameters list
input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]


print("fetching labels",graph_path,  tree_path, properties_to_fetch, ": ", end=" ")


from_graph=nx_read_dot(graph_path)
from_graph=nx.Graph(from_graph)

to_graph=nx_read_dot(tree_path)
to_graph=nx.Graph(to_graph)

for param in properties_to_fetch:
    nx.set_node_attributes(to_graph, nx.get_node_attributes(from_graph, param), param)
    print(param, end=" ")

print("")

write_dot(to_graph, tree_path)
