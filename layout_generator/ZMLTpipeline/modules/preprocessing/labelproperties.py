import sys
import os
import math

import pygraphviz as pgv
import networkx as nx
from networkx.drawing.nx_agraph import write_dot
from networkx.drawing.nx_agraph import read_dot as nx_read_dot

import tkinter
from tkinter import *

# def getBoxSize(txt, fs):
#
#     Window = Tk()
#     Window.geometry("500x500+80+80")
#     frame = Frame(Window) # this will hold the label
#     frame.pack(side = "top")
#     measure = Label(frame, font = ('Arial', fs), text = txt)
#     measure.grid(row = 0, column = 0) # put the label in
#     measure.update_idletasks() # this is VERY important, it makes python calculate the width
#     width = round(round(measure.winfo_width()/72,2)*72*1.10, 2) # get the width
#     height= round(round(measure.winfo_height()/72,2)*72*1.10, 2) # get the height
#
#     return height,width, fs




tkinter.Frame().destroy()  # Enough to initialize resources


# Main Flow
graph_path = sys.argv[1]
outputpath = graph_path

font_size = 12

if len(sys.argv)==3:
    font_size=int(sys.argv[2])

input_graph_name = os.path.basename(graph_path)
graph_name = input_graph_name.split(".")[1]

G = nx_read_dot(graph_path)
G=nx.Graph(G)

v_labels = nx.get_node_attributes(G, "label")
v_levels = nx.get_node_attributes(G, "level")
font_sizes=[30,25,20,15,12,10,9,8]
# use font size array

Window = Tk()
Window.geometry("500x500+80+80")
frame = Frame(Window) # this will hold the label
frame.pack(side = "top")


max_w = 0
max_h = 0


for v in v_labels.keys():

    v_label = v_labels[v]
    v_level = 0
    if  v in v_levels.keys():
        v_level = int(v_levels[v])-1

    font_size = font_sizes[v_level]

    # arial36b = tkFont.Font(family='Arial', size=font_size, weight='normal')
    #
    # width = arial36b.measure(v_label)
    # height = arial36b.metrics('linespace')
    # fs = 12
    fs = font_size

    measure = Label(frame, font = ('Arial', fs), text = v_label)
    measure.grid(row = 0, column = 0) # put the label in
    measure.update_idletasks() # this is VERY important, it makes python calculate the width
    width = round(round(measure.winfo_width() / 72, 2)  * 72 * 1.10, 2) # get the width
    height= round(round(measure.winfo_height()/72 ,2)   * 72 * 1.10, 2)

    width /= 72
    height /= 72
    #
    # max_h = max(max_h, height)
    # max_w = max(max_w, width)

    nx.set_node_attributes(G, {v:width}, "width")
    nx.set_node_attributes(G, {v:height}, "height")
    nx.set_node_attributes(G, {v:fs}, "fontsize")


# for v in v_labels.keys():
#     nx.set_node_attributes(G, {v:max_w}, "width")
#     nx.set_node_attributes(G, {v:max_h}, "height")
#     nx.set_node_attributes(G, {v:12}, "fontsize")

# print("assigning font size",font_size, "and width and height", max_w, max_h)

write_dot(G, outputpath)
