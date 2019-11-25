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

G=nx_read_dot(graph_path)

for v in nx.nodes(G):
    nx.set_node_attributes(G, {v:0}, "width")
    nx.set_node_attributes(G, {v:0}, "height")
    nx.set_node_attributes(G, {v:0}, "fontsize")

write_dot(G, graph_path)
