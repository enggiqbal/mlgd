import clusterData from './geojson/mingwei-lastfm-refined/im_cluster.geojson';
import clusterBoundaryData from './geojson/mingwei-lastfm-refined/im_cluster_boundary.geojson';
import edgeData from './geojson/mingwei-lastfm-refined/im_alledges.geojson';
import nodeData from './geojson/mingwei-lastfm-refined/im_nodes.geojson';
// import nodeZoomLevels from './geojson/mingwei-lastfm-refined/node_zoom_levels.json';
let nodeZoomLevels = undefined;
// import clusterData from './geojson/lastfm-CG/im_cluster.geojson';
// import clusterBoundaryData from './geojson/lastfm-CG/im_cluster_boundary.geojson';
// import edgeData from './geojson/lastfm-CG/im_alledges.geojson';
// import nodeData from './geojson/lastfm-CG/im_nodes.geojson';

import {draw} from './vis';

//main
console.log(nodeZoomLevels);
draw(clusterData, clusterBoundaryData, edgeData, nodeData, [0,0], 50, nodeZoomLevels);
