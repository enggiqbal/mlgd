import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import tkinter
from tkinter import font as tkFont

tkinter.Frame().destroy()  # Enough to initialize resources


# Main Flow
graph_path = sys.argv[1]
outputpath = graph_path

input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]

G = nx_read_dot(graph_path)
G=nx.Graph(G)

v_labels = nx.get_node_attributes(G, "label")
v_levels = nx.get_node_attributes(G, "level")
font_sizes=[30,25,20,15,12,10,9,8]
# use font size array

for v in v_labels.keys():

    v_label = v_labels[v]
    v_level = 0
    if  v in v_levels.keys():
        v_level = int(v_levels[v])-1

    font_size = font_sizes[v_level]

    arial36b = tkFont.Font(family='Arial', size=font_size, weight='normal')

    width = arial36b.measure(v_label)
    height = arial36b.metrics('linespace')
    nx.set_node_attributes(G, {v:width/72}, "width")
    nx.set_node_attributes(G, {v:height/72}, "height")
    nx.set_node_attributes(G, {v:font_size}, "fontsize")


write_dot(G, outputpath)
