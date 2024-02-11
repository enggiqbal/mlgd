import sys
import os

import pygraphviz as pgv
import networkx as nx
import math
import random

def getCoordinate(vertex):
    x = float(vertex['pos'].split(",")[0])
    y = float(vertex['pos'].split(",")[1])
    return x, y


def setCoordinate(vertex, x, y):
    vertex['pos'] = str(x)+","+str(y)
    return x, y


def shiftVertex(vertex, dx, dy):
    x, y = getCoordinate(vertex)
    setCoordinate(vertex, x+dx, y+dy)
    return getCoordinate(vertex)



def translateGraph(G, translation_dx, translation_dy):
    for currVertex in nx.nodes(G):
        shiftVertex(G.node[currVertex], translation_dx, translation_dy)
    return G



def getAngleOfLineBetweenTwoPoints(point1X, point1Y, point2X, point2Y):
        xDiff = point2X - point1X
        yDiff = point2Y - point1Y
        return math.degrees(math.atan2(yDiff, xDiff))

def computeSectorsAngles(G, centralVertex):

    angle_dict = dict()

    for currN in set(nx.all_neighbors(G, centralVertex)):

        v1 = G.node[centralVertex]
        v2 = G.node[currN]

        v1_x, v1_y = getCoordinate(v1)
        v2_x, v2_y = getCoordinate(v2)

        angle = getAngleOfLineBetweenTwoPoints(v1_x, v1_y, v2_x, v2_y)

        if(angle < 0):
            angle = 360-abs(angle)


        if(angle not in angle_dict.keys()):
            angle_dict[angle] = list()
        angle_dict[angle].append(currN)

    sorted_slopes = sorted(angle_dict.keys())

    sector_angle_dict = dict()
    sector_angles_list = list()

    for i in range(0, len(sorted_slopes)):

        first_index = i
        second_index = i+1

        if(second_index>=len(sorted_slopes)):
            second_index = 0

        first_slope = sorted_slopes[first_index]
        next_slope = sorted_slopes[second_index]


        v1_id = angle_dict[first_slope][0]
        v2_id = angle_dict[next_slope][0]

        center = G.node[centralVertex]
        v1 = G.node[v1_id]
        v2 = G.node[v2_id]

        angular_resolution = next_slope-first_slope

        if(angular_resolution < 0):
            angular_resolution = 360-abs(angular_resolution)

        if(angular_resolution not in sector_angle_dict.keys()):
            sector_angle_dict[angular_resolution] = list()


        sector_angle_dict[angular_resolution].append([v1_id,  v2_id])
        sector_angles_list.append(angular_resolution)

    return sector_angle_dict, sector_angles_list

def compute(G):

    vertices = list(G.nodes())

    angle_sum = 0.0
    angle_count = 0

    for currVertex in vertices:

        adjsIterator = nx.all_neighbors(G, currVertex)

        adjs = list(adjsIterator)

        if(len(adjs)<=1):
            continue

        # Translate Drawing for convenience
        translation_dx, translation_dy = getCoordinate(G.node[currVertex])
        translateGraph(G, -translation_dx, -translation_dy)

        # # Extract subcomponents, draw, sacle and attach it to the widest sector
        for currN in adjs:

            # Compute sector angle and get the largest
            sector_angle_dict, sector_angles_list = computeSectorsAngles(G, currVertex)
            sorted_angles = sorted(sector_angles_list)
            angle_sum += sorted_angles[0]
            angle_count += 1

        #Place back the graph at original position
        translateGraph(G, translation_dx, translation_dy)

    angle_avg = angle_sum / angle_count

    return angle_avg
