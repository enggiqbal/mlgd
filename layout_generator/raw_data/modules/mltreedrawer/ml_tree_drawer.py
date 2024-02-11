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


desired_edge_length = 100;

def extract_leaves(G):
    """Extracts from <tt>G</tt> the vertices with degree 1, i.e. the leaves."""

    leaves=[]

    for n in nx.nodes(G):
        if len(list(G.neighbors(n)))<=1:
            leaves.append(n)

    return leaves

def get_all_leaves_paths(G):

    leaves = extract_leaves(G)
    shortest_paths={}

    for s_index in range(0, len(leaves)):
        for t_index in range(s_index+1, len(leaves)):

            s = leaves[s_index]
            t = leaves[t_index]

            curr_len = nx.shortest_path_length(G, source=s, target=t)
            curr_sp = nx.shortest_path(G, source=s, target=t)

            existing_keys = shortest_paths.keys()

            if(curr_len in existing_keys):
                shortest_paths[int(curr_len)].append(curr_sp)
            else:
                shortest_paths[curr_len] = [curr_sp]

    return shortest_paths;

def extract_path_in_G_starting_from_v(G, vertices, v):

    ordered_v = [v]

    while len(ordered_v) < len(vertices):
        curr_v = ordered_v[-1]
        adj_v_in_path = set(nx.neighbors(G, curr_v)).intersection(vertices)
        for curr_adj in adj_v_in_path:
            ordered_v.append(curr_adj)
    return ordered_v


def place_path(path, angle, edge_length, origin_x, origin_y, start_at_origin=False):

    lp_pos = {}
    placed_vertices = []
    i = 1

    if(start_at_origin):
        i = 0

    for v in path:
        distance = edge_length*i
        x = origin_x + (distance)*math.cos(angle);
        y = origin_y + (distance)*math.sin(angle)
        lp_pos[v] = str(x) + "," + str(y)
        placed_vertices.append(v)
        i += 1
    return lp_pos


tree_path = sys.argv[1]

# Main Graph
tree_path_name = os.path.basename(tree_path).split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(tree_path)

placed_vertices = set()

leaves_paths = get_all_leaves_paths(G)
lens = sorted(leaves_paths.keys(), reverse=True)

# Place one longest path first
longest_index = lens[0]
angle = 0
origin_x = 0
origin_y = 0
longest_path = leaves_paths[longest_index].pop(0)
lp_pos = place_path(longest_path, angle, desired_edge_length, origin_x, origin_y, start_at_origin=True)
nx.set_node_attributes(G, lp_pos, "pos")


while(len(leaves_paths.keys()) > 0):
    lens = sorted(leaves_paths.keys(), reverse=True)
    for leng_index in range(0, len(lens)):

        curr_length = lens[leng_index]
        curr_paths = leaves_paths[curr_length]


        print("before len: " + str(len(leaves_paths[curr_length])))

        placed_pos = nx.get_node_attributes(G, "pos")
        placed_vertices = placed_vertices.union(set(placed_pos.keys()))

        placed = False

        for path_index in range(0, len(curr_paths)):

            curr_path = curr_paths[path_index]
            placed_vertices_in_path = set(curr_path).intersection(placed_vertices)


            if(len(placed_vertices_in_path) == 0):
                continue

            angle = random.randint(0,360)

            min_neigh_index = float("inf")
            max_neigh_index = float("-inf")

            for common_v in placed_vertices_in_path:
                min_neigh_index = min(min_neigh_index, curr_path.index(common_v))
                max_neigh_index = max(max_neigh_index, curr_path.index(common_v))

            min_neigh_id = curr_path[min_neigh_index]
            max_neigh_id = curr_path[max_neigh_index]

            prev_sub_path = curr_path[0:min_neigh_index]
            post_sub_path = curr_path[max_neigh_index+1:]

            min_origin_x = float(placed_pos[min_neigh_id].split(",")[0])
            min_origin_y = float(placed_pos[min_neigh_id].split(",")[1])
            lp_pos = place_path(prev_sub_path, 180+angle, desired_edge_length, min_origin_x, min_origin_y)
            nx.set_node_attributes(G, lp_pos, "pos")

            max_origin_x = float(placed_pos[max_neigh_id].split(",")[0])
            max_origin_y = float(placed_pos[max_neigh_id].split(",")[1])
            lp_pos = place_path(post_sub_path, angle, desired_edge_length, max_origin_x, max_origin_y)
            nx.set_node_attributes(G, lp_pos, "pos")

            placed = True

            curr_paths.pop(path_index)
            print("after len: " + str(len(leaves_paths[curr_length])))

            if(len(leaves_paths[curr_length]) == 0):
                leaves_paths.pop(curr_length, None)
                print("removed")


            if placed:
                leng_index = 0
                break


placed_pos = nx.get_node_attributes(G, "pos")
placed_vertices = placed_vertices.union(set(placed_pos.keys()))

non_placed_vertices =  set(G.nodes()).difference(placed_vertices)
# print(leaves_paths)
# print(non_placed_vertices)


G.remove_nodes_from(non_placed_vertices)

G = nx.Graph(G)
# print(nx.info(G))
print("saving in ", "test.dot")
write_dot(G, "test.dot")

print("end")
