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
h_path = sys.argv[2]
outputpath = sys.argv[3]
g_name = os.path.basename(g_path).split(".")[0]


# Reading graph and subgraph
G=nx_read_dot(g_path)
G = nx.Graph(G)

H=nx_read_dot(h_path)
H=nx.Graph(H)


glevels=nx.get_node_attributes(G, "level")
glabels=nx.get_node_attributes(G,'label')
gfontsize=nx.get_node_attributes(G, "fontsize")
gweight=nx.get_node_attributes(G, "weight")
gwidth=nx.get_node_attributes(G, "width")
gheight=nx.get_node_attributes(G, "height")
gfontname=nx.get_node_attributes(G, "fontname")

levels_n = {}
id_n={}
fontsize_n={}
weight_n={}
width_n={}
height_n={}
fontname_n={}


for k in glabels.keys():
    levels_n[glabels[k]]=glevels[k]
    id_n[glabels[k]]=k
    fontsize_n[glabels[k]]=gfontsize[k]
    weight_n[glabels[k]]=gweight[k]
    height_n[glabels[k]]=gheight[k]
    fontname_n[glabels[k]]=gfontname[k]
    width_n[glabels[k]]=gwidth[k]

nx.set_node_attributes(H, levels_n, "level")
nx.set_node_attributes(H, id_n, "identifier")

nx.set_node_attributes(H, fontsize_n, "fontsize")
nx.set_node_attributes(H, weight_n, "weight")
nx.set_node_attributes(H, width_n, "width")
nx.set_node_attributes(H, height_n, "height")
nx.set_node_attributes(H, fontname_n, "fontname")


write_dot(H, outputpath)
