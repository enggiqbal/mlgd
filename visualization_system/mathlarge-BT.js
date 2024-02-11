import clusterData from './geojson/mathlarge-BT/im_cluster.geojson';
import clusterBoundaryData from './geojson/mathlarge-BT/im_cluster_boundary.geojson';
import edgeData from './geojson/mathlarge-BT/im_alledges.geojson';
import nodeData from './geojson/mathlarge-BT/im_nodes.geojson';
import nodeZoomLevels from './geojson/mathlarge-BT/node_zoom_levels.json';
// let nodeZoomLevels = undefined;

import {draw} from './vis';

//main
let center = [0,0];
let resolution = 300;
draw(clusterData, clusterBoundaryData, edgeData, nodeData, center, resolution, nodeZoomLevels);
