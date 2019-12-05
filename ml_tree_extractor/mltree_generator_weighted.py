import sys
import networkx as nx
import pygraphviz as pgv
import math
from networkx.readwrite import json_graph
from networkx.drawing.nx_agraph import write_dot
#EU-core nodecountinlevels=[0.10,0.25,0.30,0.40,0.70,0.80,0.95,1.0]
nodecountinlevels=[0.005,0.05,0.10,0.20,0.30,0.50,0.85,1.0]
filepath=sys.argv[1]
output_dir='tmp/'
def isEnglish(s):
    try:
        s.encode(encoding='utf-8').decode('ascii')
    except UnicodeDecodeError:
        return False
    else:
        return True

def fixEdgeWeightAndRemoveNonEngNode(G):
    H=G.copy()
    data=H.nodes()
    c=0
    for x in data:
        if isEnglish(x)==False:
            print("droping",c)
            c=c+1
            G.remove_node(x)
    for x in G.edges():
        G[x[0]][x[1]]['weight']=float(G[x[0]][x[1]]['weight'])
    return G



def getnodes(s,n):
    selected=[]
    for node in range(0,n):
        selected.append(s[node][0])
    return selected

def genTree(G,selectednodes):
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=nx.shortest_path(G,x,y)


def extract(mst,selectednodes):
    H=nx.Graph()
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=nx.shortest_path(mst,selectednodes[x],selectednodes[y])
            for i in range(0,len(p)-1):
                H.add_node(p[i])
                H.add_edge(p[i], p[i+1])
    return H



def extract2(paths,selectednodes):
    H=nx.Graph()
    for x in  range(0, len(selectednodes)):
        for y in  range(x+1, len(selectednodes)):
            p=paths[selectednodes[x]][selectednodes[y]]
            for i in range(0,len(p)-1):
                H.add_node(p[i])
                #import pdb; pdb.set_trace()
                H.add_edge(p[i], p[i+1])
    return H






G=nx.Graph(pgv.AGraph(filepath))
G=fixEdgeWeightAndRemoveNonEngNode(G)
H=nx.connected_component_subgraphs(G)
G=list(H)[0]


#mst=nx.minimum_spanning_tree(G)
mst=nx.maximum_spanning_tree(G)
paths=nx.shortest_path(mst)
#c=nx.algorithms.degree_centrality(G)
c=nx.get_node_attributes(G,'weight')
c={k:int(v) for k, v in c.items()}

s=sorted(c.items(), key=lambda x: x[1], reverse=True)
T=nx.Graph()
'''
for i in range(0, len(nodecountinlevels)):
    gn=nx.Graph()

    nodes=getnodes(s,int(nodecountinlevels[i] * len(G.nodes())))
    gn.add_nodes_from(nodes)
    print(len(nodes), " ", round(100* len(nodes)/len(G.nodes() )), "%")
    write_dot(gn,'Layer_'+str(i+1)+'_terminals.dot')

#    import pdb; pdb.set_trace()
'''

for i in range(0, len(nodecountinlevels)):
    selectednodes= list(set(list(T.nodes())+ getnodes(s,int(nodecountinlevels[i] * len(G.nodes())))))
    print(len(selectednodes))

    T=extract2(paths,selectednodes)
    print("Layer", i+1, "nodes:", len(T.nodes()))
    write_dot(T,output_dir+'Layer_'+str(i+1)+'.dot')

write_dot(G,output_dir+'graph_connected.dot')
