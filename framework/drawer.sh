#!/bin/bash

# Author Felice De Luca
# https://github.com/felicedeluca


# File Names
layerprefix="Topics_Layer_"
forestsuffix="_forest"
drawingsuffix="_drawing"
impredsuffix="_improved"
complete_graph_inch_path="$data_main_folder/Topics_Graph_inch.dot"
complete_graph_pixel_path="$data_main_folder/Topics_Graph_pixel.dot"

# Path
data_main_folder="tmp_workspace/topics/inloopoverlap"
complete_graph_path="$data_main_folder/Topics_Graph.dot"
forestsfolder="$data_main_folder/forests"
drawingfolder="$data_main_folder/layers"
finalfolder="$data_main_folder/final/"


# Modules
addsubcomponent_scriptfile="modules/augment/addsubcomponentmodule.py"
property_fetcher_scriptfile="modules/propertyfetcher.py"
impred_jar="modules/improve/ImPred.jar"
impredoverlapremoval_jar="modules/improve/ImPredoverlapremoval.jar"


# Settings
numberoflayers=8
edgeattraction=10
nodenoderepulsion=20
edgenoderepulsion=10
iterations=190


# Apply impred to first layer
i=1
lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"


python3 $property_fetcher_scriptfile $complete_graph_inch_path $lo


java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li


python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li

python3 $property_fetcher_scriptfile $complete_graph_inch_path $li


for i in {2..8}
do

  prev=$(( ${i}-1 ))

  lprev="$drawingfolder/${layerprefix}$(( ${i}-1 ))${drawingsuffix}${impredsuffix}.dot"
  lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
  le="$drawingfolder/${layerprefix}${i}${drawingsuffix}_edges.dot"
  li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"
  fl="$forestsfolder/${layerprefix}${i}${forestsuffix}.dot"

  python3 $addsubcomponent_scriptfile $lprev $fl $lo

  java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li

    python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

    java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li


    python3 $property_fetcher_scriptfile $complete_graph_inch_path $li

done


python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li


python3 $property_fetcher_scriptfile $complete_graph_inch_path $li

python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

