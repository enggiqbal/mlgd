#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

# Main Flow
graph_path = sys.argv[1]
subgraph_path = sys.argv[2]
outputpath = sys.argv[3]

# Main Graph
input_subgraph_name = os.path.basename(subgraph_path)
subgraph_name = input_subgraph_name.split(".")[0]


# Sub Graph to be added
input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]

# Reading graph and subgraph
subG = nx_read_dot(subgraph_path)
nx.set_edge_attributes(subG, 'red', 'color')

G = nx_read_dot(graph_path)
pos = nx.get_node_attributes(G, 'pos')

nx.set_node_attributes(subG, pos, 'pos')

induced_G = nx.Graph(subG)

# print(nx.info(G))
print("saving in ", outputpath)
write_dot(induced_G, outputpath)

print("end")
