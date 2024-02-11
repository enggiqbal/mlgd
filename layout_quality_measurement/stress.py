#Author
#Felice De Luca
#https://github.com/felicedeluca

import networkx as nx
import math


def euclidean_distance(source, target):

    x_source1 = float(source['pos'].split(",")[0])
    x_target1 = float(target['pos'].split(",")[0])

    y_source1 = float(source['pos'].split(",")[1])
    y_target1 = float(target['pos'].split(",")[1])

    geomDistance = math.sqrt((x_source1 - x_target1)**2 + (y_source1 - y_target1)**2)

    return geomDistance

def scale_graph(G, alpha):

    H = G.copy()

    for currVStr in nx.nodes(H):

        currV = H.node[currVStr]

        x = float(currV['pos'].split(",")[0])
        y = float(currV['pos'].split(",")[1])

        x = x * alpha
        y = y * alpha

        currV['pos'] = str(x)+","+str(y)

    return H

def computeScalingFactor(S, all_sp):
    num = 0
    den = 0

    nodes = list(nx.nodes(S))

    for i in range(0, len(nodes)):

        sourceStr = nodes[i]
        source = S.node[sourceStr]

        for j in range(i+1, len(nodes)):

            targetStr = nodes[j]

            if(sourceStr == targetStr):
                continue

            target = S.nodes[targetStr]

            graph_theoretic_distance = 0

            graph_theoretic_distance = len(all_sp[sourceStr][targetStr])-1

            geomDistance = euclidean_distance(source, target)

            if (graph_theoretic_distance <= 0):
                continue

            weight = 1/(graph_theoretic_distance**2)

            num = num + (graph_theoretic_distance * geomDistance * weight)
            den = den + (weight * (geomDistance**2))

    scale = num/den

    return scale


def stress(S, G=None, weighted=True, all_sp=None):
    '''Computes the strees of the layout <tt>S</tt> if the parameter <tt>G</tt>
    is passed it computes the stress of the layout <tt>S</tt>
    with respect the graph distances on <tt>G</tt>'''


    S_original = S.copy()

    alpha = 1

    if all_sp is None:
        if(G is None):
            if(weighted):
                # converting weights in float
                all_weights_n = nx.get_node_attributes(S, "weight")
                for nk in all_weights_n.keys():
                    all_weights_n[nk] = float(all_weights_n[nk])
                nx.set_node_attributes(S, all_weights_n, "weight")

                all_weights_e = nx.get_edge_attributes(S, "weight")
                for ek in all_weights_e.keys():
                    all_weights_e[ek] = float(all_weights_e[ek])
                nx.set_edge_attributes(S, all_weights_e, "weight")
                all_sp = nx.shortest_path(S, weight="weight")
            else:
                all_sp = nx.shortest_path(S)
        else:
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

    alpha = computeScalingFactor(S_original, all_sp)

    S = scale_graph(S_original, alpha)

    vertices = list(nx.nodes(S))

    stress = 0

    for i in range(0, len(vertices)):

        sourceStr = vertices[i]
        source = S.node[sourceStr]

        for j in range(i+1, len(vertices)):

            targetStr =  vertices[j]
            target = S.nodes[targetStr]

            graph_theoretic_distance = len(all_sp[sourceStr][targetStr])-1
            eu_dist = euclidean_distance(source, target)

            if (graph_theoretic_distance <= 0):
                continue

            delta_squared = (eu_dist - graph_theoretic_distance)**2
            weight = 1/(graph_theoretic_distance**2)
            stress = stress +  (weight * delta_squared)

    scale_graph(S, 1/alpha)


    stress = round(stress, 3)

    return stress
