from networkx.drawing.nx_agraph import write_dot
import networkx as nx
import pygraphviz as pgv
dotpath='/Users/iqbal/MLGD/mlgd/datasets/topics/set2/input/Topics_Layer_8.dot'
G=nx.Graph(pgv.AGraph(dotpath))

weightededgedot= open('Layer8.js', 'w') # as csv_file:


e = "\"source\":\"{}\", \"target\":\"{}\", \"weight\":{}"

#854 -- 3860[weight="7"];
#
#v ='''[label:"immunology", level=1, weight="2783" ,  height=0.56, width=2.33, fontsize= 30, fontname="Arial"];'''
v ='"id":"{}", "label":"{}", "level":{}, "weight":{} ,  "height":{}, "width":{}, "fontsize": {}, "fontname":"{}"'
nlist=""
for n in G.nodes():
	nlist=nlist+ "{ " + v.format(n,G.node[n]["label"],G.node[n]["level"],G.node[n]["weight"],G.node[n]["height"],G.node[n]["width"],G.node[n]["fontsize"],G.node[n]["fontname"] )  + " },\n"

nlist=nlist[:len(nlist)-3]+"}"
eid=0
elist=""
for edge in G.edges():
	elist=elist + "{"+e.format(edge[0],edge[1], G[edge[0]][edge[1]]["weight"]) + "},\n"
	eid=eid+1
elist=elist[:len(elist)-3]+"}"

weightededgedot.write ("var graph={ \"nodes\":[ " + nlist + "],\n \"links\":[ " + elist + "] }")
