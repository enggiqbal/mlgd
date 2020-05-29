The module extracts multi layers from a node-edge weighted graph. See examples: `data/datasets/topics/orginal/Topics_Graph_Connected.dot`

Dot file must contain `weight` and `label` attributes for nodes and a `weight` attribute for edges.

high degree reducer (only for node-edge weighted graph)
```
python3 degree_reducer.py -i ../data/datasets/topics/orginal/Topics_Graph_Connected.dot -o Topics_Graph_Reduce_Connected.dot
```

```

python3 mltree_generator_weighted.py path_of_input_dot_file #for node weighted graph 
python3 fix_node_att_add_font_box.py
```

Output files are located in `outputs` directory.


Note: those script use `tmp` directory to store files from intermediate steps. If you want to debug, do empty `tmp` directory.

## Authors
Iqbal Hossain, University of Arizona
See also the list of contributors who participated in this project.

## License
This project is licensed under the Apache License, Version 2.0 - see the license.txt file for details
