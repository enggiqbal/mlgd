#continuity property
#Evaluates the quality of an highway
import networkx as nx
import math
import re

def continuity(G):

    avg_sp = nx.average_shortest_path_length(G)
    total_sp = len(nx.nodes(G))*(len(nx.nodes(G))-1)
    long_sp = total_sp
    global_max_jumps = 0
    global_min_jumps = float('inf')

    zero_jumps_sp = 0
    single_jump_sp = 0
    many_jumps_sp = 0

    # source identifier
    for source in nx.nodes(G):

        # target identifier
        for target in nx.nodes(G):

            if(source == target):
                continue

            # Compute the length of the sp
            sp_len = nx.shortest_path_length(G, source=str(source), target=str(target))

            # ignore the sp shorter than avg
            if sp_len < avg_sp or sp_len <= 1:
                long_sp -= 1
                continue

            min_jumps = float('inf')

            # all shortest paths between source and target
            all_shortest_paths =  [p for p in nx.all_shortest_paths(G, source=str(source), target=str(target))]

            for curr_sp_arr_index in range(len(all_shortest_paths)):

                # for each shortest path if the length is longer than avg
                # then compute the jumps and return the minimum value
                curr_sp = all_shortest_paths[curr_sp_arr_index]

                jumps = 0

                prev_highway_edge = False

                for curr_s_index in range(len(curr_sp)-1):
                    curr_t_index = curr_s_index + 1

                    s = curr_sp[curr_s_index]
                    t = curr_sp[curr_t_index]

                    edge = G[s][t]

                    highway_edge = is_highway_edge(edge)

                    # Count when the shortest path jumps on level 1 edges
                    if(highway_edge and prev_highway_edge != highway_edge):
                            jumps += 1

                    prev_highway_edge = highway_edge

                if(jumps == 1):
                    min_jumps = 1
                else:
                    min_jumps = min(min_jumps, jumps)

            # Store the computed value of Continuiti
            global_max_jumps = max(global_max_jumps, min_jumps)
            global_min_jumps = min(global_min_jumps, min_jumps)

            if(min_jumps == 0):
                zero_jumps_sp += 1
            if(min_jumps == 1):
                single_jump_sp += 1
            if(min_jumps > 1):
                many_jumps_sp += 1


    print("avg sp: " + str(avg_sp))
    print("# total shortest paths: " + str(total_sp))
    print("# long shortest paths: " + str(long_sp))
    print("global max jumps: " + str(global_max_jumps))
    print("global min jumps: " + str(global_min_jumps))
    print("zero jumps: " + str(zero_jumps_sp))
    print("one jump: " + str(single_jump_sp))
    print("many jumps: " + str(many_jumps_sp))
    print("zero jumps (%): " + str(zero_jumps_sp*100/long_sp))
    print("one jump (%): " + str(single_jump_sp*100/long_sp))
    print("many jumps (%): " + str(many_jumps_sp*100/long_sp))

'''
Check whether the given edge is an highway edge
TODO: Define how to extract highway info from edge
'''
def is_highway_edge(edge):

    #
    edge_layer_attr = edge["layer"].split(":")[0]
    edge_layer = int(re.findall('\d+', edge_layer_attr)[0])

    return (edge_layer == 1)
