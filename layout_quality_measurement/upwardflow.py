#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import math

import numpy as np # to compute inner product


def compute_edges_vects(G):
    '''
    Computes the vectors for each edge in G and resturns them in a list.
    '''

    vects_list = []

    pos_dict = nx.get_node_attributes(G, 'pos')

    for e in nx.edges(G):

        (source, target) = e

        x_source = float(pos_dict[source].split(",")[0])
        x_target = float(pos_dict[target].split(",")[0])

        y_source = float(pos_dict[source].split(",")[1])
        y_target = float(pos_dict[target].split(",")[1])

        # The following check is to force the upwardness
        # (if the edges vects) are inverted
        if y_target < y_source:
            x_temp = x_target
            x_target = x_source
            x_source = x_temp

            y_temp = y_target
            y_target = y_source
            y_source = y_temp


        len = [x_source - x_target, y_source - y_target]
        norm = math.sqrt(len[0] ** 2 + len[1] ** 2)
        dir = [len[0]/norm, len[1]/norm]
        vect = [dir[0], dir[1]]

        vects_list.append(vect)


    return vects_list



def compute_upwardflow(G):
    '''
    Computes the Upward flow of the given upward drawing of the graph G.

    This metric determines the proportion of edge segments of a drawing which have a consistent direction.
    Edge segments are used rather than edges, as edges with edge segments of alternating direction are generally considered undesirable.

    The desired direction is usually upwards or downwards with respect to a vertical axis, but the metric makes no assumptions about its orientation. It is assumed that G is a directed graph.

    The notation ei indicates the vector corresponding to the ith directed edge in the drawing:The inner product of two vectors is denoted <v1, v2> : The unit vector parallel with the desired direction is denoted 1 and is considered to point in the direction of desired flow.

    This metric is 0 for non directed graphs.

    <tt>https://pdfs.semanticscholar.org/be7e/4c447ea27e0891397ae36d8957d3cbcea613.pdf</tt>

    '''

    e_vects = compute_edges_vects(G)

    prods_sum = 0

    for e in e_vects:
        ones = [1, 1]
        inn_prod = np.inner(e, ones)

        if inn_prod > 0:
            prods_sum += 1


    flow = prods_sum/len(e_vects)
    flow = round(flow, 3)

    return flow
