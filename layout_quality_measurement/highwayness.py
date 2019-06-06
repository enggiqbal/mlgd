import sys
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.readwrite.graphml import read_graphml

if len(sys.argv)<2:
	print ("Enter dot file with positions ")
	quit()

filepath=sys.argv[1]
x=filepath.split(".")
inputtype=x[len(x)-1]
print ("filetype:", inputtype)
#inputtype='dot'
#inputtype='graphml'

#'highwaynessExp/path1.graphml'

if inputtype=='graphml':
	G=read_graphml(sys.argv[1])
	G=G.to_undirected()
else:
	G=nx.Graph(pgv.AGraph(filepath))
#G=nx.Graph(pgv.AGraph("Layer_7_drawing_improved.dot"))
#G=nx.Graph(pgv.AGraph(sys.argv[1]))

def calculateslope(G):
	#networkx v2
	for x in  G.edges():
		s=slope(G, x[0], x[1])
		nx.set_edge_attributes(G, {x:{'s':s}})
	return G

def calculateslopeX(G):
	for i in range(0,len(G.edges())):
		s=slope(G, G.edges()[i][0], G.edges()[i][1])
		G[G.edges()[i][0]][G.edges()[i][1]]=s
		G[G.edges()[i][1]][G.edges()[i][0]]=s
	return  G
def slope(G,a,b):
	if inputtype=='graphml':
		x1,y1=G.node[a]['x'], G.node[a]['y']
		x2,y2=G.node[b]['x'], G.node[b]['y']
	else:
		x1,y1=G.node[a]['pos'].split(",")
		x2,y2=G.node[b]['pos'].split(",")
	if x1==x2:
		ang= math.pi/2
	else:
		m=(float(y1)-float(y2))/(float(x1)-float(x2))
		ang=math.atan(m)
	return ang**2

def sumofslope(G,p):
	ang=0
	for i in range(0,len(p)-2):
		ang=ang+ abs( G[p[i]][p[i+1]]['s'] - G[p[i+1]][p[i+2]]['s'])
	return ang

G=calculateslope(G)

leaves=[]
for x in G.nodes():
	if G.degree(x)==1:
		leaves.append(x)


totalhighwayness=0
for i in range(0, len(leaves)):
	for j in range(i+1, len(leaves)):
		p=nx.shortest_path(G,leaves[i],leaves[j])
		totalhighwayness=totalhighwayness+ sumofslope(G,p) /((len(p)-1)*math.pi * math.pi )


print('score (low is better):',totalhighwayness)
print('normalized score (low is better):',totalhighwayness/ ((len(leaves) * (len(leaves)-1))/2))
