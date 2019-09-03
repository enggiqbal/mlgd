#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import math

import stress
import neighbors_preservation as neigpres
import crossings
import uniformity_edge_length as uniedgelen
import other_measures as othermeas
import labelsmeas

import upwardflow


graphpath = sys.argv[1]
outputTxtFile = sys.argv[2]


input_file_name = os.path.basename(graphpath)
graph_name = input_file_name.split(".")[0]


G = nx_read_dot(graphpath)
G = nx.Graph(G)

cmds=[]
all = False
if len(sys.argv) > 3 :
    cmds = sys.argv[3].split(",")
else:
    # Compute all measures
    all = True

cr = ('cr' in cmds) # crossings
ue =  ('ue' in cmds) # edge length uniformity
st =  ('st' in cmds) # stress
np =  ('np' in cmds) # neighbors_preservation
lblbb =  ('lblbb' in cmds) #label to boundingBox ratio
lblarea =  ('lblarea' in cmds) #labels total area
bb =  ('bb' in cmds) #bounding box
upflow =  ('upflow' in cmds) #upward flow
lblo = ('lblo' in cmds) #labels overlaping area

weighted = False #Weighted version of measures

output_txt = "Metrics for " + graph_name + "\n"
output_txt = nx.info(G) + "\n"
print(output_txt)

csv_head_line = "filename&"
csv_line = graph_name+"&"


all_pairs_sp = None
if cr or np:
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
        all_pairs_sp = nx.shortest_path(G, weight="weight")
    else:
        all_pairs_sp = nx.shortest_path(G)

if cr or all:
    crss = crossings.count_crossings(G, ignore_label_edge_cr=True)
    crossings_val = len(crss)
    output_line =  "CR: " + str(crossings_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "crossings&"
    csv_line += str(crossings_val) + "&"

if ue or all:
    uniedgelen_val = uniedgelen.uniformity_edge_length(G)
    output_line = "UE: " + str(uniedgelen_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "uniformity\_edge\_length&"
    csv_line += str(uniedgelen_val) + "&"

if st or all:
    stress_val = stress.stress(G, weighted=weighted, all_sp=all_pairs_sp)
    output_line = "ST: " + str(stress_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "stress&"
    csv_line += str(stress_val) + "&"


if np or all:
    neigpres_val = neigpres.compute_neig_preservation(G, weighted=weighted, all_sp=all_pairs_sp)
    output_line = "NP: " + str(neigpres_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "neighbors\_preservation&"
    csv_line += str(neigpres_val) + "&"

if lblbb or all:
    labelsBBRatio_val = labelsmeas.labelsBBRatio(G)
    output_line = "lblbb: " + str(labelsBBRatio_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "boundingbox\_ratio\_to\_labels&"
    csv_line += str(labelsBBRatio_val) + "&"

if lblarea or all:
    totLabelsArea_val = labelsmeas.totLabelsArea(G)
    output_line = "lblarea: " + str(totLabelsArea_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "labels\_total\_area&"
    csv_line += str(totLabelsArea_val) + "&"

if bb or all:
    bbox_val = othermeas.boundingBox(G)
    output_line = "BB: " + str(bbox_val)
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "bounding\_box"
    csv_line += str(bbox_val) + "&"

if lblo or all:
    value = labelsmeas.totLabelsOverlappingArea(G)
    output_line =  "lblo: " + str(value) + "\n"
    output_txt += output_line
    csv_head_line += "lblo;"
    csv_line += str(value) + "&"
    print(output_line)

if upflow:
    upflow_val = upwardflow.compute_upwardflow(G)
    output_txt += "upflow: " + str(upflow_val) + "\n"
    output_txt += output_line + "\n"
    print(output_line)
    csv_head_line += "upflow&"
    csv_line += str(upflow_val) + "&"


csv_head_line += "\\\\ \\hline \n"
csv_line += "\\\\ \\hline \n"


# print(output_txt)

exists = os.path.isfile(outputTxtFile)
if not exists:
    fh = open(outputTxtFile, 'w')
    fh.write(csv_head_line)

fh = open(outputTxtFile, 'a')
fh.write(csv_line)
