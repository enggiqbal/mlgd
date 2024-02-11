import networkx as nx
import math
import re

alpha = 0.5 #Speed on Highway
beta = 1 #Speed on non Highway

def highwayness(GD):
	
	
	# Set the speed attribute on each edge based on the edge type
	add_speed_attribute(GD)

	# computes the average edge length and average shortest path length
	edge_lengths = sorted([float(p) for p in nx.get_edge_attributes(GD, "length").values()])
	avg_length = sum(edge_lengths) / float(len(edge_lengths))
	avg_sp = nx.average_shortest_path_length(GD)
	
	# compute the average route length
	min_travel_time_route = avg_length*avg_sp
		
	tot_network_length = 0
	tot_travel_time = 0
	
	
	  # Get leaves as the vertices with degree 1
	leaves = [x for x in GD.nodes() if GD.degree(x)==32]
	

	for i in range(0, len(leaves)):

		sourceStr = leaves[i]

		for j in range(i+1, len(leaves)):
		
			targetStr = leaves[j]
			
			if(sourceStr == targetStr):
				continue
			
			sp = nx.shortest_path(GD, sourceStr, targetStr, weight="travel_time")
			length = compute_path_length(GD,sp)
			travel_time = compute_path_travel_time(GD, sp)
			
			if(min_travel_time_route > travel_time):
				continue
			
			tot_network_length += length
			tot_travel_time += travel_time
	
	highwayness = tot_travel_time/tot_network_length
	
	return highwayness
	
	
def highwayness_multilevel_ratio(GD_prev, GD_curr):
		
	GD_curr_with_old_positions = GD_curr.copy()	

	vertices_old_pos = nx.get_node_attributes(GD_prev, 'pos')

	nx.set_node_attributes(GD_curr_with_old_positions, vertices_old_pos, 'pos')

	highwayness_curr = highwayness(GD_curr)
	highwayness_curr_old_pos = highwayness(GD_curr_with_old_positions)
	
	return highwayness_curr/highwayness_curr_old_pos	
	
			
def compute_path_travel_time(GD, path):
	
	travel_time = 0
	
	for i in range(0, len(path)-1):
		src = path[i]
		trg = path[i+1]
		travel_time += float(GD[src][trg]["travel_time"])
	
	return travel_time
			
		
def compute_path_length(GD, path):
		
	length = 0
	
	for i in range(0, len(path)-1):
		src = path[i]
		trg = path[i+1]
		length += float(GD[src][trg]["length"])
	
	return length
		
	
def add_speed_attribute(GD):
	
	vertices_pos = nx.get_node_attributes(GD, 'pos')
	edge_layer_attributes = nx.get_edge_attributes(GD, "layer")
	
	for curr_edge in nx.edges(GD):
		
		u = curr_edge[0]
		v = curr_edge[1]
		
		u_pos = vertices_pos[u]
		v_pos = vertices_pos[v]
		
		u_x = float(u_pos.split(",")[0])
		v_x = float(v_pos.split(",")[0])
		
		u_y = float(u_pos.split(",")[1])
		v_y = float(v_pos.split(",")[1])
		
		distance = math.sqrt( (u_x - v_x)**2 + (u_y - v_y)**2)
		
		curr_edge_layer_attr = edge_layer_attributes[curr_edge]
		
		speed = beta
		
		if  (int(re.findall('\d+', curr_edge_layer_attr.split(":")[0])[0])==1):
			speed = alpha
		
		travel_time = distance * speed
		
		GD[u][v]['travel_time'] = travel_time
		GD[u][v]['length'] = distance


		
	

'''
    Check whether the given edge is an highway edge
    TODO: Define how to extract highway info from edge
'''
def is_highway_edge(edge):
    
    edge_layer_attr = edge["layer"].split(":")[0]
    edge_layer = int(re.findall('\d+', edge_layer_attr)[0])
    
    return (edge_layer == 1)
