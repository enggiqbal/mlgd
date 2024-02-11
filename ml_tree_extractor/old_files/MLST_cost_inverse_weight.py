import sys, os
import networkx as nx
import pygraphviz as pgv
import matplotlib.pyplot as plt
import numpy as np

def edges_sum(T,G):
    """
    takes a tree and find cost of edges
    :param T: tree
    :return cost: sum of cost of edges
    """
    s=0
    for x in T.edges():
        s=s+1/float( G[x[0]][x[1]]['weight'])
        #s=s+float( T[x[0]][x[1]]['weight'])
    return s

def main(folderpath,fileformat):
    """
    calculates cost of a set of trees (dot file with edge attribute: weight)
    :param folderpath: folder location of trees
    :param fileformat: format of file name
    :return MLST cost
    """
    cost=0
    costs=[]
    nodes_count=[]
    G=nx.Graph(pgv.AGraph('/Users/iqbal/MLGD/mlgd/pipeline/ml_tree_extractor/topics/Topics_Graph_Connected.dot'))
    for i in range(0,8):

        t=nx.Graph(pgv.AGraph(folderpath+fileformat.format(i+1)))
        c=edges_sum(t,G)
        cost=cost+c
        costs.append( c)
        nodes_count.append(len(t.nodes()))
        con= "Connected" if nx.is_connected(t)==True else "Disconnected"
        print("tree" , i+1, ": nodes: ", len(t.nodes()) ,"edges: ", len(t.edges()), " cost:", c , con )



    x = np.arange(0,8)
    plt.plot(x, costs, 'o-')
    plt.plot(x, nodes_count, 'o-')
    for i,j in zip(x,costs):
        plt.annotate( str(int(j)) ,xy=(i,j+10))
    for i,j in zip(x,nodes_count):
        plt.annotate( str(int(j))+ " (" + str(int(100* int(j)/nodes_count[7])) + "%)" ,xy=(i+0.1,j+5))

    plt.xlabel('levels')
    plt.ylabel('costs or node count')
    plt.legend(['costs (total '+ str(int(cost)) +')', 'node count'], loc='upper left')
    plt.show()





    return cost

if __name__=="__main__":
    folderpath="/Users/iqbal/Desktop/top_down/"
    folderpath="/Users/iqbal/Desktop/remlst/bottom_up/"


    fileformat="Composite_layer_{0}.dot"
    fileformat="Top_down_layer_{0}.dot"
    fileformat="Bottom_up_layer_{0}.dot"
    folderpath=""
    fileformat="Layer_{0}_topics_v2.dot"
    cost=main(folderpath,fileformat)
    print("Total Cost", cost)
