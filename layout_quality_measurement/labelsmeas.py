#Author
#Felice De Luca
#https://github.com/felicedeluca

import networkx as nx

import math
import decimal


global_labels_scaling_factor = 1

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

    return (width, height)

def do_rectangles_overlap(x1, y1, w1, h1, x2, y2, w2, h2):
    '''Checks if two rectangles overlap and gives the value of overlapping area.
    Reactangles are centered in x1, y1, and x2, y2 and have as height and width
    h1 , w1 and h2,w2 respectively.
    '''

    x_min_1=x1-(w1/2)
    y_min_1=y1-(h1/2)
    x_max_1=x1+(w1/2)
    y_max_1=y1+(h1/2)

    x_min_2=x2-(w2/2)
    y_min_2=y2-(h2/2)
    x_max_2=x2+(w2/2)
    y_max_2=y2+(h2/2)


    if(x_max_1 <= x_min_2 or x_max_2 <= x_min_1 or
       y_max_1 <= y_min_2 or y_max_2 <= y_min_1):
        # print("No Overlap")
        overlap = {'w': 0, 'h': 0, 'a':0, 'u':-1, 'v':-1}

        return overlap

    l1=(x_min_1, y_min_1)
    l2=(x_min_2, y_min_2)

    r1=(x_max_1, y_max_1)
    r2=(x_max_2, y_max_2)

    area_1=w1*h1
    area_2=w2*h2

    width_overlap = (min(x_max_1, x_max_2)-max(x_min_1, x_min_2))
    height_overlap = (min(y_max_1, y_max_2)-max(y_min_1, y_min_2))

    areaI = width_overlap*height_overlap

    total_area = area_1 + area_2 - areaI

    overlap = {"w": width_overlap, "h": height_overlap, "a":areaI}

    return overlap

def totLabelsOverlappingArea(G, labelsscalingfactor=global_labels_scaling_factor):
    '''Computes the overlapping area of the labels of the graph G.
    It requires width and height for each vertex. The labels are supposed to be
    rectangular. Before computing the value, each label is scaled by
    <tt>labelsscalingfactor</tt> in order to reflect the printed size of the label.
    The default value of <tt>labelsscalingfactor</tt> is <tt>72</tt>'''

    overlapping_area = 0
    widths=nx.get_node_attributes(G, "width")
    heights=nx.get_node_attributes(G, "height")
    all_pos = nx.get_node_attributes(G, "pos")

    nodes = list(nx.nodes(G))

    for u_index in range(0, len(nodes)):

        u = nodes[u_index]
        u_width = float(widths[u])*labelsscalingfactor
        u_height = float(heights[u])*labelsscalingfactor
        u_x = float(all_pos[u].split(",")[0])
        u_y = float(all_pos[u].split(",")[1])

        for v_index in range(u_index+1, len(nodes)):

            v = nodes[v_index]
            v_width = float(widths[v])*labelsscalingfactor
            v_height = float(heights[v])*labelsscalingfactor
            v_x = float(all_pos[v].split(",")[0])
            v_y = float(all_pos[v].split(",")[1])

            curr_overlap = do_rectangles_overlap(u_x, u_y, u_width, u_height, v_x, v_y, v_width, v_height)

            overlapping_area += curr_overlap['a']

    return overlapping_area


def totLabelsArea(G, labelsscalingfactor=global_labels_scaling_factor):
    '''Computes the minimum area covered by non overlapping labels
    It requires width and height for each vertex. The labels are supposed to be
    rectangular. Before computing the value, each label is scaled by
    <tt>labelsscalingfactor</tt> in order to reflect the printed size of the label.
    The default value of <tt>labelsscalingfactor</tt> is <tt>72</tt>'''

    widths=nx.get_node_attributes(G, "width")
    heights=nx.get_node_attributes(G, "height")

    tot_area = 0

    for v in widths.keys():

        curr_area = (float(widths[v])*labelsscalingfactor)*(float(heights[v])*labelsscalingfactor)
        tot_area+=curr_area

    return tot_area



def labelsBBRatio(G):
    '''
    Computes the ratio between the minimum area occupied by non overlapping labels and
    the actual area of the drawings.

    Return ratio in scientific notation
    '''

    bb = boundingBox(G)
    bb_area = bb[0]*bb[1]

    l_area = totLabelsArea(G)

    aspectRatio = bb_area/l_area


    aspectRatio = '%.2E' % decimal.Decimal(aspectRatio)

    return aspectRatio


def diameter(G):

    return nx.diameter(G)
