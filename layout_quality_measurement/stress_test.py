#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import math

import stress as st

s_path = sys.argv[1]
g_path = sys.argv[2]
outputTxtFile = sys.argv[3]

input_file_name = os.path.basename(s_path)
graph_name = input_file_name.split(".")[0]

S = nx_read_dot(s_path)
G = nx_read_dot(g_path)
G=sorted(nx.connected_component_subgraphs(G), key=len, reverse=True)[0]



S_induced = nx.Graph(G.subgraph(S))

stress_val_S = st.stress(S)
stress_val_SG = st.stress(S, G)
stress_val_SGI = st.stress(S, S_induced)



output_txt = "Metrics for " + graph_name + "\n"
output_txt += "stress_S:" + str(stress_val_S) + "\n"
output_txt += "stress_SG:" + str(stress_val_SG) + "\n"
output_txt += "stress_SGI:" + str(stress_val_SGI) + "\n"



print(output_txt)


csv_head_line = "filename;stress;stress(induced);stress(complete)\n"
csv_line = graph_name+";"+ str(stress_val_S) + ";" + str(stress_val_SGI) + ";" +  str(stress_val_SG) + "\n"


exists = os.path.isfile(outputTxtFile)
if not exists:
    fh = open(outputTxtFile, 'w')
    fh.write(csv_head_line)

fh = open(outputTxtFile, 'a')
fh.write(csv_line)
