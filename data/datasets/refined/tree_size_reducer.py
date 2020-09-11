import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot
import copy

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

def write_txt_file(fname, G):
  f = open(fname, "w")
  for u, v in G.edges():
   f.write('"'+u+'" -- "'+v+'"\n')
  f.close()

def reduce_tree(bot_tree, top_tree, reduction_size_arr):
 output_trees = []
 for i in range(len(reduction_size_arr)):
  cur_reduction_size = reduction_size_arr[i]
  while cur_reduction_size>0:
   leaf_nodes = [x for x in bot_tree.nodes() if bot_tree.degree(x)==1]
   found_node_to_remove = False
   for l in leaf_nodes:
    if not l in top_tree.nodes():
     bot_tree.remove_node(l)
     found_node_to_remove = True
     cur_reduction_size -= 1
     if cur_reduction_size==0:
      break
   if not found_node_to_remove:
    print("Error: no node to remove")
    quit()
  output_trees.append(copy.deepcopy(bot_tree))
 return output_trees

'''
folder_name = "TopicsLayersData"
file_name = ["Tree_level_0.txt", "Tree_level_1.txt", "Tree_level_2.txt", "Tree_level_3.txt", "Tree_level_4.txt"]

bot_tree = read_txt_file(folder_name + '/' + file_name[0])
reduction_size_arr = [55]
file_names = ["Graph_100.txt"]
reduced_trees = reduce_tree(bot_tree, nx.Graph(), reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_txt_file(fname, G)

bot_tree = read_txt_file(folder_name + '/' + "Graph_100.txt")
reduction_size_arr = [50]
file_names = ["Graph_50.txt"]
reduced_trees = reduce_tree(bot_tree, nx.Graph(), reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_txt_file(fname, G)

top_tree = read_txt_file(folder_name + '/' + file_name[0])
bot_tree = read_txt_file(folder_name + '/' + file_name[1])
reduction_size_arr = [98]
file_names = ["Graph_200.txt"]
reduced_trees = reduce_tree(bot_tree, top_tree, reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_txt_file(fname, G)

top_tree = read_txt_file(folder_name + '/' + file_name[1])
bot_tree = read_txt_file(folder_name + '/' + file_name[2])
reduction_size_arr = [162]
file_names = ["Graph_500.txt"]
reduced_trees = reduce_tree(bot_tree, top_tree, reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_txt_file(fname, G)
'''

'''
folder_name = "lastfm_felice"
file_name = ["lastfm_1.dot", "lastfm_2.dot"]

bot_tree = nx_read_dot(folder_name + '/' + file_name[0])
reduction_size_arr = [99, 100, 50]
file_names = ["Graph_200.dot", "Graph_100.dot", "Graph_50.dot"]
reduced_trees = reduce_tree(bot_tree, nx.Graph(), reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_dot(G, fname)

bot_tree = nx_read_dot(folder_name + '/' + file_name[1])
top_tree = nx_read_dot(folder_name + '/' + file_name[0])
reduction_size_arr = [119]
file_names = ["Graph_500.dot"]
reduced_trees = reduce_tree(bot_tree, top_tree, reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_dot(G, fname)
'''

folder_name = "topics_iqbal"
file_name = ["Topics_Layer_1.dot", "Topics_Layer_2.dot"]

bot_tree = nx_read_dot(folder_name + '/' + file_name[0])
reduction_size_arr = [113, 100, 50]
file_names = ["Graph_200.dot", "Graph_100.dot", "Graph_50.dot"]
reduced_trees = reduce_tree(bot_tree, nx.Graph(), reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_dot(G, fname)

bot_tree = nx_read_dot(folder_name + '/' + file_name[1])
top_tree = nx_read_dot(folder_name + '/' + file_name[0])
reduction_size_arr = [400]
file_names = ["Graph_500.dot"]
reduced_trees = reduce_tree(bot_tree, top_tree, reduction_size_arr)
for i in range(len(reduced_trees)):
 fname = folder_name + '/' + file_names[i]
 G = reduced_trees[i]
 write_dot(G, fname)


