import clusterData from './geojson/reyan-topics-refined/im_cluster.geojson';
import clusterBoundaryData from './geojson/reyan-topics-refined/im_cluster_boundary.geojson';
import edgeData from './geojson/reyan-topics-refined/im_alledges.geojson';
import nodeData from './geojson/reyan-topics-refined/im_nodes.geojson';
import nodeZoomLevels from './geojson/reyan-topics-refined/node_zoom_levels.json';

// import clusterData from './geojson/topics-DELG/im_cluster.geojson';
// import clusterBoundaryData from './geojson/topics-DELG/im_cluster_boundary.geojson';
// import edgeData from './geojson/topics-DELG/im_alledges.geojson';
// import nodeData from './geojson/topics-DELG/im_nodes.geojson';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 40, nodeZoomLevels);
