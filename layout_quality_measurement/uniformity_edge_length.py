#Author
#Felice De Luca
#https://github.com/felicedeluca

##
# EU corresponds to the normalized standard deviation of the edge length.
##

import networkx as nx
import math


def avg_edge_length(G):

    '''
    Computes the average edge length of the given graph layout <tt>G</tt>
    '''

    sum_edge_length = 0.0

    pos_dict = nx.get_node_attributes(G, 'pos')

    for edge in nx.edges(G):

        (s,t) = edge

        x_source = float(pos_dict[s].split(",")[0])
        x_target = float(pos_dict[t].split(",")[0])

        y_source = float(pos_dict[s].split(",")[1])
        y_target = float(pos_dict[t].split(",")[1])

        curr_length = math.sqrt((x_source - x_target)**2 + (y_source - y_target)**2)

        sum_edge_length += curr_length

    edges_count = len(nx.edges(G))
    avg_edge_len = sum_edge_length/edges_count

    return avg_edge_len



def uniformity_edge_length(G):
    '''
    The Edge length uniformity corresponds to the normalized standard deviation of the edge length.
    '''

    edges = nx.edges(G)
    edge_count = len(edges)
    avgEdgeLength = avg_edge_length(G)
    tot_sum = 0.0

    pos_dict = nx.get_node_attributes(G, 'pos')

    for edge in edges:

        (s,t) = edge

        x_source = float(pos_dict[s].split(",")[0])
        x_target = float(pos_dict[t].split(",")[0])

        y_source = float(pos_dict[s].split(",")[1])
        y_target = float(pos_dict[t].split(",")[1])

        curr_length = math.sqrt((x_source - x_target)**2 + (y_source - y_target)**2)

        num = (curr_length-avgEdgeLength)**2
        den = edge_count*(avgEdgeLength**2)

        currValue = num/den
        tot_sum += currValue

    uniformity_e_len = math.sqrt(tot_sum)

    result = round(uniformity_e_len, 3)

    return result
