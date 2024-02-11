#zigzagness
import networkx as nx
import math
import numpy as np


def angleBetweenTwoPointsWithFixedPoint(point1X, point1Y, point2X, point2Y, fixedX, fixedY):
    """Computes the angle between two lines defined by three points."""

    angle1 = math.atan2(point1Y - fixedY, point1X - fixedX)
    angle2 = math.atan2(point2Y - fixedY, point2X - fixedX)

    angle = angle1-angle2

    if(angle<0):
        angle = 2*math.pi- abs(angle)
    degree = math.degrees(angle)

    min_degree = min(degree, 360-degree)

    return min_degree


def slopeBetweenPoints(point1X, point1Y, point2X, point2Y):
     """Computes the slope between two points."""

     dy = (point2Y-point1Y)
     dx = (point2X-point1X)

     if (dx == 0):
         dx += 1* math.pow(10, -100)
         print(dx)

     return dy/dx



def compute_zigzagness(G):
    """Computes the zigzagness of the given tree."""

    total_zigzagness = 0

    # Get leaves as the vertices with degree 1
    leaves = [x for x in G.nodes() if G.degree(x)==1]

    for i in range(0, len(leaves)):

        sourceStr = leaves[i]

        for j in range(i+1, len(leaves)):

            targetStr = leaves[j]

            if(sourceStr == targetStr):
                continue

            sp = nx.shortest_path(G, sourceStr, targetStr)

            curr_zigzagness = 0

            prev_slope = 0

            for firstIndex in range(0, len(sp)-2):

                secondIndex = firstIndex + 1
                thirdIndex = secondIndex + 1

                v1Identifier = sp[firstIndex]
                v2Identifier = sp[secondIndex]
                v3Identifier = sp[thirdIndex]

                v1 = G.node[v1Identifier]
                v2 = G.node[v2Identifier]
                v3 = G.node[v3Identifier]

                v1_x = float(v1['pos'].split(",")[0])
                v1_y = float(v1['pos'].split(",")[1])

                v2_x = float(v2['pos'].split(",")[0])
                v2_y = float(v2['pos'].split(",")[1])

                v3_x = float(v3['pos'].split(",")[0])
                v3_y = float(v3['pos'].split(",")[1])

                # Angle Version
                curr_angle = 180-angleBetweenTwoPointsWithFixedPoint(v1_x, v1_y, v3_x, v3_y, v2_x, v2_y)
                curr_zigzagness += curr_angle
                print(curr_angle)

                # # Slope Version
                # curr_slope = slopeBetweenPoints(v1_x, v1_y, v2_x, v2_y)
                # print(curr_slope)
                # if(firstIndex > 0):
                #     curr_zigzagness += math.fabs(prev_slope - curr_slope)
                # prev_slope = curr_slope

            total_zigzagness += curr_zigzagness

    return total_zigzagness


def compute_zigzagness_angle(GD):

	total_zigzagness = 0

	# Get leaves as the vertices with degree 1
	leaves = [x for x in GD.nodes() if GD.degree(x)==1]
	vertices_pos = nx.get_node_attributes(GD, 'pos')

	tot_penalty = 0

	for i in range(0, len(leaves)):

		src = leaves[i]

		for j in range(i+1, len(leaves)):

			trg = leaves[j]

			if(src == trg):
				continue

			sp = nx.shortest_path(GD, src, trg)

			print(sp)

			path_angle_penality = 0
			prev_angle = 0
			prev_diff = 0

			for k in range(0, len(sp)-1):
				v1 = sp[k]
				v2 = sp[k+1]

				v1_x = float(vertices_pos[v1].split(",")[0])
				v1_y = float(vertices_pos[v1].split(",")[1])

				v2_x = float(vertices_pos[v2].split(",")[0])
				v2_y = float(vertices_pos[v2].split(",")[1])


				curr_angle = np.rad2deg(np.arctan2(v2_y - v1_y, v2_x - v1_x))

				if(k == 0):
					prev_angle = curr_angle

				angle_diff = curr_angle - prev_angle

				print("prev: "+ str(prev_angle) + " curr: " + str(curr_angle)  + "prev diff: "+ str(prev_diff) +  "curr diff: " + str(angle_diff))

				curr_penality = compute_angle_penalty_continuous(prev_angle, curr_angle, prev_diff)

				print("pen: " + str(curr_penality))

				path_angle_penality += curr_penality

				prev_angle = curr_angle
				prev_diff = angle_diff

			tot_penalty += path_angle_penality

	return tot_penalty

def compute_angle_penalty_continuous(prev_angle, curr_angle, prev_diff):

    angle_diff = curr_angle - prev_angle

    if(angle_diff == 0):

        return 0
    
    if(prev_angle == 0):
    
        print("test")

    if((np.sign(angle_diff) == np.sign(prev_diff)) or prev_diff == 0):

        angle_diff = abs(angle_diff)

        if(angle_diff >= 0 and angle_diff <=45):

            return angle_diff

        else:

            if(angle_diff >= 45 and angle_diff <=90):

                return abs(90-angle_diff)

            return abs(angle_diff-90) * 2

    else:

        angle_diff = abs(angle_diff)

        if(angle_diff >= 0 and angle_diff <=45):

            return angle_diff * 2

        else:

            if(angle_diff >= 45 and angle_diff <=90):

                return abs(90-angle_diff) * 2

            return abs(angle_diff-90) * 3



def compute_angle_penalty_discrete(prev_angle, curr_angle, prev_diff):

	angle_diff = curr_angle - prev_angle

	if(angle_diff == 0):

		return 0

	different_direction_penalty = 1

	if((np.sign(angle_diff) == np.sign(prev_diff)) or prev_diff == 0):

		different_direction_penalty = 0

	print("different direction")
	if (angle_diff >= -45 and angle_diff <= 45):

		return 1 + different_direction_penalty

	if (angle_diff >= -90 and angle_diff <= 90):

		return 2 + different_direction_penalty

	if (angle_diff >= -135 and angle_diff <= 135):

		return 4 + different_direction_penalty

	return 5 + different_direction_penalty

	print("missing value: " + str(angle_diff))

	return 0
