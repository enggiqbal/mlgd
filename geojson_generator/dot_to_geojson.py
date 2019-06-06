#see license.txt 

import networkx as nx
import pygraphviz as pgv
import json
f="../T8.dot"
output="/Users/iqbal/openlayerapp/T8.geojson"
#/Users/iqbal/openlayerapp/map.geojson
G=nx.Graph(pgv.AGraph(f))

layerinput="/Users/iqbal/MLGD/mlgd/datasets/topics/set2/input/Topics_Graph.dot"
GL=nx.Graph(pgv.AGraph(layerinput))


n={}
n["type"]="Feature"
n["geometry"]={}
n["geometry"]["type"]="Point"
n["geometry"]["coordinates"]={}
n["properties"]={}
txt=""
i=0
for node in G.nodes():
    nodejson=n.copy()
    data= G.node[node]
    x=float(data["pos"].split(",")[0])
    y=float(data["pos"].split(",")[1])
    nodejson["geometry"]["coordinates"]=[x,y]
    nodejson["properties"]=data
    nodejson["properties"]["level"]= GL.node[node]["level"]
    txt=txt + json.dumps(nodejson, indent=2) + ", \n"
    i=i+1
    if i==6000:
        break

txt=txt[0:len(txt)-3]
txt= '''{
  "type": "FeatureCollection",
    "crs": {
    "type": "name",
    "properties": {
      "name": "EPSG:3857"
    }
  },
  "features": [
  '''+txt + "\n]}"
f=open(output,"w")
f.write(txt)
f.close()
