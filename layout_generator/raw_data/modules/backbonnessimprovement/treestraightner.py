import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

import vertexmanager

def translateGraph(G, translation_dx, translation_dy):

    for currVertex in nx.nodes(G):
        vertexmanager.shiftVertex(G.node[currVertex], translation_dx, translation_dy)

    return G

def extract_leaves(G):
    """Extracts from <tt>G</tt> the vertices with degree 1, i.e. the leaves."""

    leaves=[]

    for n in nx.nodes(G):
        if len(list(G.neighbors(n)))<=1:
            leaves.append(n)

    return leaves

def rotate(G, angle):

    angle = math.radians(angle)

    for currVertex in nx.nodes(G):
        x, y = vertexmanager.getCoordinate(G.node[currVertex])
        x_rot = x*math.cos(angle) - y*math.sin(angle)
        y_rot = x*math.sin(angle) + y*math.cos(angle)
        vertexmanager.setCoordinate(G.node[currVertex], x_rot, y_rot)

    return G


def angle_three_vertices(p1x, p1y, p2x, p2y, p3x, p3y):

    result = math.atan2(p3y - p1y, p3x - p1x) - math.atan2(p2y - p1y, p2x - p1x);
    return result


def getAngleOfLineBetweenTwoPoints(point1X, point1Y, point2X, point2Y):
        xDiff = point2X - point1X
        yDiff = point2Y - point1Y
        return math.degrees(math.atan2(yDiff, xDiff))

def rectify_Tree(G):

    leaves = extract_leaves(G)

    shortest_sp_len = float("-inf")
    shortest_sp = []

    for i in range(0, len(leaves)):
        for j in range(i+1, len(leaves)):

            source_id = leaves[i]
            target_id = leaves[j]

            source = G.node[source_id]
            target = G.node[target_id]

            curr_sp_len = nx.shortest_path_length(G, source=source_id, target=target_id)

            if(curr_sp_len>shortest_sp_len):
                shortest_sp_len = curr_sp_len

                shortest_sp = nx.shortest_path(G, source=source_id, target=target_id)


    print(shortest_sp_len)
    print(shortest_sp)
    return G
