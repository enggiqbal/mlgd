# Author
# Felice De Luca
# https://www.github.com/felicedeluca


import networkx as nx
import math

import vertexmanager

def monotone_draw(G, root, edge_length):
  """ take tree
  assign unique slope
  use tan-1 for slopes
  if path, may consider same slop
  run DFS
  """

  i = 0 # starting with zero angle

  vertexmanager.setCoordinate(G.node[root], 0.0, 0.0)

  for e in nx.dfs_edges(G,root):
    u, v = e
    slp = math.atan(i)

    x_u, y_u = vertexmanager.getCoordinate(G.node[u])

    x_v = x_u + math.cos(slp)
    y_v = y_u + math.sin(slp)

    vertexmanager.setCoordinate(G.node[v], x_v+edge_length, y_v+edge_length)

    i = i + 1

  return G
