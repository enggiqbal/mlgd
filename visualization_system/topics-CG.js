import clusterData from './geojson/mingwei-topics-refined/im_cluster.geojson';
import clusterBoundaryData from './geojson/mingwei-topics-refined/im_cluster_boundary.geojson';
import edgeData from './geojson/mingwei-topics-refined/im_alledges.geojson';
import nodeData from './geojson/mingwei-topics-refined/im_nodes.geojson';

// import clusterData from './geojson/topics-CG/im_cluster.geojson';
// import clusterBoundaryData from './geojson/topics-CG/im_cluster_boundary.geojson';
// import edgeData from './geojson/topics-CG/im_alledges.geojson';
// import nodeData from './geojson/topics-CG/im_nodes.geojson';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData);//, center, resolution);
