#!/bin/bash

# Bash script to run ZMLT
# Author
# Felice De Luca
# github.com/felicedeluca


# Set the info of the graph to be drawn.
layerprefix="lastfm_"
data_main_folder="tmp_workspace/templatefolder"
complete_graph_path="$data_main_folder/lastfm_graph.dot"
complete_graph_inch_path="$data_main_folder/lastfm_graph.dot"
complete_graph_pixel_path="$data_main_folder/lastfm_graph.dot"
numberoflayers=8



# This is the default name format used in this script
forestsuffix="_forest"
drawingsuffix="_drawing"
impredsuffix="_improved"


# This is the default folder structure of the project
inputfolder="$data_main_folder/input"
forestsfolder="$data_main_folder/forests"
drawingfolder="$data_main_folder/layers"
finalfolder="$data_main_folder/final"

# Where to write the computed metrics.
metrics_output_file="$data_main_folder/metrics.csv"

# where to find the scripts files
addsubcomponent_scriptfile="modules/add_forest/addsubcomponentmodule.py"
planar_augmentation_scriptfile="modules/planar_augmentation/planar_augmentation.py"
property_fetcher_scriptfile="modules/propertyfetcher.py"
impred_jar="modules/drawing_improvement/ImPred.jar"
impredoverlapremoval_jar="modules/drawing_improvement/ImPredoverlapremoval.jar"

leaves_uniformer="modules/uniform_leaves_edges/uniform_leaves_edges_main.py"
impred_scriptfile="modules/drawing_improvement/impred.py"
extract_sublevel_scriptfile="modules/planar_augmentation/subgraph_extractor.py"
extract_subgraphs_scriptfile="modules/sublevel_extractor/sublevel_extractor.py"
scale_to_remove_overlap_scriptfile="modules/labelsoverlapremoval/labelsoverlapremoval.py"
leaves_shortener_scriptfile="modules/refinement/uniform_leaves_edges/uniform_leaves_edges_main.py"
remove_crossings_scriptfile="modules/add_forest/remove_crossings_main.py"
setup_boxsizes_scriptfile="modules/preprocessing/labelproperties.py"
extractforest_scriptfile="modules/preprocessing/forest_extractor.py"
scalegraph_scriptfile="modules/resizegraph.py"
remove_labels_scriptfile="modules/removelabelsize.py"

metrics_computer_scriptfile="../../measurement/metricscomputer.py"



edgeattraction=30
nodenoderepulsion=20
edgenoderepulsion=20
iterations=130
layers=4

font_sizes=(12 12 12 12 12 12 12 12)

# Setup the box sizes for the layer
python3 $setup_boxsizes_scriptfile $complete_graph_path ${font_sizes[1]}


for i in {1..7}
do

  next=$(( ${i}+1))

  tcurr="$inputfolder/${layerprefix}${i}.dot"
  tnext="$inputfolder/${layerprefix}${next}.dot"
  output_folder="${forestsfolder}/"

  python3 $extractforest_scriptfile $tcurr $tnext $output_folder

done


#Draw the first layer.
sfdp "$inputfolder/${layerprefix}1.dot" -Tdot > "$drawingfolder/${layerprefix}1${drawingsuffix}.dot"


# ## Apply impred to first layer
i=1
lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"

# Fetch the properties from the original graph.
# Some of them may go lost during the process.
python3 $property_fetcher_scriptfile $complete_graph_inch_path $lo

# Setup the box sizes for the layer
python3 $setup_boxsizes_scriptfile $complete_graph_path ${font_sizes[$i]}


# Remove the crossings of the input tree, if any.
python3 $remove_crossings_scriptfile $lo

# Fetch the properties from the original graph.
# Some of them may go lost during the process.
python3 $property_fetcher_scriptfile $complete_graph_inch_path $lo

python3 $remove_labels_scriptfile $lo

# Improve the drawing given at the first level.
java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li

# Fetch the properties from the original graph.
# Some of them may go lost during the process.
python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

# Remove the overlap of the labels
java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=0 --outputfile=$li



python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li
python3 $scalegraph_scriptfile $li $(( ${i}*4000 ))
python3 $setup_boxsizes_scriptfile $li 12
java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=70 --outputfile=$li
python3 $property_fetcher_scriptfile $complete_graph_inch_path $li

# Shorten Edges
#python3 $leaves_shortener_scriptfile $li

# Remove the overlap of the labels
#java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li


# Fetch the properties from the original graph.
# Some of them may go lost during the process.
python3 $property_fetcher_scriptfile $complete_graph_inch_path $li

topdownoutput="$finalfolder/${layerprefix}${i}_topdown.dot"
cp $li $topdownoutput
neato  -n2 $topdownoutput -Nshape=rectangle -Tpdf > "${topdownoutput}.pdf"


for i in {2..8}
do

  prev=$(( ${i}-1 ))

  lprev="$drawingfolder/${layerprefix}$(( ${i}-1 ))${drawingsuffix}${impredsuffix}.dot"
  lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
  le="$drawingfolder/${layerprefix}${i}${drawingsuffix}_edges.dot"
  li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"
  fl="$forestsfolder/${layerprefix}${i}${forestsuffix}.dot"

  # Add the forest to the improved drawing of the previous level
  # To get the new level
  # This drawing will be planar
  python3 $addsubcomponent_scriptfile $lprev $fl $lo

  # Fetch the properties from the original graph.
  # Some of them may go lost during the process.
  python3 $property_fetcher_scriptfile $complete_graph_pixel_path $lo

  # Setup the box sizes for the layer
  python3 $setup_boxsizes_scriptfile $complete_graph_path ${font_sizes[${i}]}

  python3 $remove_labels_scriptfile $lo
  # Improve the drawing
  java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li
  # Fetch the properties from the original graph.
  # Some of them may go lost during the process.
  python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li

  # Remove the overlap of the labels
  java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=0 --outputfile=$li
  python3 $property_fetcher_scriptfile $complete_graph_inch_path $li
  # Shorten Edges
#  python3 $leaves_shortener_scriptfile $li

  # Remove the overlap of the labels
#  java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li

  # Fetch the properties from the original graph.
  # Some of them may go lost during the process.

  python3 $scalegraph_scriptfile $li $(( ${i}*4000 ))
  python3 $setup_boxsizes_scriptfile $li 12
  java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=70 --outputfile=$li
  python3 $property_fetcher_scriptfile $complete_graph_inch_path $li


  topdownoutput="$finalfolder/${layerprefix}${i}_topdown.dot"
  cp $li $topdownoutput
  neato  -n2 $topdownoutput -Nshape=rectangle -Tpdf > "${topdownoutput}.pdf"


done
#
# # Fetch the properties from the original graph.
# # Some of them may go lost during the process.
# python3 $property_fetcher_scriptfile $complete_graph_pixel_path $li
#
# # At this stage there shouldn't be any label overlap but we try it again.
# # Remove the overlap of the labels
# java -jar $impredoverlapremoval_jar --inputgraph=$li --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$li
#
# # Fetch the properties from the original graph.
# # Some of them may go lost during the process.
# python3 $property_fetcher_scriptfile $complete_graph_inch_path $li
#
#
#
# for i in {1..8}
# do
#
#   tcurr="$inputfolder/${layerprefix}${i}.dot"
#   tout="$finalfolder/${layerprefix}${i}.dot"
#
#   cp $tcurr $tout
#
#   python3 $property_fetcher_scriptfile  $li $tout "label,weight,fontsize,level,width,height,pos"
#
#   # Setup the box sizes for the layer
#   python3 $setup_boxsizes_scriptfile $complete_graph_path ${font_sizes[${i}]}
#
#   # Remove the overlap of the labels
#   java -jar $impredoverlapremoval_jar --inputgraph=$tout --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=$tout
#
#   neato  -n2 $tout -Nshape=rectangle -Tpdf > "${tout}.pdf"
#
#
# done
