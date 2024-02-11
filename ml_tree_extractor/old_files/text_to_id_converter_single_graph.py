import sys
import networkx as nx
import pygraphviz as pgv
from networkx.readwrite import json_graph

from networkx.drawing.nx_agraph import write_dot

G_file_name="/Users/iqbal/MLGD/mlgd/datasets/topics/orginal/Topics_Graph.dot"
G_out="/Users/iqbal/MLGD/mlgd/datasets/topics/orginal/Topics_Graph_Connected.dot"

G=nx.Graph(pgv.AGraph(G_file_name))
H=nx.connected_component_subgraphs(G)
G=list(H)[0]

def replace(str):
    str=str.replace("\xe9","").replace("\xf3","").replace("\xed","").replace("\xe1","").replace("\xfa","")
    return str



nodeid={}

i=1
for x in G.nodes():
    nodeid[x]=i
    i=i+1

def writeG(G):
    nodes=""
    edges=""
    for x in G.nodes():
        #import pdb; pdb.set_trace()
        nodes=nodes+ "" + str( nodeid[x]) + " [label=\""+replace(G.nodes[x]["label"])+"\", weight=\""+  G.nodes[x]["weight"] +"\" ];\n"
    for x in G.edges():
        if x[0] in nodeid and x[1] in nodeid:
            w=G[x[0]][x[1]]['weight'] if 'weight' in G[x[0]][x[1]] else 1
            edges=edges + str(nodeid[x[0]]) + " -- " + str(nodeid[x[1]]) + "[weight=\""+ str(w)+"\"];\n"

    f=open(G_out,"w")
    txt="graph {" + nodes + edges + "}"
    f.write(txt)
    f.close()
    print("done writing G", )


writeG(G)
