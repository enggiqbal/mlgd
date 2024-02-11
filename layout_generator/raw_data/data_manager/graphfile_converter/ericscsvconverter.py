#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import math

graphpath = sys.argv[1]

path_folder = os.path.dirname(graphpath)
input_file_name = os.path.basename(graphpath)
graph_name = input_file_name.split(".")[0]

graphs_folder = path_folder+'/graphs'
layouts_folder = path_folder+'/layouts'

print(graphs_folder)
print(layouts_folder)


if not os.path.exists(graphs_folder):
    os.makedirs(graphs_folder)
if not os.path.exists(layouts_folder):
    os.makedirs(layouts_folder)

print(path_folder)

G = nx_read_dot(graphpath)

all_pos = nx.get_node_attributes(G, "pos")
# all_pos_dict = dict((k, (float(all_pos[k].split(",")[0]), float(all_pos[k].split(",")[1]))) for k in all_pos.keys())
vertices_ids = sorted(all_pos.keys())

edges_str = ""
for currEdge in nx.edges(G):
    edges_str = edges_str + currEdge[0]+","+currEdge[1] + "\n"

layout_str = ""
for value in vertices_ids:
    layout_str = layout_str + all_pos[value] + "\n"



f_graph = open(graphs_folder+'/'+graph_name+"_graph.csv", "w")
f_graph.write(edges_str)

f_layout = open(layouts_folder+'/'+graph_name+"_layout.csv", "w")
f_layout.write(layout_str)
