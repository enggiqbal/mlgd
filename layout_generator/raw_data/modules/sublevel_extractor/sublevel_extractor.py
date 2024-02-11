import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

def save_graph(G, outputpath):
    G = nx.Graph(G)
    write_dot(G, outputpath)


def extract_level(G, level):
    G = nx.Graph(G)
    levels_info = nx.get_node_attributes(G, 'level')
    to_remove_nodes = [n for n in levels_info.keys() if int(levels_info[n]) > level]
    G.remove_nodes_from(to_remove_nodes)
    return G


g_path = sys.argv[1]
outputpath = sys.argv[2]
g_name = os.path.basename(g_path).split(".")[0]


# Reading graph and subgraph
G = nx_read_dot(g_path)
G = nx.Graph(G)

levels_info = nx.get_node_attributes(G, 'level')
levels =  sorted(list(set(levels_info.values())))

for level in levels:
    level = int(level)
    sub_G = extract_level(G, level)
    save_graph(sub_G, outputpath+g_name+"_"+str(level)+"_final.dot")
