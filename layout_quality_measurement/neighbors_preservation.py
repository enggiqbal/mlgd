#Author
#Felice De Luca
#https://github.com/felicedeluca

import pygraphviz as pgv
import networkx as nx
import math

# import numpy as np
# from numpy import random
# from scipy.spatial import distance
# import time
#
# def euclidean_closest_nodes(node, nodes, k_i):
#     '''
#     Computes the distance matrix between point.
#     '''
#
#     node = np.array(node)
#     nodes = np.array(nodes)
#
#     distances = distance.cdist(node, nodes)
#     indices = np.argpartition(distances, 3)
#
#     return indices


def euclidean_distance(source, target):
    x_source1 = float(source['pos'].split(",")[0])
    x_target1 = float(target['pos'].split(",")[0])

    y_source1 = float(source['pos'].split(",")[1])
    y_target1 = float(target['pos'].split(",")[1])

    geomDistance = math.sqrt((x_source1 - x_target1)**2 + (y_source1 - y_target1)**2)

    return geomDistance


def find_graph_closest_nodes(G, r_g, sourceStr, all_sp):
    closest = []
    # vertices = list(nx.nodes(G))
    # source = G.node[sourceStr]
    # for i in range(0, len(vertices)):
    for target in nx.nodes(G):
        if(target == sourceStr):
            continue
        graph_theoretic_distance = len(all_sp[sourceStr][target])-1
        if(graph_theoretic_distance <= r_g):
            closest.append(target)
    return closest


def find_space_closest_nodes(Gnx, k_i, sourceStr):

    closest = []
    vertices = list(nx.nodes(Gnx))
    source = Gnx.node[sourceStr]

    closest_dict = dict()

    for i in range(0, len(vertices)):

        targetStr = vertices[i]
        target = Gnx.node[targetStr]

        if(target == source):
            continue

        space_distance = euclidean_distance(source, target)

        closest_dict[targetStr] = space_distance


    res = list(sorted(closest_dict, key=closest_dict.__getitem__, reverse=False))

    closest = res[:k_i+1]

    return closest




def compute_neig_preservation(G, weighted=True, all_sp=None):

    if all_sp is None:
        if(weighted):
            # converting weights in float
            all_weights_n = nx.get_node_attributes(G, "weight")
            for nk in all_weights_n.keys():
                all_weights_n[nk] = float(all_weights_n[nk])
            nx.set_node_attributes(G, all_weights_n, "weight")

            all_weights_e = nx.get_edge_attributes(G, "weight")
            for ek in all_weights_e.keys():
                all_weights_e[ek] = float(all_weights_e[ek])
            nx.set_edge_attributes(G, all_weights_e, "weight")
            all_sp = nx.shortest_path(G, weight="weight")
        else:
            all_sp = nx.shortest_path(G)

    r_g = 3


    vertices = list(nx.nodes(G))

    sum = 0

    # all_pos = nx.get_node_attributes(G, "pos")
    # nodes_positions = {}
    # for v in all_pos.keys():
    #     x = float(all_pos[v].split(",")[0])
    #     y = float(all_pos[v].split(",")[1])
    #     nodes_positions[v] = (x, y)

    for i in range(0, len(vertices)):

        sourceStr = vertices[i]
        source = G.node[sourceStr]


        graph_neighbors = find_graph_closest_nodes(G, r_g, sourceStr, all_sp)

        k_i = len(graph_neighbors)

        # x = float(all_pos[sourceStr].split(",")[0])
        # y = float(all_pos[sourceStr].split(",")[1])
        # curr_node_pos = [(x, y)]
        # space_neigobors_new = euclidean_closest_nodes([curr_node_pos], list(nodes_positions.values()), k_i)
        space_neigobors = find_space_closest_nodes(G, k_i, sourceStr)


        vertices_intersection = set(graph_neighbors).intersection(set(space_neigobors))


        vertices_union = set(graph_neighbors).union(set(space_neigobors))

        sum  += len(vertices_intersection)/len(vertices_union)

    pres = (1/len(vertices))*sum

    pres = round(pres, 3)

    return pres
