import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import math
import random

import leavesoverlapremoval


def getCoordinate(vertex):
    """Returns the coordinate of the given vertex."""

    x = float(vertex['pos'].split(",")[0])
    y = float(vertex['pos'].split(",")[1])

    return x, y

def compute_edge_length(G, edge):
    """Computes the length of the given edge
    the position of the vertices is given by the <tt>pos</tt> attribute
    <strong>return</strong> length of the given edge as a double
    """

    s1_id,t1_id = edge

    nodes_position = nx.get_node_attributes(G, 'pos')

    s1_pos = nodes_position[s1_id]
    t1_pos = nodes_position[t1_id]


    x_source1 = float(s1_pos.split(",")[0])
    x_target1 = float(t1_pos.split(",")[0])

    y_source1 = float(s1_pos.split(",")[1])
    y_target1 = float(t1_pos.split(",")[1])

    curr_length = math.sqrt((x_source1 - x_target1)**2 + (y_source1 - y_target1)**2)

    return curr_length


def avg_edge_length(G):
    """Computes the average length of the given edges set
    <strong>return</strong> average length of the edges as a double
    """

    edges = G.edges()
    sum_edge_length = 0.0
    edge_count = len(edges)

    for e in edges:
        curr_length = compute_edge_length(G, e)
        sum_edge_length += curr_length

    avg_edge_len = sum_edge_length/edge_count
    return avg_edge_len


def extract_leaves(G):
    """Extracts from <tt>G</tt> the vertices with degree 1, i.e. the leaves."""

    leaves=[]

    for n in nx.nodes(G):
        if len(list(G.neighbors(n)))<=1:
            leaves.append(n)

    return leaves


def unify_leaves_edges_leghths(G, value=-1):
    """This function sets the length of the edges incident on the leaves
    of a tree to a fixed value.
    The idea is to position the leaves next to their parent to save space.
    The edges are set to the given <tt>value</tt> parameter. If no value is given
    or it is set to -1 then the edges are set to half the length of the average
    edge lenght."""


    # If the edge length value is not given set it half the length of the
    # average length value
    if value == -1:
        avgEdgeLength = avg_edge_length(G)
        value = avgEdgeLength/3

    leaves = extract_leaves(G)

    to_be_shortened_edges = list(nx.edges(G, leaves))

    print("Shortening " + str(len(to_be_shortened_edges)) + " edges.")

    for e in to_be_shortened_edges:

        if compute_edge_length(G, e) <= value:
            continue

        t_id, s_id = e

        s = G.node[s_id]
        t = G.node[t_id]

        origin = s
        leaf = t

        origin_id = s_id
        leaf_id = t_id

        if s in leaves:
            origin = t
            origin_id = t_id

            leaf = s
            leaf_id = s_id


        x_origin, y_origin = getCoordinate(origin)
        x_leaf, y_leaf = getCoordinate(leaf)

        original_edge_length = math.sqrt((x_origin-x_leaf)**2 + (y_origin-y_leaf)**2)

        x_num = value * (x_leaf - x_origin)
        y_num = value * (y_leaf - y_origin)

        x_den = math.sqrt((x_origin-x_leaf)**2 + (y_origin-y_leaf)**2)
        y_den = math.sqrt((x_origin-x_leaf)**2 + (y_origin-y_leaf)**2)

        x_leaf_new = x_origin + x_num/x_den
        y_leaf_new = y_origin + y_num/y_den


        G.node[leaf_id]['pos'] = str(x_leaf_new)+","+str(y_leaf_new)

        # ovelapping = leavesoverlapremoval.get_overlapping_vertices(G, with_vertices=[origin_id, leaf_id])


    return G
