import clusterData from 'url:./geojson/reyan-lastfm-refined/im_cluster.geojson';
import clusterBoundaryData from 'url:./geojson/reyan-lastfm-refined/im_cluster_boundary.geojson';
import edgeData from 'url:./geojson/reyan-lastfm-refined/im_alledges.geojson';
import nodeData from 'url:./geojson/reyan-lastfm-refined/im_nodes.geojson';
import nodeZoomLevels from './geojson/reyan-lastfm-refined/node_zoom_levels.json';

// import clusterData from './geojson/lastfm-DELG/im_cluster.geojson';
// import clusterBoundaryData from './geojson/lastfm-DELG/im_cluster_boundary.geojson';
// import edgeData from './geojson/lastfm-DELG/im_alledges.geojson';
// import nodeData from './geojson/lastfm-DELG/im_nodes.geojson';



import {draw} from './vis';

//main
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 50, nodeZoomLevels);