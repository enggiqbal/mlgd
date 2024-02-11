import sys, os
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.drawing.nx_agraph import write_dot

#G=nx.read_graphml('g.graphml')
#write_dot(G, 'g.dot')
G=nx.Graph(pgv.AGraph("g.dot"))
nx.write_graphml()
L=8

folderpath="../../../datasets/topics/set2/input/"
outformat="Topics_Layer_{0}.dot"

T=[]
for i in range(0,L):
    t=nx.Graph(pgv.AGraph(folderpath+outformat.format(i+1)))
    T.append(t)

def extract(G,T):
    H=nx.Graph()
    attr={}
    for x in G.nodes():
        #import pdb; pdb.set_trace()
    #    print(x)
        nid=str(int (float((G.nodes[x]["Node Id"]))))
        #print(nid)
        #
        if str(nid) in T.nodes():
            H.add_node(nid)
            #k=G.nodes[x]
            k={}
            k["label"]=G.nodes[x]["label"].replace("&","")
            #k["pos"]=G.nodes[x]["x"] +","+ G.nodes[x]["y"]
            k["pos"]=str(float(G.nodes[x]["x"]) +  float(G.nodes[x]["Widthpx"])/2 ) + "," +  str(float(  G.nodes[x]["y"])+ float(G.nodes[x]["Heightpx"])/2)
            #k["pos"]=str(float(G.nodes[x]["x"]) ) + "," +  str(float(  G.nodes[x]["y"])- float(G.nodes[x]["Heightpx"])/2)
            k["fontsize"]=int(G.nodes[x]["Label Font Size"])
            k["height"]=float(G.nodes[x]["Heightpx"])
            k["width"]=float(G.nodes[x]["Widthpx"])
            k["boxxy"]=G.nodes[x]["x"] + ","+ G.nodes[x]["y"]

            #del k["Height"]
            #del k["Width"]
            #k["Height"]= round(  float(k["Height"]) - 0.2, 2)
            attr[nid]=k

            #import pdb; pdb.set_trace()

    nx.set_node_attributes(H,attr)

    for x in T.edges():
        H.add_edge(x[0],x[1])
    return H

for i in range(0,L):
    T_i=extract(G,T[i])
    f='T'+str(i+1)+'.dot'
    o='T'+str(i+1)+'.svg'
    pdf='T'+str(i+1)+'.pdf'
    write_dot(T_i,f)
    #os.system('neato -n2 ' + f + "   -Nfixedsize=true -Nshape=rectangle -Tsvg > "+ o)
    os.system('python3 dot_to_svg.py '+ f )
    os.system('/Applications/Inkscape.app/Contents/Resources/script --without-gui --export-pdf=$PWD/'+pdf+'   $PWD/'+o)
