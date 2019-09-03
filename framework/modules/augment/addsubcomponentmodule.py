#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import monotone_drawing
import math
import random

import edge_crossing as crossings
import vertexmanager
import tree_crossings_remover


def translateGraph(G, translation_dx, translation_dy):

    for currVertex in nx.nodes(G):
        vertexmanager.shiftVertex(G.node[currVertex], translation_dx, translation_dy)

    return G

def extract_subcomponent(G, mainVertex, vertex):

    tempG = G.copy()
    tempG.remove_node(mainVertex)
    subcomponet_vertices = nx.node_connected_component(tempG, vertex)
    subcomponet_vertices.add(mainVertex)
    subcomponet_edges = G.subgraph(subcomponet_vertices).copy().edges()
    H = nx.Graph()
    H.add_nodes_from(list(subcomponet_vertices))
    H.add_edges_from(list(subcomponet_edges))
    return H

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

    return width, height

def scale(G, scaling_factor):

    for currVertex in nx.nodes(G):
        v = G.node[currVertex]
        x = float(v['pos'].split(",")[0])
        y = float(v['pos'].split(",")[1])
        v_x_scaled = x * scaling_factor
        v_y_scaled = y * scaling_factor
        v['pos'] = str(v_x_scaled)+","+str(v_y_scaled)

    return G


def rotate(G, angle):

    angle = math.radians(angle)

    for currVertex in nx.nodes(G):
        x, y = vertexmanager.getCoordinate(G.node[currVertex])
        x_rot = x*math.cos(angle) - y*math.sin(angle)
        y_rot = x*math.sin(angle) + y*math.cos(angle)
        vertexmanager.setCoordinate(G.node[currVertex], x_rot, y_rot)

    return G


def getAngleOfLineBetweenTwoPoints(point1X, point1Y, point2X, point2Y):
        xDiff = point2X - point1X
        yDiff = point2Y - point1Y
        return math.degrees(math.atan2(yDiff, xDiff))

def computeSectorsAngles(G, commonVertex):

    angle_dict = dict()

    for currN in set(nx.all_neighbors(G, commonVertex)):

        v1 = G.node[commonVertex]
        v2 = G.node[currN]

        v1_x, v1_y = vertexmanager.getCoordinate(v1)
        v2_x, v2_y = vertexmanager.getCoordinate(v2)

        angle = getAngleOfLineBetweenTwoPoints(v1_x, v1_y, v2_x, v2_y)



        if(angle < 0):
            angle = 360-abs(angle)


        if(angle not in angle_dict.keys()):
            angle_dict[angle] = list()
        angle_dict[angle].append(currN)

    sorted_slopes = sorted(angle_dict.keys())

    sector_angle_dict = dict()

    for i in range(0, len(sorted_slopes)):

        first_index = i
        second_index = i+1

        if(second_index>=len(sorted_slopes)):
            second_index = 0

        first_slope = sorted_slopes[first_index]
        next_slope = sorted_slopes[second_index]


        v1_id = angle_dict[first_slope][0]
        v2_id = angle_dict[next_slope][0]

        center = G.node[commonVertex]
        v1 = G.node[v1_id]
        v2 = G.node[v2_id]

        angular_resolution = next_slope-first_slope

        if(angular_resolution < 0):
            angular_resolution = 360-abs(angular_resolution)

        if(angular_resolution not in sector_angle_dict.keys()):
            sector_angle_dict[angular_resolution] = list()


        sector_angle_dict[angular_resolution].append([v1_id,  v2_id])

    return sector_angle_dict


def avg_edge_length(G):

    sum_edge_length = 0.0
    edge_count = len(G.edges())

    for edge in G.edges():

        s,t = edge

        s = G.node[s]
        t = G.node[t]

        x_source1, y_source1  = vertexmanager.getCoordinate(s)
        x_target1, y_target1 = vertexmanager.getCoordinate(t)

        curr_length = math.sqrt((x_source1 - x_target1)**2 + (y_source1 - y_target1)**2)

        sum_edge_length += curr_length

    avg_edge_len = sum_edge_length/edge_count
    return avg_edge_len

# Main Flow

graphpath = sys.argv[1]
subgraphpath = sys.argv[2]
outputpath = sys.argv[3]

print("add subcomponent: input ", graphpath, subgraphpath, outputpath)

# Main Graph
input_graph_name = os.path.basename(graphpath)
graph_name = input_graph_name.split(".")[0]


print(graph_name)

# Sub Graph to be added
input_subgraph_name = os.path.basename(subgraphpath)
subgraph_name = subgraphpath.split(".")[0]

# Reading graph and subgraph
G = nx_read_dot(graphpath)
nx.set_edge_attributes(G, 'red', 'color')
SubG = nx_read_dot(subgraphpath)


commonVertices = set(set(G.nodes()) & set(SubG.nodes()))


avg_edge_length = avg_edge_length(G)

if len(crossings.count_crossings_single_graph(G)):
    print(graph_name + "  has crossings.")
    print("exiting")
    sys.exit()


v_counter=0
for commonVertex in commonVertices:
    v_counter+=1

    translation_dx, translation_dy = vertexmanager.getCoordinate(G.node[commonVertex])
    translateGraph(G, -translation_dx, -translation_dy)

    a_counter=0
    # # Extract subcomponents, draw, sacle and attach it to the widest sector
    for currN in set(nx.all_neighbors(SubG, commonVertex)):
        a_counter+=1

        # Compute sector angle and get the largest
        sector_angle_dict = computeSectorsAngles(G, commonVertex)
        sorted_angles = sorted(sector_angle_dict.keys())
        largest_sector_angle = sorted_angles[-1]

        # Get First vertex
        sector_vertices = sector_angle_dict[largest_sector_angle][0]

        if(largest_sector_angle == 0):
            largest_sector_angle = 360

        center_v_id = commonVertex
        first_v_id = sector_vertices[0]

        center_v = G.node[commonVertex]
        first_v = G.node[first_v_id]

        center_v_x, center_v_y = vertexmanager.getCoordinate(center_v)
        first_v_x, first_v_y = vertexmanager.getCoordinate(first_v)

        min_sector_angle = getAngleOfLineBetweenTwoPoints(center_v_x, center_v_y, first_v_x, first_v_y)

        if(min_sector_angle < 0):
            min_sector_angle = 360-abs(min_sector_angle)

        drawing_rotation_factor = -min_sector_angle

        # Rotate
        rotate(G, drawing_rotation_factor)
        # Compute subcomponent Drawing
        H = extract_subcomponent(SubG, commonVertex, currN)
        H = monotone_drawing.monotone_draw(H, commonVertex, avg_edge_length)

        mid_sector_angle = largest_sector_angle/2

        # Place first vertex of new component on bisector
        currN_v = H.node[currN]
        currN_v_x, currN_v_y = vertexmanager.getCoordinate(currN_v)
        first_H_vertex_angle = getAngleOfLineBetweenTwoPoints(center_v_x, center_v_y, currN_v_x, currN_v_y)

        if(first_H_vertex_angle < 0):
            first_H_vertex_angle = 360-abs(first_H_vertex_angle)

        desired_first_H_angle = mid_sector_angle - first_H_vertex_angle
        rotate(H, desired_first_H_angle)

        scaling_factor = 0.5
        # # # Add subcomponent
        G.add_nodes_from(H.copy())
        nx.set_node_attributes(G, nx.get_node_attributes(H, 'pos'), 'pos')
        G.add_edges_from(H.edges)
        G.add_edge(currN, commonVertex)

#        Count crossings between H and G
        edges_to_compare = list(H.edges)
        edges_to_compare.append((currN, commonVertex))
        crossing_pair=crossings.count_crossings(G, edges_to_compare)

        while len(crossing_pair):
            H = scale(H, scaling_factor)
            H_pos = nx.get_node_attributes(H, 'pos')
            # print(nx.nodes(H))
            nx.set_node_attributes(G, H_pos, 'pos')
            edges_to_compare = list(H.edges)
            edges_to_compare.append((currN, commonVertex))
            crossing_pair=crossings.count_crossings(G, edges_to_compare)

        rotate(G, -drawing_rotation_factor)

    #Place back the graph at original position
    translateGraph(G, translation_dx, translation_dy)




G = nx.Graph(G)
#G = tree_crossings_remover.remove_crossings(G)
#G = nx.Graph(G)

nx.set_node_attributes(G, nx.get_node_attributes(SubG, 'level'), 'level')
nx.set_node_attributes(G, nx.get_node_attributes(SubG, 'label'), 'label')

# lbl_scaling_factor = 72
#
# scaled_w = nx.get_node_attributes(SubG, 'width')
# for k in scaled_w.keys():
#     scaled_w[k] = float(scaled_w[k])/lbl_scaling_factor
#
# scaled_h = nx.get_node_attributes(SubG, 'height')
# for k in scaled_w.keys():
#     scaled_h[k] = float(scaled_h[k])/lbl_scaling_factor
#
#nx.set_node_attributes(G, scaled_w, 'width')
#nx.set_node_attributes(G, scaled_h, 'height')

write_dot(G, outputpath)
