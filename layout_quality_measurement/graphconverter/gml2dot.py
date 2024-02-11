#Author
#Felice De Luca
#https://github.com/felicedeluca
import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot


def set_graph_properties(G):
    ''' Extracts the attributes of the vertices and edges from
    the gml data structure
    <tt>return</tt> the graph with the standard dot attributes'''

    graphics = nx.get_node_attributes(G, "graphics")
    # print(graphics)

    for k in nx.nodes(G):

        pos = str(graphics[k]['x'])  +"," + str(graphics[k]['y'])
        nx.set_node_attributes(G, {k:pos}, 'pos')
        nx.set_node_attributes(G, {k:""}, "graphics")
        nx.set_node_attributes(G, {k:k}, "label")

        if('w' in graphics[k].keys() and 'h' in graphics[k].keys()):
            # HERE W and H seem to be inverted
            width = float(graphics[k]['w'])
            height = float(graphics[k]['h'])
            nx.set_node_attributes(G, {k:max(width, height)}, "width")
            nx.set_node_attributes(G, {k:min(width, height)}, "height")

    G = nx.Graph(G)

    return G



g_path = sys.argv[1]
outputpath = sys.argv[2]
g_name = os.path.basename(g_path).split(".")[0]


# Reading graph and subgraph
G = nx.read_gml(g_path)
G = nx.Graph(G)
print(nx.info(G))
G = set_graph_properties(G)
print(nx.info(G))
write_dot(G, outputpath)
print(nx.info(G))
