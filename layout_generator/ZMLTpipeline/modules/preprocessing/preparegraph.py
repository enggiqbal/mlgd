#Author
#Felice De Luca
#https://github.com/felicedeluca

"""
This script is to clean and prepare the graph for the next steps.
First it changes the id of the vertices to integers, and sets
the string of the id as label_attribute
Then it extracts the levels if the 'level' info is present
Finally it extracts the forests if the 'level info is present'

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

def extract_level(G, level):
    G = nx.Graph(G)
    levels_info = nx.get_node_attributes(G, 'level')
    to_remove_nodes = [n for n in levels_info.keys() if int(levels_info[n]) > level]
    G.remove_nodes_from(to_remove_nodes)
    return G


parameters = list(sys.argv)

# Main Flow
graph_path = parameters[1]
outputpath = parameters[2]
input_graph_name = os.path.basename(graph_path)
g_name = os.path.basename(graph_path).split(".")[0]


G=nx_read_dot(graph_path)
G=nx.Graph(G)

print(nx.info(G))


labels_info = nx.get_node_attributes(G, 'label')
G = nx.convert_node_labels_to_integers(G, label_attribute='label')

if len(labels_info.keys()) > 0:
    nx.set_node_attributes(G, labels_info, 'label')


#TODO add label size info: width height font name font size

write_dot(G, outputpath+g_name+"intid.dot")


levels_info = nx.get_node_attributes(G, 'level')
levels =  sorted(list(set(levels_info.values())))
level_to_graph_map = dict()

if len(levels) < 1 :
    print("No level info")
    quit()

print("extracting levels")

for level in levels:
    level = int(level)
    sub_G = extract_level(G, level)
    level_to_graph_map[level] = nx.Graph(sub_G)
    write_dot(sub_G, outputpath+g_name+"_"+str(level)+".dot")


print("extracting forests")

print(level_to_graph_map.keys())

for i in range(0, len(levels)-1):

    prev_level_index = int(levels[i])
    next_level_index = int(levels[i+1])

    G_t1 = nx.Graph(level_to_graph_map[prev_level_index])
    G_t2 = nx.Graph(level_to_graph_map[next_level_index])

    commonVertices = set(set(G_t1.nodes()) & set(G_t2.nodes()))
    t2_only_vertices = set(G_t2.nodes()).difference(set(G_t1.nodes()))
    forest_vertices = set()
    forest_vertices = set(G_t2.nodes()).difference(set(G_t1.nodes()))

    for v_id in commonVertices:

        v_adjs = set(nx.neighbors(G_t2, v_id))

        adjs_new = v_adjs.difference(commonVertices)

        if len(adjs_new)>0:
            # v is connected to new vertices
            forest_vertices.add(v_id)

    # print("forest vertices", forest_vertices)

    t2_forest = nx.Graph(nx.subgraph(G_t2, forest_vertices))
    t2_forest.remove_edges_from(nx.edges(G_t1))

    write_dot(t2_forest, outputpath+g_name+"_"+str(next_level_index)+"_forest.dot")
