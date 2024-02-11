import networkx as nx
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import sys

levels = int(sys.argv[1])
folder_name = "lastfm"
file_name = ["lastfm_12nodes.dot", "lastfm_20nodes.dot", "lastfm_43nodes.dot", "lastfm_86nodes.dot", "lastfm_155nodes.dot"]
#folder_name = "topics_layer"
#file_name = ["Topics_Layer_1.dot", "Topics_Layer_2.dot", "Topics_Layer_3.dot", "Topics_Layer_4.dot", "Topics_Layer_5.dot", "Topics_Layer_6.dot", "Topics_Layer_7.dot", "Topics_Layer_8.dot"]

# check trees
for fname in file_name:
  G = nx_read_dot(folder_name + '/' + fname)
  print(len(G.nodes()))
  cycle = None
  try:
    cycle = nx.find_cycle(G)
  except:
    pass
  if not cycle==None:
    print("Found cycle in ", fname)
    quit()

G = nx_read_dot(folder_name + '/' + file_name[levels-1])

number_lab_to_name_lab = dict()
name_lab_to_number_lab = dict()
edges_to_index = dict()
edge_distance = dict()

for n in G.nodes():
 number_lab_to_name_lab[n] = G.nodes[n]["label"]
 name_lab_to_number_lab[G.nodes[n]["label"]] = n


label_to_index = dict()
index_to_label = dict()
bfs_edges = []
#center = "1149"
center = nx.center(G)[0]
for e in nx.bfs_edges(G, center):
 u, v = e
 bfs_edges.append((u, v))
bfs_edges2 = []
for e in bfs_edges:
 u, v = e
 u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
 u = u[:12]
 v = v[:12]
 bfs_edges2.append([u, v])
bfs_edges = bfs_edges2
G2 = nx.Graph()
for e in G.edges():
 u, v = e
 u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
 u = u[:12]
 v = v[:12]
 G2.add_edge(u, v)
G = G2
for i in range(len(bfs_edges)):
  edges_to_index[(bfs_edges[i][0], bfs_edges[i][1])] = i
edge_list = []
#for e in nx.bfs_edges(G, "machine learning"):
for e in bfs_edges:
 #print(e)
 u, v = e
 if not u in label_to_index.keys():
  label_to_index[u] = len(label_to_index.keys())
  index_to_label[len(label_to_index.keys())-1] = u
 if not v in label_to_index.keys():
  label_to_index[v] = len(label_to_index.keys())
  index_to_label[len(label_to_index.keys())-1] = v
 edge_list.append([label_to_index[u], label_to_index[v]])
#print(label_to_index)
#print(index_to_label)
print("my_edges = ", bfs_edges)
print("label_to_id = ", label_to_index)
print("id_to_label = ", index_to_label)

l = levels-1
cur_dis = 50
while l>=0:

  G = nx_read_dot(folder_name + '/' + file_name[l])

  bfs_edges = []
  center = nx.center(G)[0]
  for e in nx.bfs_edges(G, center):
   u, v = e
   bfs_edges.append((u, v))
  bfs_edges2 = []
  for e in bfs_edges:
   u, v = e
   u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
   u = u[:12]
   v = v[:12]
   bfs_edges2.append([u, v])
  bfs_edges = bfs_edges2

  for i in range(len(bfs_edges)):
    e = bfs_edges[i]
    if (e[0], e[1]) in edges_to_index.keys():
      edge_index = edges_to_index[(e[0], e[1])]
    elif (e[1], e[0]) in edges_to_index.keys():
      edge_index = edges_to_index[(e[1], e[0])]
    else:
      print("Edge not found!")
      quit()
    edge_distance[edge_index] = cur_dis
  l -= 1
  cur_dis += 50

print("edge_distance = ", edge_distance)



