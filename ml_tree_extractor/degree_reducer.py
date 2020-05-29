import networkx as nx
import pygraphviz as pgv
import argparse
from networkx.drawing.nx_agraph import write_dot

 
def graph_reducer(G):
    node_frequency = nx.get_node_attributes(G, 'weight')
    total = sum(int(node_frequency[k]) for k in node_frequency.keys())
    # updating edge weight based on ZTest
    edges = list(G.edges())
    for e in edges:
        topicFreq_i = int(G.nodes[e[0]]["weight"])
        topicProb_j = int(G.nodes[e[1]]["weight"]) / total
        expected = topicFreq_i * topicProb_j
        ZTest = (int(G.edges[e]["weight"]) - expected)/(expected**0.5)
        if ZTest < 50:
            G.remove_edge(*e)
        else:
            G.edges[e]["weight"] = ZTest
    #taking only max connected component
    Gc = max(nx.connected_components(G), key=len)
    G = nx.subgraph(G, Gc)
    return G

def degree_distribution(G,name):
    import collections
    import matplotlib.pyplot as plt
    degree_sequence = sorted([d for n, d in G.degree()], reverse=True) 
    degreeCount = collections.Counter(degree_sequence)
    deg, cnt = zip(*degreeCount.items())
    plt.clf()
    fig, ax = plt.subplots()
    plt.bar(deg, cnt, width=0.80, color='b')
    plt.title("Degree Histogram")
    plt.ylabel("Count")
    plt.xlabel("Degree")
    ax.set_xticks([d + 0.4 for d in deg])
    ax.set_xticklabels(deg)
    plt.savefig(name+".png")
    



    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='graph reducer')

    parser.add_argument('-i', '--input_dot', default='input',
                        help='input dot path', required=True)

    parser.add_argument('-o', '--output_dot', default='outputs',
                        help='Output dot path', required=True)
    args = parser.parse_args()
    G = nx.Graph(pgv.AGraph(args.input_dot))
    #degree_distribution(G,"old")
    print("old max degree:", max([d for n, d in G.degree()]))
    G=graph_reducer(G)
    print("new max degree:", max([d for n, d in G.degree()]))
    write_dot(G,args.output_dot)
    #degree_distribution(G,"new")
 