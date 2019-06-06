import sys, os
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.drawing.nx_agraph import write_dot

G=nx.Graph(pgv.AGraph("20190603_iq_T8.dot"))

#G=nx.Graph(pgv.AGraph("Last_layer_cross_free_drawing_dot_mod.dot"))
L=8



folderpath="../../datasets/topics/set2/input/"
outformat="Topics_Layer_{0}.dot"

T=[]
for i in range(0,L):
    t=nx.Graph(pgv.AGraph(folderpath+outformat.format(i+1)))
    T.append(t)

def extract(T):
    G1=G.copy()

    H=nx.Graph()
    attr={}
    for x in G.nodes():
        if x in T.nodes():

            H.add_node(x)
            attr[x]=G1.node[x]
            attr[x]["height"]=round(float(G.nodes[x]["height"]))
            attr[x]["width"]=round(float(G.nodes[x]["width"]))


    nx.set_node_attributes(H,attr)

    for x in T.edges():
        H.add_edge(x[0],x[1])
    return H

for i in range(0,L):
    T_i=extract(T[i])
    f='T'+str(i+1)+'.dot'
    o='T'+str(i+1)+'.pdf'
    write_dot(T_i,f)
#    os.system('neato -n2 ' + f + " -Nshape=rectangle -Tpdf > "+ o)
