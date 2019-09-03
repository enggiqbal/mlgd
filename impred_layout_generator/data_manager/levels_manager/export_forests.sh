#!/bin/bash

#Author
#Felice De Luca
#https://github.com/felicedeluca

input_layer_folder="../../tmp_workspace/topics/set1/input/" 
output_forest_folder="../../tmp_workspace/topics/set1/forests/"


python3 forest_extractor.py $input_layer_folder"Topics_Layer_1.dot" $input_layer_folder"Topics_Layer_2.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_2.dot" $input_layer_folder"Topics_Layer_3.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_3.dot" $input_layer_folder"Topics_Layer_4.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_4.dot" $input_layer_folder"Topics_Layer_5.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_5.dot" $input_layer_folder"Topics_Layer_6.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_6.dot" $input_layer_folder"Topics_Layer_7.dot" $output_forest_folder
python3 forest_extractor.py $input_layer_folder"Topics_Layer_7.dot" $input_layer_folder"Topics_Layer_8.dot" $output_forest_folder
