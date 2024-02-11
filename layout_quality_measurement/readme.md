# Author
# Felice De Luca
# https://github.com/felicedeluca

# Info
For the latest version of the measurement project for graph drawing refer to: 
https://github.com/felicedeluca/graphmetrics


# Intro
Compute the most popular aestethic criteria for a graph drawing.

The input format is a DOT file (see https://en.wikipedia.org/wiki/DOT_(graph_description_language) for more information). To covert a graph into a DOT file please check some graph converters in 'graphconverter' folder or use another tool like NetworkX to read and write a dot file.

This project uses: .

* networkx (https://networkx.github.io) to handle graphs and reading/writing files
* pygraphviz (https://pygraphviz.github.io) to manage the DOT format
* numpy: for inner product of upward drawings

The project is tested on python3

# Computed Metrics
* Crossings
* Edge length uniformity
* Bounding box
* Aspect ratio
* Stress
* Neighbors preservation

_Labels metrics_
* Labels total area
* Labels total area to boundin box ratio


##### Crossings
Counts the number of edge crossings in the given layout. The technique checkes the
intersection between any pair of edges, and returns the crossings number.

The algorithm should be improved using a sweep-line technique.

##### Edge length uniformity

The Edge length uniformity corresponds to the normalized standard deviation of the edge length, i.e.:

$$EU = \sqrt{\sum_{e \in E}\frac{(l_e - l_{avg})^2}{|E|l^2_{avg}}}$$

##### Bounding box
The bounding box computes the width and height of the given layout.


##### Aspect Ratio
The aspect ratio returns width/heigth of the layout



##### Stress
The stress is a well known measure for the energy of the layout of a graph drawing.
It computes the difference betewwn the graph thoretic distance and the gemetric distance
between any pair of vertices of the given graph.

Since scaling a given layout changes the computed stress value, the computed value
is the minimum value achievable fixing the layout, that is, the drawing is scaled before
computing the stress measurement.

$$ ST = \sum_{i,j \in V} w_{ij}(\parallel p_i - p_j \parallel - d_{ij})^2$$





##### Neighbors preservation
The neighbors preservation checks for each vertex how many neighbors are close to it
in the given layout. This value is the range $[0, 1]$ such that 0 means that the adjacecies
of the vertices are not respected in the layout while 1 means that all the neighbors of a vertex are close to it in the layout.



# Usage

    python3 metricscomputer.py {input_graph_path.dot} {desired_metrics_comma_separated} {input_metrics_path.csv}

{input_graph_path.dot} : full path of the input graph

{desired_metrics_comma_separated} :
  possible values are  

    cr        : for crossings;
    ue        : for edge length uniformity;
    st        : for stress;
    np        : for neighbors_preservation;
    lblbb     : for label to boundingBox ratio;
    lblarea   : for labels total area;
    bb        : for bounding box;
    upflow    : for upward flow;

example

*"cr,st,ue,np"* for crossings, stress, edge length uniformity and neighbors preservation

{input_metrics_path.csv} : full path for the output file with computed metrics. The file is created if does not exist

### Future improvements (needed)
Currently the crossing counting is rather slow, it checks each pair of edges. A better way to count the crossings, such as a sweep-line algorithm, would speed-up the computation.
