import clusterData from './geojson/topics-BT/im_cluster.geojson';
import clusterBoundaryData from './geojson/topics-BT/im_cluster_boundary.geojson';
import edgeData from './geojson/topics-BT/im_alledges.geojson';
import nodeData from './geojson/topics-BT/im_nodes.geojson';
import nodeZoomLevels from './geojson/topics-BT/node_zoom_levels.json';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 6, nodeZoomLevels);
