#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import boxoverlap

def scale(G, scaling_factor):

    all_pos = nx.get_node_attributes(G, "pos")

    for v in nx.nodes(G):

        v_x=float(all_pos[v].split(",")[0])
        v_y=float(all_pos[v].split(",")[1])

        v_x_scaled = v_x * scaling_factor
        v_y_scaled = v_y * scaling_factor
        coo = str(v_x_scaled)+","+str(v_y_scaled)

        nx.set_node_attributes(G, {v:coo}, "pos")

    return G

def getmaxoverlap(G):

    inches_to_pixel_factor = 1

    max_overlapping_width = 0
    max_overlapping_height = 0
    max_overlapping_area = 0

    max_alpha_x = 0
    max_alpha_y = 0

    widths=nx.get_node_attributes(G, "width")
    heights=nx.get_node_attributes(G, "height")
    poss=nx.get_node_attributes(G, "pos")

    nodelist = list(G.nodes())

    for v_id in range(0, len(nodelist)):

        v = nodelist[v_id]

        v_width=float(widths[v])*inches_to_pixel_factor
        v_height=float(heights[v])*inches_to_pixel_factor
        v_x=float(poss[v].split(",")[0])
        v_y=float(poss[v].split(",")[1])

        for u_id in range(v_id+1, len(nodelist)):
            u = nodelist[u_id]
            if(v == u):
                continue
            u_width=float(widths[v])*inches_to_pixel_factor
            u_height=float(heights[v])*inches_to_pixel_factor
            u_x=float(poss[u].split(",")[0])
            u_y=float(poss[u].split(",")[1])

            curr_overlap = boxoverlap.do_overlap(v_x, v_y, v_width, v_height, u_x, u_y, u_width, u_height)

            curr_alpha_x = 0
            curr_alpha_y = 0

            if(u_x-v_x > 0):
                curr_alpha_x = (abs(u_width+v_width)/abs(u_x-v_x))
            if(u_y-v_y > 0):
                curr_alpha_y = (abs(u_height+v_height)/abs(u_y-v_y))

            max_overlapping_width = max(max_overlapping_width, curr_overlap['w'])
            max_overlapping_height = max(max_overlapping_height, curr_overlap['h'])
            max_overlapping_area=max(max_overlapping_area, curr_overlap['a'])

            # compute the scaling factor only if needed
            if(max_overlapping_area>0):
                max_alpha_x=max(max_alpha_x, curr_alpha_x)
                max_alpha_y=max(max_alpha_y, curr_alpha_y)

    return {"w": max_overlapping_width, "h": max_overlapping_height, "a":max_overlapping_area, "max_alpha_x":max_alpha_x, "max_alpha_y":max_alpha_y}


# Main Flow
graph_path = sys.argv[1]
outputpath = sys.argv[2]

input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]

G = nx_read_dot(graph_path)
G=nx.Graph(G)
# G=scale(G, 1/100000000)
max_overlap=getmaxoverlap(G)
print(max_overlap)
scaling_factor=float(max(max_overlap['max_alpha_x'], max_overlap['max_alpha_y']))
print(scaling_factor)
if(scaling_factor>0):
    if(scaling_factor<1):
        scaling_factor = 1.01
    print("scaling")
    G=scale(G, scaling_factor)
    max_overlap=getmaxoverlap(G)
    print("after")
    print(max_overlap)

write_dot(G, outputpath)
