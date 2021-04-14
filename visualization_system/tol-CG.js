import clusterData from './geojson/tol-CG/im_cluster.geojson';
import clusterBoundaryData from './geojson/tol-CG/im_cluster_boundary.geojson';
import edgeData from './geojson/tol-CG/im_alledges.geojson';
import nodeData from './geojson/tol-CG/im_nodes.geojson';
import nodeZoomLevels from './geojson/tol-CG/node_zoom_levels.json';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 40, nodeZoomLevels);
