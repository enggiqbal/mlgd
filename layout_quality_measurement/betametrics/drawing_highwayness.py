import networkx as nx
import math
import re


def boundingBox(G, vertices=[]):

    all_pos = nx.get_node_attributes(G, "pos")

    if(len(vertices)>1):
        all_pos = dict((k, all_pos[k]) for k in vertices if k in all_pos)

    all_pos = all_pos.values()

    coo_x = sorted([float(p.split(",")[0]) for p in all_pos])
    coo_y = sorted([float(p.split(",")[1]) for p in all_pos])

    min_x = float(coo_x[0])
    max_x = float(coo_x[-1])

    min_y = float(coo_y[0])
    max_y = float(coo_y[-1])

    width = abs(max_x - min_x)
    height = abs(max_y - min_y)

    return (width, height)


def highwayDrawingCoverage(GD):

    all_layers = nx.get_edge_attributes(GD, "layer")
    highway_edges = {k: v for k, v in all_layers.items() if  (int(re.findall('\d+', v.split(":")[0])[0])==1)}
    highway_vertices = set()

    for curr_edge in highway_edges.keys():
        highway_vertices.add(curr_edge[0])
        highway_vertices.add(curr_edge[1])

    gd_bb = boundingBox(GD)
    hw_bb = boundingBox(GD, highway_vertices)

    gb_area = gd_bb[0] * gd_bb[1]
    hw_area = hw_bb[0] * hw_bb[1]

    return hw_area/gb_area
