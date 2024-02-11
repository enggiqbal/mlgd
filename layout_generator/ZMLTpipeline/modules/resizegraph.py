# Author
# Felice De Luca
# https://www.github.com/felicedeluca

import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random


def getCoordinate(vertex):

    x = float(vertex['pos'].split(",")[0])
    y = float(vertex['pos'].split(",")[1])

    return x, y


def setCoordinate(vertex, x, y):

    vertex['pos'] = str(x)+","+str(y)

    return x, y


def shiftVertex(vertex, dx, dy):

    x, y = getCoordinate(vertex)

    setCoordinate(vertex, x+dx, y+dy)

    return getCoordinate(vertex)



def translateGraph(G, translation_dx, translation_dy):

    for currVertex in nx.nodes(G):
        shiftVertex(G.node[currVertex], translation_dx, translation_dy)

    return G

def boundingBox(G):

    all_pos = nx.get_node_attributes(G, "pos").values()

    coo_x = sorted([float(p.split(",")[0]) for p in all_pos])
    coo_y = sorted([float(p.split(",")[1]) for p in all_pos])

    min_x = float(coo_x[0])
    max_x = float(coo_x[-1])

    min_y = float(coo_y[0])
    max_y = float(coo_y[-1])

    width = abs(max_x - min_x)
    height = abs(max_y - min_y)

    return width, height

def scale(G, scaling_factor):

    for currVertex in nx.nodes(G):
        v = G.node[currVertex]
        x = float(v['pos'].split(",")[0])
        y = float(v['pos'].split(",")[1])
        v_x_scaled = x * scaling_factor
        v_y_scaled = y * scaling_factor
        v['pos'] = str(v_x_scaled)+","+str(v_y_scaled)

    return G

# Main Flow

graphpath = sys.argv[1]
max_side = int(sys.argv[2])

# Main Graph
input_graph_name = os.path.basename(graphpath)
graph_name = input_graph_name.split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(graphpath)


width, height = boundingBox(G)

longest_side = max(width, height)

scaling_factor = max_side/longest_side

G = scale(G, scaling_factor)

width, height = boundingBox(G)
longest_side_after = max(width, height)


print("Scaling Graph from ", longest_side, "to", longest_side_after)


write_dot(G, graphpath)
