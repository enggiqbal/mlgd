#Author
#Felice De Luca
#https://github.com/felicedeluca
import networkx as nx
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import vertexmanager


def dot_to_txt(input_file, output_file):
    G = nx_read_dot(input_file)
    print(nx.info(G))
    f = open(output_file, 'w')
    f.write(str(len(G.nodes()))+'\n')
    id_to_name = dict()
    name_to_id = dict()
    count = 0
    for node_id in G.nodes():
        node = G.node[node_id]
        name_to_id[node_id] = count
        count += 1
        x, y = vertexmanager.getCoordinate(node)
        f.write(str(x) + ' ' + str(y) + '\n')
    print(name_to_id)
    for edg in G.edges():
        f.write(str(name_to_id[edg[0]]) + " " + str(name_to_id[edg[1]]) + "\n")
    f.close()


# Main Flow

input_path = sys.argv[1]
output_path = sys.argv[2]

dot_to_txt(input_path, output_path)
