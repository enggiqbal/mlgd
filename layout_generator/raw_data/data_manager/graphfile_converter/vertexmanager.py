#Author
#Felice De Luca
#https://github.com/felicedeluca

import networkx as nx


def getCoordinate(vertex):

    x = float(vertex['pos'].split(",")[0])
    y = float(vertex['pos'].split(",")[1])

    return x, y


def setCoordinate(vertex, x, y):

    vertex['pos'] = str(x)+","+str(y)

    return x, y


def shiftVertex(vertex, dx, dy):

    x, y = getCoordinate(vertex)

    setCoordinate(vertex, x+dx, y+dy)

    return getCoordinate(vertex)
