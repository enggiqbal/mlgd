
import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot


# Main Flow
graph_path = sys.argv[1]
tree_path = sys.argv[2]
outputpath = tree_path #sys.argv[3]

input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]

G=nx_read_dot(graph_path)
G=nx.Graph(G)

T=nx_read_dot(tree_path)
T=nx.Graph(T)

print(nx.info(G))
print(nx.info(T))

# weight_V_info=nx.get_node_attributes(G, 'weight')
label_V_info=nx.get_node_attributes(G, 'label')
width_V_info=nx.get_node_attributes(G, 'width')
heigth_V_info=nx.get_node_attributes(G, 'height')
level_V_info=nx.get_node_attributes(G, 'level')
#pos_V_info=nx.get_node_attributes(G, 'pos')
fontname_V_info=nx.get_node_attributes(G, 'fontname')
fontsize_V_info=nx.get_node_attributes(G, 'fontsize')



# nx.set_node_attributes(T, weight_V_info, 'weight')
nx.set_node_attributes(T, label_V_info, 'label')
nx.set_node_attributes(T, width_V_info, 'width')
nx.set_node_attributes(T, heigth_V_info, 'height')
# nx.set_node_attributes(T, level_V_info, 'level')
#nx.set_node_attributes(T, pos_V_info, 'pos')
nx.set_node_attributes(T, fontname_V_info, 'fontname')
nx.set_node_attributes(T, fontsize_V_info, 'fontsize')

write_dot(T, outputpath)
