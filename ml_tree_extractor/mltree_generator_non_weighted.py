
import sys
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.drawing.nx_agraph import write_dot
#EU-core nodecountinlevels=[0.10,0.25,0.30,0.40,0.70,0.80,0.95,1.0]
nodecountinlevels=[0.10,0.25,0.30,0.40,0.70,0.80,0.95,1.0]

def getnodes(s,n):
    selected=[]
    for node in range(0,n):
        selected.append(s[node][0])
    return selected

def genTree(G,selectednodes):
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=nx.shortest_path(G,x,y)



def extract(mst,selectednodes):
    H=nx.Graph()
    for x in mst.nodes():
        if x in selectednodes:
            H.add_node(x,G.node[x])
    for x in T.edges():
        H.add_edge(x[0],x[1])
    return H

def extract(mst,selectednodes):
    H=nx.Graph()
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=nx.shortest_path(mst,selectednodes[x],selectednodes[y])
            for i in range(0,len(p)-1):
                H.add_node(p[i])
                H.add_edge(p[i], p[i+1])
    return H






G=nx.Graph(pgv.AGraph("r.dot"))
H=nx.connected_component_subgraphs(G)
G=list(H)[0]


mst=nx.minimum_spanning_tree(G)
c=nx.degree_centrality(G)
s=sorted(c.items(), key=lambda x: x[1], reverse=True)
T=nx.Graph()
for i in range(0, len(nodecountinlevels)):
    selectednodes= list(set(T.nodes()+ getnodes(s,int(nodecountinlevels[i] * len(G.nodes())))))
    print(len(selectednodes))
    T=extract(mst,selectednodes)
    print("Layer", i+1, "nodes:", len(T.nodes()))
    write_dot(T,'Layer_'+str(i+1)+'_EU_core.dot')

write_dot(G,'EU_core.dot')
