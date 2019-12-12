#see license.txt 
import xml.etree.ElementTree as ET
import numpy as np
import json
import networkx as nx
import pygraphviz as pgv
#direct_topics/impred_topics/impred_lastfm

#layout_name="direct_topics"
#layout_name="impred_topics"
layout_name="impred_topics"
layout_output_dir="impred_topics2"
nodecoordinate={}
def getLayer0(t1):
    numberofnodes=30
    paths=nx.shortest_path(t1)
    c=nx.get_node_attributes(t1,'weight')
    c={k:int(v) for k, v in c.items()}
    s=sorted(c.items(), key=lambda x: x[1], reverse=True)
    selectednodes=[]
    for node in range(0,numberofnodes):
        selectednodes.append(s[node][0])
    H=nx.Graph()
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=paths[selectednodes[x]][selectednodes[y]]
            for i in range(0,len(p)-1):
                H.add_node(p[i])
                H.add_edge(p[i], p[i+1])
    print(len(H.nodes()))
    return H

globaldatapath="../visualization_system/geojson/"

if layout_name=="direct_topics":
#for direct approach
    mappath="../direct_approach_output/mapDirect_modularity.svg"
    clusteroutput=globaldatapath + layout_output_dir+ "/cluster.geojson"
    polylineoutput=globaldatapath + layout_output_dir+"/cluster_boundary.geojson"
    edgesoutput=globaldatapath + layout_output_dir+"/edges.geojson"
    nodesoutput=globaldatapath + layout_output_dir+"/nodes.geojson"
    alledges=globaldatapath + layout_output_dir+"/alledges.geojson"

    inputdir="../data/datasets/topics/set2/input/"
    input_graph="Topics_Graph.dot"
    layer_file_format="Topics_layer_{0}.dot"


if layout_name=="impred_topics":
    mappath="../map_generator/impred/map.svg"
    clusteroutput=globaldatapath + layout_output_dir+"/im_cluster.geojson"
    polylineoutput=globaldatapath + layout_output_dir+"/im_cluster_boundary.geojson"
    edgesoutput=globaldatapath + layout_output_dir+"/im_edges.geojson"
    nodesoutput=globaldatapath + layout_output_dir+"/im_nodes.geojson"
    alledges=globaldatapath + layout_output_dir+"/im_alledges.geojson"

    inputdir="../data/datasets/topics/set2/input/"
    input_graph="Topics_Graph.dot"
    layer_file_format="Topics_layer_{0}.dot"
    layout_cordinate_path="../map_generator/impred/T8_for_map.dot"





if layout_name=="impred_lastfm":
    mappath="../map_generator/maplastfm/map.svg"
    clusteroutput=globaldatapath + layout_output_dir+"/im_cluster.geojson"
    polylineoutput=globaldatapath + layout_output_dir+"/im_cluster_boundary.geojson"
    edgesoutput=globaldatapath + layout_output_dir+"/im_edges.geojson"
    nodesoutput=globaldatapath + layout_output_dir+"/im_nodes.geojson"
    alledges=globaldatapath + layout_output_dir+"/im_alledges.geojson"

    inputdir="../ml_tree_extractor/outputs/"
    input_graph="output_graph.dot"
    layer_file_format="output_Layer_{0}.dot"
    layout_cordinate_path="../layout_generator/ZMLTpipeline/tmp_workspace/lastfm/final/output_Layer_8.dot"



G=nx.Graph(pgv.AGraph(inputdir+input_graph))

T=[]
L=8
#################adding layer0###############
t1=nx.Graph(pgv.AGraph(inputdir+layer_file_format.format(1)))
t=getLayer0(t1)
T.append(t)
###########
for i in range(0,L):
    R=nx.Graph(pgv.AGraph(inputdir+layer_file_format.format(i+1)))
    T.append(R)




def process_alledges(G,alledges):
    global inputdir
    global input_graph
    global layout_cordinate_path
    G=nx.Graph(pgv.AGraph(inputdir+ input_graph))
    G_cord=nx.Graph(pgv.AGraph(layout_cordinate_path))
    id=0
    txt=""
    for e in G.edges():
        #import pdb; pdb.set_trace()
        edge=n.copy()
        edge["id"]= id
        id =id +1

        edge["geometry"]["type"]="LineString"
        edge["properties"]=G.edges[e]
        n1=e[0]
        n2=e[1]
        edge["properties"]["src"]=int(n1)
        edge["properties"]["dest"]=int(n2)
        #print(n1, n2)
        if "weight" in G.edges[e]:
            edge["properties"]["weight"]=G.edges[e]['weight']
        #import pdb; pdb.set_trace()
        a=G_cord.nodes[n1]["pos"]
        b=G_cord.nodes[n2]["pos"]
        '''
        x1=float(a.split(",")[0])  
        y1=float(a.split(",")[1])
        h = float(G_cord.nodes[n1]["height"]) * 1.10 * 72  # inch to pixel conversion
        w =float(G_cord.nodes[n1]["width"]) * 1.10 * 72 # inch to pixel conversion
        

        x2=float(b.split(",")[0])  
        y2=float(b.split(",")[1])

        points_array=[[x1-w/2,y1-h/2], [x2+w/2,y2-h/2] ]
        '''
        a=nodecoordinate[G.node[n1]["label"]]
        b=nodecoordinate[G.node[n2]["label"]]
        points_array=[a, b]
        #print(points_array)
        #print(e)
        edge["geometry"]["coordinates"]=points_array

        #import pdb; pdb.set_trace()
#            edge["properties"]["level"]=str(max(  int(G.nodes[e1]['level']),  int(G.nodes[e2]['level'])))

        txt=txt+ json.dumps(edge, indent=2)+ ", \n"
        #import pdb; pdb.set_trace()
    write_to_file(txt,alledges)






def getLayer(x):
    for i in range(0,L+1): #considering added layer0
        if x in T[i].edges():
            return i+1


def getLevel(x):
    for i in range(0,L+1): #considering added layer0
        if x in T[i].nodes():
            return i+1







#for direct approach


root = ET.parse(mappath).getroot()
n={}
n["type"]="Feature"
n["geometry"]={}
n["geometry"]["type"]=""
n["geometry"]["coordinates"]={}
n["properties"]={}

header='''{
  "type": "FeatureCollection",
    "crs": {
    "type": "name",
    "properties": {
      "name": "EPSG:3857"
    }
  },
  "features": [
  '''
footer= "\n]}"

def process_polygon(xml,id):
    polygon=n.copy()
    polygon["geometry"]["type"]="Polygon"
    polygon["id"]="cluster" + str(id)
    points=xml.attrib.pop('points')
    points_array=[[ float(p.split(",")[0]), float(p.split(",")[1])  ] for p in points.split(" ")]
    polygon["properties"]=xml.attrib
    polygon["properties"]["label"]=""
    polygon["geometry"]["coordinates"]=[points_array]
    return json.dumps(polygon, indent=2)



def process_polyline(xml):
    polygon=n.copy()
    polygon["geometry"]["type"]="LineString"
    points=xml.attrib.pop('points')
    #import pdb; pdb.set_trace()
    points_array=[[ float(p.split(",")[0]), float(p.split(",")[1])  ] for p in points.strip().split(" ")]
    polygon["properties"]=xml.attrib
    polygon["properties"]["label"]=""
    polygon["geometry"]["coordinates"]=points_array
    return json.dumps(polygon, indent=2)



def process_edge(xml,G):
    #import pdb; pdb.set_trace()
    edge=n.copy()
    edge["geometry"]["type"]="LineString"
    points=xml[1].attrib.pop('d')
    points=points.replace("M"," ").replace("D"," ").replace("C"," ")
    #import pdb; pdb.set_trace()
    points_array=[[ float(p.split(",")[0]), float(p.split(",")[1])  ] for p in points.strip().split(" ")]
    edge["properties"]=xml[1].attrib
    n1=xml[0].text.split("--")[0]
    n2=xml[0].text.split("--")[1]
    a=nodecoordinate[G.node[n1]["label"]]
    b=nodecoordinate[G.node[n2]["label"]]
    edge["properties"]["src"]=n1
    edge["properties"]["dest"]=n2
    edge["properties"]["label"]=G.node[n1]["label"] + " -- " +  G.node[n2]["label"]
    #edge["properties"]["weight"]=G.edges[(n1,n2)]["weight"] 
    #todo: ignoring edge weights for lastfm data
    edge["geometry"]["coordinates"]=[a,b]
    edge["properties"]["level"]=getLayer((n1,n2))
    return json.dumps(edge, indent=2)




 
def process_node(xml,G):
    node_g=xml[0].text
    node=n.copy()
    node["geometry"]["type"]="Point" #"Point"
    node["id"]="node" + node_g
    node["properties"]=G.node[node_g]
    x=float(xml[2].attrib.pop('x'))  
    y=float(xml[2].attrib.pop('y'))
    nodecoordinate[G.node[node_g]["label"]]=[x,y]
    h= float(node["properties"]["height"]) * 1.10 * 72  # inch to pixel conversion
    w=float(node["properties"]["width"]) * 1.10 * 72 # inch to pixel conversion
    points_array=[[x-w/2,y-h/2], [x+w/2,y-h/2], [x+w/2,y+h/2], [x-w/2,y+h/2], [x-w/2,y-h/2]]

    node["properties"]["height"]=h
    node["properties"]["width"]= w

    node["geometry"]["coordinates"]= [x,y] #//[x,y]
    node["properties"]["level"]=getLevel(node_g)
    return json.dumps(node, indent=2)


def write_to_file(data,file):
    data=data[0:len(data)-3]
    data=header+ data+footer
    f=open(file,"w")
    f.write(data)
    f.close()




polygonCount=0
polylindCount=0
nodeCount=0
edgeCount=0
polygons=""
polylines=""
edges=""
nodes=""


for child in root.findall('*[@id="graph0"]/*'):
    if "{http://www.w3.org/2000/svg}g"==child.tag:
        if child.attrib["class"]=="node":
            nodeCount=nodeCount+1
            nodes=nodes+ process_node(child,G)+ ", \n"


for child in root.findall('*[@id="graph0"]/*'):
    if "polygon" in child.tag:
        if polygonCount!=0: #scape 1st rectangle
            polygons=polygons+ process_polygon(child,polygonCount) + ", \n"
        polygonCount=polygonCount+1
    if "polyline" in child.tag:
        polylines=polylines+ process_polyline(child) + ", \n"
    if "{http://www.w3.org/2000/svg}g"==child.tag:
        if child.attrib["class"]=="edge":
            edges=edges+ process_edge(child,G)+ ", \n"
            edgeCount=edgeCount+1

process_alledges(G,alledges)

print(polygonCount,polylindCount,nodeCount)

write_to_file(polygons,clusteroutput)
write_to_file(polylines,polylineoutput)
write_to_file(edges,edgesoutput)

write_to_file(nodes,nodesoutput)



'''
<g id="node2830" class="node">
<title>3506</title>
<text text-anchor="middle" x="23888.5" y="-6861.22" font-family="Helvetica,sans-Serif" font-weight="bold" font-size="15.00">block copolymers</text>
</g>
'''

'''

<g id="edge5238" class="edge">
<title>3324&#45;&#45;971</title>
<path fill="none" stroke="grey" d="M7023.05,-6911.53C7021.39,-6919.29 7019.08,-6930.12 7017.69,-6936.64"/>
</g>
'''
