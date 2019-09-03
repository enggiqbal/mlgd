#Author
#Felice De Luca
#https://github.com/felicedeluca

import sys
import os

import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import math

import neighbors_preservation as np

s_path = sys.argv[1]
g_path = sys.argv[2]
outputTxtFile = sys.argv[3]

input_file_name = os.path.basename(s_path)
graph_name = input_file_name.split(".")[0]

S = nx_read_dot(s_path)
G = nx_read_dot(g_path)
G=sorted(nx.connected_component_subgraphs(G), key=len, reverse=True)[0]



S_induced = nx.Graph(G.subgraph(S))

np_val_S = np.compute_neig_preservation(S)
np_val_SG = np.compute_neig_preservation(S, G)
np_val_SGI = np.compute_neig_preservation(S, S_induced)



output_txt = "Metrics for " + graph_name + "\n"
output_txt += "np_S:" + str(np_val_S) + "\n"
output_txt += "np_SG:" + str(np_val_SG) + "\n"
output_txt += "np_SGI:" + str(np_val_SGI) + "\n"



print(output_txt)


csv_head_line = "filename;np;np(induced);np(complete)\n"
csv_line = graph_name+";"+ str(np_val_S) + ";" + str(np_val_SGI) + ";" +  str(np_val_SG) + "\n"


exists = os.path.isfile(outputTxtFile)
if not exists:
    fh = open(outputTxtFile, 'w')
    fh.write(csv_head_line)

fh = open(outputTxtFile, 'a')
fh.write(csv_line)
