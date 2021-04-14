import clusterData from './geojson/lastfm-BT/im_cluster.geojson';
import clusterBoundaryData from './geojson/lastfm-BT/im_cluster_boundary.geojson';
import edgeData from './geojson/lastfm-BT/im_alledges.geojson';
import nodeData from './geojson/lastfm-BT/im_nodes.geojson';
import nodeZoomLevels from './geojson/lastfm-BT/node_zoom_levels.json';


import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 20, nodeZoomLevels);
