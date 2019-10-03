#dot_to_svg.py g_path ,  outputpath  , scalefactor , vertexsize

import os
import sys
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
vertexsize='5'

def scale(G,times):
	for x in G.nodes():
		x1=float( G.node[x]['pos'].split(",")[0]) * times
		y1=float(G.node[x]['pos'].split(",")[1]) * times
		G.node[x]['pos']= str(x1) + "," + str(y1)
#		G.node[x]['pos']= "" + str( float(G.node[x]['pos'].split(",")[0])* times ) + ","+  str( float(G.node[x]['pos'].split(",")[1] * times))
	return G

def nodes_drawing(G):
	circles=""
	lines=""
	fillcolor="#000000"
	rect=""
	lbl=""
	combined=""
	for x in G.nodes():
		x1,y1= G.node[x]["pos"].split(",")
		w=round(float(  G.node[x]["width"]),2)
		h=round(float(G.node[x]["height"]),2)
		xtxt=str((float(x1)+w/2))
		ytxt=str((float(y1)+h/2))
		boxxy=G.node[x]["boxxy"].split(",")
		rect=  '<rect width="'+str(w)+'" x="'+boxxy[0] +'" y="'+ boxxy[1] +'" height="'+str(h)+'" style="fill:rgb(255,255,255);stroke-width:2;stroke:rgb(0,0,0)" />\n'
		lbl= '<text text-anchor="left" x="'+str( float(boxxy[0])+3 ) +'" y="'+ str((float(boxxy[1]) +  h/2+5)) +'" font-size="'+G.node[x]["fontsize"]+'" font-family="arial" fill="black">'+G.node[x]["label"] +'</text>'
		combined=combined+ '<g>' + rect + lbl + "</g>"
		circles=circles+ '<circle cx="'+ x1 + '"  cy="'+ y1+'" r="'+vertexsize+'" stroke="'+fillcolor+'"  stroke-width="1" fill="'+fillcolor+'"/>\n'
	for x in G.edges():
		x1,y1=G.node[x[0]]["pos"].split(",")
		x2,y2=G.node[x[1]]["pos"].split(",")
		lines=lines+ ' <line x1="'+x1+'" y1="'+y1+'" x2="'+x2+'" y2="'+y2+'" stroke="'+fillcolor+'" stroke-width="1" />\n'
	return rect, lines, lbl, combined


def get_bounding_box(G):

   xmin=0
   ymin=0
   xmax=0
   ymax=0
   ofset=0
   for x in G.nodes():
	   xmin=min(xmin,float(G.node[x]["pos"].split(",")[0]) )
	   xmax=max(xmax,float(G.node[x]["pos"].split(",")[0]) )
	   ymin=min(ymin,float(G.node[x]["pos"].split(",")[1]) )
	   ymax=max(ymax,float(G.node[x]["pos"].split(",")[1]) )
   return  xmin ,ymin ,xmax ,ymax

if len(sys.argv)<2:
	print ("Enter dot file with positions:")
	quit()



g_path = sys.argv[1]
outputpath = ""
scalefactor=1#sys.argv[2]
vertexsize='10'#sys.argv[3]

g_name = os.path.basename(g_path).split(".")[0]

G=nx.Graph(pgv.AGraph(g_path))
G=nx.Graph(G) #To remove multiple edges (if exist)

scalefactor=float(scalefactor)
#G=scale(G,scalefactor)
nodes, egeds, lbl,combined =nodes_drawing(G)


xmin,ymin,xmax,ymax=get_bounding_box(G)
print(xmin,ymin,xmax,ymax)
padding=50
w=xmax-xmin+padding
h=ymax-ymin+padding

#<svg height="15000" width="15000"   viewBox="-10000 -10000 15000  15000"  xmlns="http://www.w3.org/2000/svg" xmlns:xlink= "http://www.w3.org/1999/xlink"  >
svgtag='<svg  width="'+str( w) +'" height="'+str(h)+'"    viewBox="'+ str(xmin-padding) +' '+str(ymin-padding)+ ' ' + str( w) +' ' + str(h) + '\"  xmlns="http://www.w3.org/2000/svg" xmlns:xlink= "http://www.w3.org/1999/xlink"  >'
svg_header='<?xml version="1.0" encoding="UTF-8" standalone="no"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">'
svg_header=svg_header+svgtag+ '<rect   x="'+str(xmin)+'" y="'+str(ymin)+'" width="100%" height="100%" fill="white"/>'



bcurve_svg=svg_header+egeds+combined+"</svg>"
x=open(outputpath+g_name+'.svg','w')
x.write(bcurve_svg)
x.close()
