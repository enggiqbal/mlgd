import clusterData from './geojson/tol-DELG/im_cluster.geojson';
import clusterBoundaryData from './geojson/tol-DELG/im_cluster_boundary.geojson';
import edgeData from './geojson/tol-DELG/im_alledges.geojson';
import nodeData from './geojson/tol-DELG/im_nodes.geojson';
import nodeZoomLevels from './geojson/tol-DELG/node_zoom_levels.json';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 100, nodeZoomLevels);
