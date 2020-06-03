import sys
import networkx as nx
import pygraphviz as pgv
from networkx.readwrite import json_graph
import tkinter
from tqdm import tqdm
from tkinter import *
fontsize=[30,25,20,15,12,10,9,8]
fontname='Arial'
def getBoxSize(txt,l):
    Window = Tk()
    Window.geometry("500x500+80+80")
    frame = Frame(Window) # this will hold the label
    frame.pack(side = "top")
    measure = Label(frame, font = (fontname, fontsize[l]), text = txt)
    measure.grid(row = 0, column = 0) # put the label in
    measure.update_idletasks() # this is VERY important, it makes python calculate the width
    width = round(  round(measure.winfo_width() / 72, 2)  * 72 * 1.10, 2) # get the width
    height= round( round(measure.winfo_height()/72 ,2)   * 72 * 1.10, 2 )
#    width = round(measure.winfo_width() /72, 2) # get the width
#    height= round(measure.winfo_height()/72,2)

    return height,width, str( fontsize[l])

def get_all_node_att(T,G):
    allboxatt={}
    for x in tqdm(T.nodes()):
        layer=str(getLayer(x))
        h,w,s=getBoxSize(G.nodes[x]["label"],int(layer)-1)
        hw=" height="+str(h) + ", width="+ str( w)+ ", fontsize= "+ s+", fontname=\""+fontname+"\""
        nodes=  "" + str( x) + " [label=\""+G.nodes[x]["label"]+"\", level="+layer+", weight=\""+  G.nodes[x]["weight"] +"\" , "+ hw+"];\n"
        allboxatt[str( x)]=nodes
        #print(x)
    return allboxatt


from networkx.drawing.nx_agraph import write_dot
T=[]
L=8
#
#folderpath="EU/"
input_folderpath="tmp/"
output_folder='outputs/'
#fileformat="Layer_{0}_EU_core.dot"
input_file_format="Layer_{0}.dot"
outformat="output_Layer_{0}.dot"

G_file_name="graph_connected.dot"
#G_file_name="EU_core_orginal.dot"

#G_out="G_EU_core_id.dot"

G_out="output_graph.dot"

G=nx.Graph(pgv.AGraph(input_folderpath+G_file_name))

for i in range(0,L):
    R=nx.Graph(pgv.AGraph(input_folderpath+input_file_format.format(i+1)))
    T.append(R)

def getLayer(x):
    for i in range(0,L):
        if x in T[i].nodes():
            return i+1
def write_to_file(folderpath,G_out, nodes, edges ):
    f=open(folderpath+G_out,"w")
    txt="graph {" + nodes + edges + "}"
    f.write(txt)
    f.close()
    print("done writing G", )

def writeG(T,G,allboxatt):
    nodes=""
    edges=""
    for x in T.nodes():
        nodes= nodes+allboxsizes[ str( x)]
    for x in G.edges():
        w=G[x[0]][x[1]]['weight']
        edges=edges + x[0] + " -- " + x[1] + "[weight=\""+ str(w)+"\"];\n"
    write_to_file(output_folder,G_out, nodes, edges )

def writeLayer(T,l,allboxatt):
    nodes=""
    edges=""
    for x in T.nodes():
        nodes= nodes+allboxsizes[ str( x)]
    for x in T.edges():
        w=G[x[0]][x[1]]['weight']
        edges=edges + x[0] + " -- " + x[1] + "[weight=\""+ str(w)+"\"];\n"

    write_to_file(output_folder,outformat.format(l), nodes, edges )

allboxsizes=get_all_node_att(T[L-1],G)

for i in range(0,L):
     writeLayer(T[i],str(i+1),allboxsizes)



writeG( T[L-1],G,allboxsizes)
