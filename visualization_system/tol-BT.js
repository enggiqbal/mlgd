import clusterData from './geojson/tol-BT/im_cluster.geojson';
import clusterBoundaryData from './geojson/tol-BT/im_cluster_boundary.geojson';
import edgeData from './geojson/tol-BT/im_alledges.geojson';
import nodeData from './geojson/tol-BT/im_nodes.geojson';
import nodeZoomLevels from './geojson/tol-BT/node_zoom_levels.json';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,-1000], 15, nodeZoomLevels);
