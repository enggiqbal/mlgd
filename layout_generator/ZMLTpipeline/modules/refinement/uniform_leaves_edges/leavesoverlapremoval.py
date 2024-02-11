import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

# import edge_crossing as crossings

import boxoverlap

def remove_leaves_overlap(G):

    # print("computing overlap")
    overlapping_vertices = get_overlapping_vertices(G)

    # print("computing boxes")
    all_boxes, all_boxes_dict = compute_boxes(G)

    leaves = [x for x in G.nodes() if G.degree(x)==1]

    poss=nx.get_node_attributes(G, "pos")

    for bb in overlapping_vertices:

        # bb=overlapping_vertices.pop()

        u = bb["u"]
        v = bb["v"]

        if (u not in leaves) and (v not in leaves):
            continue

        width_overlap=bb["w"]
        height_overlap=bb["h"]

        delta_l_u = float('inf')
        delta_l_v = float('inf')

        desired_length_u = float('inf')
        desired_length_v = float('inf')


        if u in leaves:
            leaf_id = u
            origin_id = list(nx.all_neighbors(G, leaf_id))[0]
            delta_l_u = get_delta_to_remove_ovelap(G, origin_id, leaf_id, all_boxes_dict[leaf_id], width_overlap, height_overlap)
            desired_length_u = compute_desired_length(G, origin_id, leaf_id, delta_l_u)

        if v in leaves:
            leaf_id = v
            origin_id = list(nx.all_neighbors(G, leaf_id))[0]
            delta_l_v = get_delta_to_remove_ovelap(G, origin_id, leaf_id, all_boxes_dict[leaf_id], width_overlap, height_overlap)
            desired_length_v = compute_desired_length(G, origin_id, leaf_id, delta_l_v)

        if desired_length_u <= 0:
            print("delta longer than edge")
            delta_l_u = float('inf')

        if desired_length_v <= 0:
            print("delta longer than edge")
            delta_l_v = float('inf')

        if delta_l_v == float('inf') and  delta_l_u  == float('inf'):
            print("impossible to shorten")
            continue

        leaf_id = u
        desired_length = desired_length_u
        # print(u, v)
        # print(delta_l_u, delta_l_v)

        if delta_l_v < delta_l_u:
            leaf_id = v
            desired_length = desired_length_v

        origin_id = list(nx.all_neighbors(G, leaf_id))[0]
        shorten_leaf(leaf_id, origin_id,  G, value=desired_length)

        # print("removed", str(u), str(v), "shorten", str(leaf_id))
        # overlapping_vertices = get_overlapping_vertices(G)

def compute_hit_side(G, origin, v, v_box):

    poss=nx.get_node_attributes(G, "pos")

    x_origin=float(poss[origin].split(",")[0])
    y_origin=float(poss[origin].split(",")[1])
    x_v=float(poss[v].split(",")[0])
    y_v=float(poss[v].split(",")[1])

    # {'min_x':min_x, 'max_x':max_x, 'min_y':min_y, 'max_y':max_y, 'node':v}

    hit = '0'

    # print(v_box)

    if crossings.doSegmentsIntersect(float(x_origin), float(y_origin), float(x_v), float(y_v), float(v_box['min_x']), float(v_box['min_y']), float(v_box['min_x']), float(v_box['max_y'])):
        hit = "l"
    if crossings.doSegmentsIntersect(float(x_origin), float(y_origin), float(x_v), float(y_v), float(v_box['max_x']), float(v_box['min_y']), float(v_box['max_x']), float(v_box['max_y'])):
        hit = "r"
    if crossings.doSegmentsIntersect(float(x_origin), float(y_origin), float(x_v), float(y_v), float(v_box['min_x']), float(v_box['min_y']), float(v_box['max_x']), float(v_box['min_y'])):
        hit = "b"
    if crossings.doSegmentsIntersect(float(x_origin), float(y_origin), float(x_v), float(y_v), float(v_box['min_x']), float(v_box['max_y']), float(v_box['max_x']), float(v_box['max_y'])):
        hit = "t"
    return hit




def compute_desired_length(G, origin_id, leaf_id, delta):

    poss=nx.get_node_attributes(G, "pos")

    x_origin=float(poss[origin_id].split(",")[0])
    y_origin=float(poss[origin_id].split(",")[1])
    x_leaf=float(poss[leaf_id].split(",")[0])
    y_leaf=float(poss[leaf_id].split(",")[1])

    length = math.sqrt((x_origin - x_leaf)**2 + (y_origin - y_leaf)**2)
    desired_length = length - delta

    # print("len des delta", length, desired_length, delta, 'id', leaf_id)


    return desired_length




def get_delta_to_remove_ovelap(G, origin_id, leaf_id, leaf_box, width_overlap, height_overlap):

    poss=nx.get_node_attributes(G, "pos")

    x_origin=float(poss[origin_id].split(",")[0])
    y_origin=float(poss[origin_id].split(",")[1])
    x_leaf=float(poss[leaf_id].split(",")[0])
    y_leaf=float(poss[leaf_id].split(",")[1])

    theta = getAngleOfLineBetweenTwoPoints(x_origin, y_origin, x_leaf, y_leaf)

    hit_side = compute_hit_side(G, origin_id, leaf_id, leaf_box)


    x_shift = height_overlap/math.cos(theta)
    y_shift = width_overlap/math.sin(theta)

    if hit_side == 0:
        hitsize=0
        return float('inf')

    if hit_side == 'l' or hit_side == 'r':
        return x_shift

    if hit_side == 'b' or hit_side == 't':
        return y_shift

    # print("no side found")
    return float('inf')

    # delta = min(abs(x_shift), abs(y_shift))
    #
    # return delta






def shorten_leaf(leaf_id, origin_id, G, value=0):

    poss=nx.get_node_attributes(G, "pos")

    x_origin=float(poss[origin_id].split(",")[0])
    y_origin=float(poss[origin_id].split(",")[1])

    x_leaf=float(poss[leaf_id].split(",")[0])
    y_leaf=float(poss[leaf_id].split(",")[1])

    x_num = value * (x_leaf - x_origin)
    y_num = value * (y_leaf - y_origin)

    x_den = math.sqrt((x_origin-x_leaf)**2 + (y_origin-y_leaf)**2)
    y_den = math.sqrt((x_origin-x_leaf)**2+(y_origin-y_leaf)**2)

    x_leaf_new = x_origin + x_num/x_den
    y_leaf_new = y_origin + y_num/y_den


    G.node[leaf_id]['pos'] = str(x_leaf_new)+","+str(y_leaf_new)






def get_overlapping_vertices(G, with_vertices=None):

    inches_to_pixel_factor = 72

    leaves = [x for x in G.nodes() if G.degree[x]>0]
    widths=nx.get_node_attributes(G, "width")
    heights=nx.get_node_attributes(G, "height")


    overlapping_vertices=[]

    found = set()

    if with_vertices is None:
        with_vertices = G.nodes()


    for v in leaves:

        poss=nx.get_node_attributes(G, "pos")

        v_width=float(widths[v])*inches_to_pixel_factor
        v_height=float(heights[v])*inches_to_pixel_factor
        v_x=float(poss[v].split(",")[0])
        v_y=float(poss[v].split(",")[1])

        for u in with_vertices:

            if(v == u):
                continue

            if (u not in leaves) and (v not in leaves):
                continue

            u_width=float(widths[v])*inches_to_pixel_factor
            u_height=float(heights[v])*inches_to_pixel_factor
            u_x=float(poss[u].split(",")[0])
            u_y=float(poss[u].split(",")[1])

            curr_overlap = boxoverlap.do_overlap(v_x, v_y, v_width, v_height, u_x, u_y, u_width, u_height)

            if curr_overlap['a'] == 0:
                continue

            if (u,v) in found or (v, u) in found:
                continue

            overlapping_vertices.append({'v': v, 'u':u, 'w':v_width+u_width, 'h':v_height+u_height})
            # overlapping_vertices.append({'v': v, 'u':u, 'w':curr_overlap['w'] , 'h':curr_overlap['h']})
            found.add((u,v))


    if len(overlapping_vertices)>1:
        print("overlapping: " + str(len(overlapping_vertices)))

    return overlapping_vertices


def getAngleOfLineBetweenTwoPoints(point1X, point1Y, point2X, point2Y):
        xDiff = point2X - point1X
        yDiff = point2Y - point1Y
        angle = math.degrees(math.atan2(yDiff, xDiff))
        return angle #math.radians(angle)


def compute_boxes(G):

    all_boxes = []

    widths=nx.get_node_attributes(G, "width")
    heights=nx.get_node_attributes(G, "height")
    poss=nx.get_node_attributes(G, "pos")

    all_boxes_dict = {}

    for v in G.nodes():

        curr_box = {'min_x':0, 'max_x':0, 'min_y':0, 'max_y':0}

        v_x=float(poss[v].split(",")[0])
        v_y=float(poss[v].split(",")[1])
        v_width = float(widths[v])
        v_height = float(heights[v])

        min_x = v_x-(v_width/2)
        max_x = v_x+(v_width/2)

        min_y = v_y-(v_height/2)
        max_y = v_y+(v_height/2)
        all_boxes_dict[v] = {'min_x':min_x, 'max_x':max_x, 'min_y':min_y, 'max_y':max_y, 'node':v}
        all_boxes.append({'min_x':min_x, 'max_x':max_x, 'min_y':min_y, 'max_y':max_y, 'node':v})

    return all_boxes, all_boxes_dict
