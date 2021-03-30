import clusterData from './geojson/reyan-topics-refined/im_cluster.geojson';
import clusterBoundaryData from './geojson/reyan-topics-refined/im_cluster_boundary.geojson';
import edgeData from './geojson/reyan-topics-refined/im_alledges.geojson';
import nodeData from './geojson/reyan-topics-refined/im_nodes.geojson';

// import clusterData from './geojson/topics-DELG/im_cluster.geojson';
// import clusterBoundaryData from './geojson/topics-DELG/im_cluster_boundary.geojson';
// import edgeData from './geojson/topics-DELG/im_alledges.geojson';
// import nodeData from './geojson/topics-DELG/im_nodes.geojson';


import {draw} from './vis';

//main
// let center = [0,0];
// let resolution = 4;
draw(clusterData, clusterBoundaryData, edgeData, nodeData);//, center, resolution);
