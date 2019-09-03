#!/bin/bash
#Author
#Felice De Luca
#https://github.com/felicedeluca


layerprefix="Topics_Layer_"
forestsuffix="_forest"
drawingsuffix="_drawing"
impredsuffix="_improved"


data_main_folder="tmp_workspace/topics/20190515_labels_pixels"

complete_graph_path="$data_main_folder/input/Topics_Graph.dot"

numberoflayers=8

forestsfolder="$data_main_folder/forests"
drawingfolder="$data_main_folder/layers"
finalfolder="$data_main_folder/final/"

metrics_output_file="$data_main_folder/metrics.csv"

addsubcomponent_scriptfile="modules/add_forest/addsubcomponentmodule.py"
planar_augmentation_scriptfile="modules/planar_augmentation/planar_augmentation.py"
property_fetcher_scriptfile="modules/planar_augmentation/propertyfetcher.py"
impred_jar="modules/drawing_improvement/ImPred.jar"
leaves_uniformer="modules/uniform_leaves_edges/uniform_leaves_edges_main.py"
impred_scriptfile="modules/drawing_improvement/impred.py"
extract_sublevel_scriptfile="modules/planar_augmentation/subgraph_extractor.py"
extract_subgraphs_scriptfile="modules/sublevel_extractor/sublevel_extractor.py"
scale_to_remove_overlap_scriptfile="modules/labelsoverlapremoval/labelsoverlapremoval.py"

metrics_computer_scriptfile="../../measurement/metricscomputer.py"


edgeattraction=10
nodenoderepulsion=20
edgenoderepulsion=10
iterations=190


# Apply impred to first layer
i=1
lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"

java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li #  2>&1 | tee -a output.txt


for i in {2..8}
do


  prev=$(( ${i}-1 ))

  lprev="$drawingfolder/${layerprefix}$(( ${i}-1 ))${drawingsuffix}${impredsuffix}.dot"
  lo="$drawingfolder/${layerprefix}${i}${drawingsuffix}.dot"
  le="$drawingfolder/${layerprefix}${i}${drawingsuffix}_edges.dot"
  li="$drawingfolder/${layerprefix}${i}${drawingsuffix}${impredsuffix}.dot"
  fl="$forestsfolder/${layerprefix}${i}${forestsuffix}.dot"

#echo "addsubcomponent, $lprev $fl $lo"
python3 $addsubcomponent_scriptfile $lprev $fl $lo # 2>&1 | tee -a output.txt

#echo "leaves_uniformer, $lo"
# python3 $leaves_uniformer $lo # 2>&1 | tee -a output.txt

# python3 $scale_to_remove_overlap_scriptfile $lo $lo

#echo "planar_augmentation, $le"
#python3 $planar_augmentation_scriptfile $lo  $complete_graph_path $le #  2>&1 | tee -a output.txt

#echo "impred, $lo $li"
java -jar $impred_jar --inputgraph=$lo --edgeattraction=$edgeattraction --nodenoderepulsion=$nodenoderepulsion --edgenoderepulsion=$edgenoderepulsion --iterations=$iterations --outputfile=$li #  2>&1 | tee -a output.txt


python3 $planar_augmentation_scriptfile $complete_graph_path $li
# python3 $scale_to_remove_overlap_scriptfile $li $li
# python3 $extract_sublevel_scriptfile $li $lo $li #  2>&1 | tee -a output.txt

# python3 $metrics_computer_scriptfile $li $metrics_output_file #  2>&1 | tee -a output.txt

# python3 $impred_scriptfile $li #  2>&1 | tee -a output.txt

# python3 $metrics_computer_scriptfile $li #  2>&1 | tee -a output.txt

# python3 $leaves_uniformer $li #  2>&1 | tee -a output.txt

#python3 $metrics_computer_scriptfile $li $metrics_output_file #  2>&1 | tee -a output.txt

done

# python3 $extract_subgraphs_scriptfile $li8 $finalfolder #  2>&1 | tee -a output.txt
