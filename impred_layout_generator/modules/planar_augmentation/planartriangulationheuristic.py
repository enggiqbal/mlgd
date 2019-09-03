#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os
import math

import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import edge_crossing


# Main Flow

graph_path = sys.argv[1]
output_path = sys.argv[2]

G = nx_read_dot(graph_path)
G = nx.Graph(G)
nx.set_edge_attributes(G, 'red', 'color')

nodes = list(nx.nodes(G))
all_edges=[]

for v_id in range(0, len(nodes)):
    v = nodes[v_id]

    for u_id in range(v_id+1, len(nodes)):
        u = nodes[u_id]

        all_edges.append((u,v))


edges = list(nx.edges(G))

for to_add_e in all_edges:

    u, v = to_add_e

    if ((u, v) in edges) or ((v, u) in edges):
        continue

    if edge_crossing.count_crossings(G, edges_to_compare=[to_add_e]):
        continue

    G.add_nodes_from([u, v])
    print("edge added")



G=nx.Graph(G)
write_dot(G, output_path)
