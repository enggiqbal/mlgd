#Author
#Felice De Luca
#https://github.com/felicedeluca

# This script removes the crossings from the given tree
# it searches for a crossing, splits the graph in 3 components removing
# the crossing edges and then scales the smaller component till there is no more
# crossing.


import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

import edge_crossing as crossings

import vertexmanager

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

    for currVertex in nx.nodes(G):
        v = G.node[currVertex]
        v_x, v_y = vertexmanager.getCoordinate(v)
        v_x_scaled = v_x * scaling_factor
        v_y_scaled = v_y * scaling_factor
        vertexmanager.setCoordinate(v, v_x_scaled, v_y_scaled)

    return G


def remove_crossings(G):

    while len(crossings.count_crossings_single_graph(G)):

        crs = crossings.count_crossings_single_graph(G)

        current_crossing_edges = crs[0]

        G_copy = G.copy()

        # Removing edges to separate graph
        G_copy.remove_edges_from(current_crossing_edges)

        # Getting the smaller component to be scaled
        smaller_component_vertices = list([c for c in sorted(nx.connected_components(G_copy), key=len, reverse=True)][-1])
        # print(smaller_component_vertices)

        main_edge = ""
        main_vertex = ""
        other_vertex = ""

        # Getting the edge connecting the main and the smaller component
        for curr_edge in current_crossing_edges:
            s = curr_edge[0]
            t = curr_edge[1]

            if s in smaller_component_vertices:
                main_vertex = t
                other_vertex = s
                main_edge = curr_edge
                break

            if t in smaller_component_vertices:
                main_vertex = s
                other_vertex = t
                main_edge = curr_edge
                break


        # print("main: "  + main_vertex)
        # print("other: " + other_vertex)
        # print(main_edge)

        # Translating the graph for better scaling
        translation_dx, translation_dy = vertexmanager.getCoordinate(G.node[main_vertex])
        translateGraph(G, -translation_dx, -translation_dy)


        subcomponet_vertices = smaller_component_vertices
        subcomponet_edges = G.subgraph(subcomponet_vertices).copy().edges()

        H = nx.Graph()

        H.add_nodes_from(list(subcomponet_vertices))
        H.add_node(main_vertex)
        nx.set_node_attributes(H, nx.get_node_attributes(G, 'pos'), 'pos')

        H.add_edges_from(list(subcomponet_edges))
        H.add_edge(main_vertex, other_vertex)
        # print(nx.info(H))

        # print(nx.get_node_attributes(H, 'pos'))
        scale(H, 0.5)
        # print(nx.get_node_attributes(H, 'pos'))

        nx.set_node_attributes(G, nx.get_node_attributes(H, 'pos'), 'pos')

        # print("changed")
    print("crossings:" + str(len(crossings.count_crossings_single_graph(G))))
    return G
