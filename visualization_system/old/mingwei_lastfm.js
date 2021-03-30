import clusterData from './geojson/mingwei-lastfm-refined/im_cluster.geojson';
import clusterBoundaryData from './geojson/mingwei-lastfm-refined/im_cluster_boundary.geojson';
import edgeData from './geojson/mingwei-lastfm-refined/im_alledges.geojson';
import nodeData from './geojson/mingwei-lastfm-refined/im_nodes.geojson';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData);
