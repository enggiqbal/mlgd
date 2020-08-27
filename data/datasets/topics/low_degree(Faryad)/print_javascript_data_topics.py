import networkx as nx
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import sys

levels = int(sys.argv[1])
#folder_name = "lastfm"
#file_name = ["lastfm_12nodes.dot", "lastfm_20nodes.dot", "lastfm_43nodes.dot", "lastfm_86nodes.dot", "lastfm_155nodes.dot"]
#folder_name = "."
#file_name = ["Layer1.dot", "Layer2.dot", "Layer3.dot", "Layer4.dot", "Layer5.dot", "Layer6.dot", "Layer7.dot"]
folder_name = "TopicsLayersData"
file_name = ["Tree_level_0.txt", "Tree_level_1.txt", "Tree_level_2.txt", "Tree_level_3.txt", "Tree_level_4.txt"]

def create_alphanum_dict(G):
 org_to_alphanum = dict()
 alphanum_to_org = dict()
 for n in G.nodes():
  n_short = n[:12]
  if n_short in alphanum_to_org.keys():
   n_short = n[:10]
   cnt = 2
   while True:
    cnt_str = str(cnt)
    if len(cnt_str)==1:
     cnt_str = '0'+cnt_str
    n_alphanum = n_short+cnt_str
    if not n_alphanum in alphanum_to_org.keys():
     org_to_alphanum[n] = n_alphanum
     alphanum_to_org[n_alphanum] = n
     break
    cnt = cnt + 1
  else:
   org_to_alphanum[n] = n_short
   alphanum_to_org[n_short] = n
 return org_to_alphanum, alphanum_to_org

def convert_nodes_to_alphanum(G, org_to_alphanum, alphanum_to_org):
 G2 = nx.Graph()
 for e in G.edges():
  u, v = e
  u2, v2 = org_to_alphanum[u], org_to_alphanum[v]
  G2.add_edge(u2, v2)
 return G2

def read_txt_file(fname):
  G = nx.Graph()
  f = open(fname, "r")
  while True:
    l = f.readline()
    if len(l)==0:
      break
    l = l.split('"')
    u = l[1]
    v = l[3]
    G.add_edge(u, v)
  f.close()
  return G

org_to_alphanum, alphanum_to_org = None, None

# check trees
for fname in file_name:
  if folder_name == "TopicsLayersData":
    G = read_txt_file(folder_name + '/' + fname)
  else:
    G = nx_read_dot(folder_name + '/' + file_name)
  org_to_alphanum, alphanum_to_org = create_alphanum_dict(G)
  if not nx.is_connected(G):
    print("not connected:", fname)
    quit()
  cycle = None
  try:
    cycle = nx.find_cycle(G)
  except:
    pass
  if not cycle==None:
    print("Found cycle in ", fname)
    quit()

if folder_name == "TopicsLayersData":
  G = read_txt_file(folder_name + '/' + file_name[levels-1])
else:
  G = nx_read_dot(folder_name + '/' + file_name[levels-1])
G = convert_nodes_to_alphanum(G, org_to_alphanum, alphanum_to_org)

number_lab_to_name_lab = dict()
name_lab_to_number_lab = dict()
edges_to_index = dict()
edge_distance = dict()

#for n in G.nodes():
# number_lab_to_name_lab[n] = G.nodes[n]["label"]
# name_lab_to_number_lab[G.nodes[n]["label"]] = n

for u in G.nodes():
  if not u in name_lab_to_number_lab.keys():
   name_lab_to_number_lab[u] = len(name_lab_to_number_lab.keys())
   number_lab_to_name_lab[len(name_lab_to_number_lab.keys())-1] = u

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
 #u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
 u = u[:12]
 v = v[:12]
 bfs_edges2.append([u, v])
bfs_edges = bfs_edges2
G2 = nx.Graph()
for e in G.edges():
 u, v = e
 #u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
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

  if folder_name == "TopicsLayersData":
    G = read_txt_file(folder_name + '/' + file_name[l])
  else:
    G = nx_read_dot(folder_name + '/' + file_name[l])
  G = convert_nodes_to_alphanum(G, org_to_alphanum, alphanum_to_org)

  bfs_edges = []
  center = nx.center(G)[0]
  for e in nx.bfs_edges(G, center):
   u, v = e
   bfs_edges.append((u, v))
  bfs_edges2 = []
  for e in bfs_edges:
   u, v = e
   #u, v = number_lab_to_name_lab[u], number_lab_to_name_lab[v]
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



