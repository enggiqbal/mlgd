import clusterData from './geojson/topicslarge-BT/im_cluster.geojson';
import clusterBoundaryData from './geojson/topicslarge-BT/im_cluster_boundary.geojson';
import edgeData from './geojson/topicslarge-BT/im_alledges.geojson';
import nodeData from './geojson/topicslarge-BT/im_nodes.geojson';


import {draw} from './vis';

//main
// let center = [0,0];
// let resolution = 4;
draw(clusterData, clusterBoundaryData, edgeData, nodeData);//, center, resolution);
