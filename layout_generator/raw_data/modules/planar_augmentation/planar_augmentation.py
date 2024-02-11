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

import crossings


# Main Flow

subgraph_path = sys.argv[1]
graph_path = sys.argv[2]
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
# subG.remove_edges_from(subG.selfloop_edges())
subG=nx.Graph(subG)


G = nx_read_dot(graph_path)
nx.set_edge_attributes(G, 'black', 'color')
# G.remove_edges_from(G.selfloop_edges())
G=nx.Graph(G)


induced_G = nx.Graph(G.subgraph(nx.nodes(subG)))


edges_to_be_added = list(nx.edges(induced_G))

fake_edges_levels = dict()

v_pos = nx.get_node_attributes(subG, "pos")

for e in edges_to_be_added:

    (s1,t1) = (e[0], e[1])
    if s1 == t1:
        continue

    if e in list(nx.edges(subG)):
        continue

    single_edge_list = [e]

    crossing = crossings.count_crossings(G=subG, edge_list_H=single_edge_list, stop_when_found=True)

    if crossing > 0:
        continue

    (u, v) = e

    if u not in nx.nodes(subG) or v not in nx.nodes(subG):
        continue

    x_source = float(v_pos[u].split(",")[0])
    y_source = float(v_pos[u].split(",")[1])

    x_target = float(v_pos[v].split(",")[0])
    y_target = float(v_pos[v].split(",")[1])

    geomDistance = math.sqrt((x_source - x_target)**2 + (y_source - y_target)**2)

    if geomDistance < 0.01:
        continue

    subG.add_edges_from(single_edge_list)
    fake_edges_levels[e] = 1

crossing = crossings.count_crossings(G=subG, edge_list_H=single_edge_list, stop_when_found=True)
selfloops=subG.selfloop_edges()
subG.remove_edges_from(list(selfloops))
subG.remove_edges_from(selfloops)
subG = nx.Graph(subG)


print("saving in ", outputpath)
write_dot(subG, outputpath)

print("end")
