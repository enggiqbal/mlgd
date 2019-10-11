#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot


g_path = sys.argv[1]
outputpath = sys.argv[2]
g_name = os.path.basename(g_path).split(".")[0]


# Reading graph and subgraph
G = nx.read_gml(g_path)
G = nx.Graph(G)

graphics = nx.get_node_attributes(G, "graphics")
# print(graphics)

for k in graphics.keys():

    pos = str(graphics[k]['x'])  +"," + str(graphics[k]['y'])
    nx.set_node_attributes(G, {k:pos}, 'pos')
    nx.set_node_attributes(G, {k:""}, "graphics")
    nx.set_node_attributes(G, {k:k}, "label")

G = nx.Graph(G)
write_dot(G, outputpath)
