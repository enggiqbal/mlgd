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
import random

import vertexmanager


def get_bounding_box(G):

   xmin=0
   ymin=0
   xmax=0
   ymax=0
   ofset=0
   for x in G.nodes():
	   xmin=min(xmin,float(G.node[x]["pos"].split(",")[0]) )
	   xmax=max(xmax,float(G.node[x]["pos"].split(",")[0]) )
	   ymin=min(ymin,float(G.node[x]["pos"].split(",")[1]) )
	   ymax=max(ymax,float(G.node[x]["pos"].split(",")[1]) )
   return  xmin ,ymin ,xmax ,ymax


def translateGraph(G, translation_dx, translation_dy):

    for currVertex in nx.nodes(G):
        vertexmanager.shiftVertex(G.node[currVertex], translation_dx, translation_dy)

    return G

def scale(G, scaling_factor):

    all_pos = nx.get_node_attributes(G, "pos").values()

    coo_x = sorted([float(p.split(",")[0]) for p in all_pos])
    coo_y = sorted([float(p.split(",")[1]) for p in all_pos])
    min_x = float(coo_x[0])
    min_y = float(coo_y[0])

    translateGraph(G, min_x, min_y)

    for currVertex in nx.nodes(G):
        v = G.node[currVertex]
        v_x, v_y = vertexmanager.getCoordinate(v)
        v_x_scaled = v_x * scaling_factor
        v_y_scaled = v_y * scaling_factor
        vertexmanager.setCoordinate(v, v_x_scaled, v_y_scaled)

    return G

def avg_edge_length(G):

    sum_edge_length = 0.0
    edge_count = len(G.edges())

    for edge in G.edges():

        s,t = edge

        s = G.node[s]
        t = G.node[t]

        x_source1, y_source1  = vertexmanager.getCoordinate(s)
        x_target1, y_target1 = vertexmanager.getCoordinate(t)

        curr_length = math.sqrt((x_source1 - x_target1)**2 + (y_source1 - y_target1)**2)

        sum_edge_length += curr_length

    avg_edge_len = sum_edge_length/edge_count
    return avg_edge_len

# Main Flow

graphpath = sys.argv[1]
outputpath = graphpath

# Main Graph
input_graph_name = os.path.basename(graphpath)
graph_name = input_graph_name.split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(graphpath)
nx.set_edge_attributes(G, 'red', 'color')
avg_edge = avg_edge_length(G)
xmin ,ymin ,xmax ,ymax = get_bounding_box(G)
scaling_factor = 100/avg_edge
translateGraph(G, -xmin, -ymin)
G = scale(G, scaling_factor)
G = nx.Graph(G)
write_dot(G, outputpath)
