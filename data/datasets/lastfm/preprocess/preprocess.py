import networkx as nx
import pygraphviz as pgv
from networkx.drawing.nx_agraph import write_dot
def containsNonAscii(s):
    return any(ord(i)>127 for i in s)

G=nx.Graph(pgv.AGraph("lastfm.dot"))
G2=nx.Graph(pgv.AGraph("lastfm.dot"))

for n in G.nodes():
    lbl=G.node[n]["label"]
    lbl=lbl.replace("\\n"," ")
    lbl=lbl.replace("\'"," ")
    lbl=lbl.replace("\"","")
    lbl=lbl.replace("&","and")
    if containsNonAscii(lbl):
        G2.remove_node(n)
        print (lbl)
        continue
    if len(lbl)>15:
        lbl=lbl[0:20]+"..."
    G2.node[n]["label"]=lbl




write_dot(G2,"lastfm_cleaned.dot")