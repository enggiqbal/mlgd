#zigzagness
import networkx as nx
import math
import numpy as np


def extract_leaves(G):
    """Extracts from <tt>G</tt> the vertices with degree 1, i.e. the leaves."""

    leaves=[]

    for n in nx.nodes(G):
        if len(list(G.neighbors(n)))<=1:
            leaves.append(n)

    return leaves


def compute_euclidean_distance(s, t):
    """Computes the distance between the two points <tt>s</tt> and <tt>t</tt>.
    <strong>return</strong> distance between the given vertices
    """

    s_pos = s["pos"]
    t_pos = t["pos"]


    x_s = float(s_pos.split(",")[0])
    x_t = float(t_pos.split(",")[0])

    y_s = float(s_pos.split(",")[1])
    y_t = float(t_pos.split(",")[1])

    curr_distance = math.sqrt((x_s - x_t)**2 + (y_s - y_t)**2)

    return curr_distance

def compute_distance_on_graph(G, s_id, t_id):
    """ Computes the sum of the length in the shortest path between <tt>s</tt> and <tt>t</tt>.
    If the shortest path are more than one the shorter in edge lengths is considered
    <tt>return</tt> a double value of the length of the shortestpath between <tt>s</tt> and <tt>t</tt>
    """
    all_sp = list(nx.all_shortest_paths(G, source=s_id, target=t_id))

    min_sp = float("inf")

    for sp in all_sp:
        curr_length = 0
        for s_index in range(0, len(sp)-1):
            t_index = s_index+1

            s_id = sp[s_index]
            t_id = sp[t_index]

            s = G.node[s_id]
            t = G.node[t_id]

            curr_length += compute_euclidean_distance(s, t)

        min_sp = min(min_sp, curr_length)

    return min_sp


def compute_smoothness(G):
    """
    Smoothness is the difference between the Euclidean Distance and the
    distance on the graph of any pair of leaves of the given Tree
    """

    leaves = extract_leaves(G)

    total_error=0

    sp_avg_len = nx.average_shortest_path_length(G)

    for s_index in range(0, len(leaves)):
        for t_index in range(s_index+1, len(leaves)):

            s_id = leaves[s_index]
            t_id = leaves[t_index]

            s = G.node[s_id]
            t = G.node[t_id]

            curr_distance = compute_euclidean_distance(s, t)
            path_distance = compute_distance_on_graph(G, s_id, t_id)
            sp_length = nx.shortest_path_length(G, s_id, t_id)

            penalization = (math.e**(-sp_avg_len))

            curr_error = (path_distance-curr_distance)*penalization

            total_error += curr_error

    print(total_error)
