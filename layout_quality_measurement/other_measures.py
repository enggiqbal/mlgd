#Author
#Felice De Luca
#https://github.com/felicedeluca

import pygraphviz as pgv
import networkx as nx

import math

def vertex_degree(G):

    degree = sorted(dict(nx.degree(G)).values())[-1]

    return degree


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

    return (width, height)


def aspectRatio(G):

    bb = boundingBox(G)

    aspectRatio = bb[0]/bb[1]

    return aspectRatio


def diameter(G):

    return nx.diameter(G)
