We provide some function to measure the quality of the drawing.

One measure is area coverage, which is implemented in area_coverage.js. We need to provide a coordinate as input:

my_coords = [{x:0, y:0}, {x:1000, y:0}, {x:1000, y:1000}, {x:0, y:1000}];
area_coverage(coordinates);

Another measure is the ideal edge lenghth preservation, which is implemented at ideal_edge_lenghth_preservation.js. Here, we need to provide the coordinates of the edges/links and the desired edge lengths as input:

links = [{source:{x:0, y:0}, target:{x:101, y:0}}, {source:{x:101, y:0}, target:{x:200, y:0}}, {source:{x:200, y:0}, target:{x:410, y:0}}];
ideal_lengths = [100, 100, 200];
ideal_edge_length_preservation(links, ideal_lengths);

