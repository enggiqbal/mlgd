import sys
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.drawing.nx_agraph import write_dot
import os 
output_prefix="without_ds_topics"
filename='../data/datasets/topics/orginal/Topics_Graph_Connected.dot'
G=nx.Graph(pgv.AGraph(filename))
H=nx.connected_component_subgraphs(G)
print("connected subgraph ", len(list(H)))
print("nx.average_shortest_path_length(G)", nx.average_shortest_path_length(G))

c=nx.algorithms.degree_centrality(G)

s=sorted(c.items(), key=lambda x: x[1], reverse=True)
print(len(G.nodes()))
 
top20Nodes=s[0:20]
for n in top20Nodes:
    print(n[0], G.nodes[n[0]], G.degree(n[0]))
listofnodes_to_remove=[ 'machine learning','artificial intelligence','data mining','optimization']
Gsub=G.copy()
for n in G.node():
    if G.nodes[n]["label"] in listofnodes_to_remove:
        Gsub.remove_node(n)
print(len(Gsub.nodes()))
print("nx.average_shortest_path_length(Gsub)", nx.average_shortest_path_length(Gsub))


H=nx.connected_component_subgraphs(Gsub)

print("connected subgraph after remove", len(list(H)))

Gc = sorted(nx.connected_component_subgraphs(Gsub), key=len)
for g in list(Gc):
    print(len(g))
'''

write_dot(Gsub,"network_without_ml.dot")

os.system("sfdp -Goverlap=prism -Gstart=123 -Ecolor=white network_without_ml.dot -Tsvg > "+output_prefix+".svg")
os.system("sfdp -Goverlap=prism -Gstart=123  -Ecolor=white "+filename+" -Tsvg > Gout.svg")
'''