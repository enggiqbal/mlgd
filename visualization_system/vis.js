//see license.txt
import Ol from "ol";
import Map from "ol/Map.js";
import View from "ol/View.js";
import GeoJSON from "ol/format/GeoJSON.js";
import MultiPoint from "ol/geom/MultiPoint.js";
import VectorLayer from "ol/layer/Vector.js";
import VectorSource from "ol/source/Vector.js";
import Select from "ol/interaction/Select.js";
import { altKeyOnly, click, pointerMove } from "ol/events/condition.js";
import {
  Circle as CircleStyle,
  Fill,
  RegularShape,
  Stroke,
  Style,
} from "ol/style.js";
import Text from "ol/style/Text";
import { transform } from "ol/proj.js";
import Circle from "ol/geom/Circle";
import Feature from "ol/Feature.js";
import { Tile as TileLayer, VectorL } from "ol/layer.js";
import { OSM, Vector } from "ol/source.js";
import Overlay from "ol/Overlay";
import {
  defaults as defaultControls,
  FullScreen,
  LayerSwitcher,
  OverviewMap,
} from "ol/control.js";

import * as d3 from "d3";
import * as utils from "./utils";

import "./style.css";

//data
// import clusterData from './geojson/mingwei_topics/im_cluster.geojson';
// import clusterBoundaryData from './geojson/mingwei_topics/im_cluster_boundary.geojson';
// import edgeData from './geojson/mingwei_topics/im_edges.geojson';
// import nodeData from './geojson/mingwei_topics/im_nodes.geojson';

// import clusterData from './geojson/mingwei-topics-refined/im_cluster.geojson';
// import clusterBoundaryData from './geojson/mingwei-topics-refined/im_cluster_boundary.geojson';
// import edgeData from './geojson/mingwei-topics-refined/im_edges.geojson';
// import nodeData from './geojson/mingwei-topics-refined/im_nodes.geojson';

// import clusterData from './geojson/mingwei-lastfm-refined/im_cluster.geojson';
// import clusterBoundaryData from './geojson/mingwei-lastfm-refined/im_cluster_boundary.geojson';
// import edgeData from './geojson/mingwei-lastfm-refined/im_edges.geojson';
// import nodeData from './geojson/mingwei-lastfm-refined/im_nodes.geojson';

// import clusterData from './geojson/reyan-topics-refined/im_cluster.geojson';
// import clusterBoundaryData from './geojson/reyan-topics-refined/im_cluster_boundary.geojson';
// import edgeData from './geojson/reyan-topics-refined/im_edges.geojson';
// import nodeData from './geojson/reyan-topics-refined/im_nodes.geojson';

// import clusterData from './geojson/reyan-lastfm-refined/im_cluster.geojson';
// import clusterBoundaryData from './geojson/reyan-lastfm-refined/im_cluster_boundary.geojson';
// import edgeData from './geojson/reyan-lastfm-refined/im_edges.geojson';
// import nodeData from './geojson/reyan-lastfm-refined/im_nodes.geojson';

//consts
const FONT = "arial";
// const [maxFont, minFont] = [16,12];
const [maxFont, minFont] = [14, 14];
const [maxEdgeWidth, minEdgeWidth] = [3, 0.5];
//globals
let sl, se;
let searched;
let graphMinResolution;

function clusterStyleFunction(feature, resolution) {
  let clusterStyle = new Style({
    stroke: new Stroke({
      color: feature.get("stroke"),
      width: 1,
    }),
    fill: new Fill({
      color: feature.get("fill"),
    }),
  });
  return clusterStyle;
}

function clusterBoundaryStyleFunction(feature, resolution) {
  let clusterStyle = new Style({
    stroke: new Stroke({
      color: feature.get("stroke"),
      width: 0,
    }),
    fill: new Fill({
      color: feature.get("fill"),
    }),
  });
  return clusterStyle;
}

let ef = false;
function edgeStyleFunction(feature, resolution) {
  if (!ef) {
    console.log(feature);
    ef = true;
  }

  if (feature.get("level") !== undefined) { //edges of the tree
    if (getVisible(feature, resolution)) {
      return new Style({
        stroke: new Stroke({
          color: "#aaaaaa",
          width: se(+feature.get("level")),
        }),
      });
    } else {
      return new Style({
        stroke: new Stroke({
          color: "#bbbbbb77",
          width: 0.5,
        }),
      });
    }
  } else { //other edges of the graph
    // if(true){
    // if(resolution < graphMinResolution){
    //   return new Style({
    //     stroke: new Stroke({
    //       // color: '#aaaaff20',
    //       color: '#aaaaff77',
    //       width: 1,
    //       // lineDash: [1, 1],
    //     })
    //   });
    // }else{
    return new Style({});
    // }
  }
}

function nodeStyleFunction(feature, resolution) {
  // console.log(feature.getGeometry());
  if (getVisible(feature, resolution)) {
    return new Style({
      image: new RegularShape({
        stroke: new Stroke({
          color: "rgba(0,0,0,0.5)",
          width: 1,
        }),
        fill: new Fill({
          color: "rgba(255,255,255,0.5)",
        }),
        radius: 10 / Math.SQRT2,
        radius2: 10,
        points: 4,
        angle: 0,
        scale: 0.5,
      }),
      text: createTextStyle(feature, resolution)
    });
  } else {
    return new Style({
      // image: new CircleStyle({
      //   fill: new Fill({
      //     color: '#aaa'
      //   }),
      //   stroke: new Stroke({
      //     color: '#3399CC',
      //     width: 0
      //   }),
      //   radius: sl(+feature.get('level'))/20
      // }),
    });
  }
}

// function selectStyleFunction(feature, resolution) {
//   let nodestyle = new Style({
//     stroke: new Stroke({
//       color: 'rgba(0,0,0,0.5)',
//       width: 1
//     }),
//     fill: new Fill({
//       color: 'rgba(255,255,255,0.5)'
//     })
//   });
// };

function selectStyleFunctionForNode(feature, resolution) {
  // let style = nodeStyleFunction(feature, resolution);
  // if (style.getFill()){
  //   style.setFill( new Fill({
  //     color: 'rgba(255,255,255,1)'
  //   }));
  //   style.setStroke( new Stroke({
  //     color: 'rgba(255,0,0,0.5)',
  //     width: 2
  //   }))
  // }
  // return style;
  return new Style({
    text: createTextStyle(feature, resolution, true, true),
  });
}

function selectStyleFunctionForEdge(feature, resolution) {
  let style = edgeStyleFunction(feature, resolution);
  let stk = style.getStroke();
  if (stk) {
    stk.width_ = stk.width_ + 2;
    stk.color_ = "red";
    style.setStroke(stk);
  }
  //console.log(stk)
  return style;
}

function getVisible(feature, resolution) {
  return feature.get("resolution") > resolution;
}

function createTextStyle(
  feature,
  resolution,
  fullText = false,
  select = false,
) {
  let fontsize = sl(+feature.get("level"));
  return new Text({
    font: `${fontsize}px ${FONT}`,
    text: fullText ? feature.get("label-full") : feature.get("label"),
    fill: new Fill({
      color: (searched == feature || select)
        ? "rgba(72,79,255,1)"
        : "rgba(72,79,90,1)",
    }),
    stroke: new Stroke({
      color: "rgba(250,250,250,1)",
      width: 2,
    }),
    offsetX: 0,
    offsetY: 0, //boxheight/2,
  });
}

function focus(map, nodeFeature, maxResolution) {
  let center = nodeFeature.values_.geometry.flatCoordinates;
  let resolution = Math.max(0.01, nodeFeature.get("resolution") - 0.01);
  resolution = Math.min(maxResolution, resolution);
  console.log(resolution);
  // let zoom = map.getView().getZoom() + 1;
  map.getView().animate({
    resolution,
    center,
    duration: 750,
  });
}

function initSearchBar(map, features) {
  // <input type="text" placeholder="Search..">
  let barDiv = d3.select("body")
    .append("div")
    .style("position", "absolute")
    .style("top", "1em")
    .style("right", "2em")
    .style("width", "12em");

  let bar = barDiv
    .append("input")
    .attr("type", "text")
    .attr("placeholder", "Search..")
    .style("width", "12em");
  let barWidth = `${bar.node().offsetWidth}px`;

  let icon = barDiv
    .append("i")
    .attr("class", "fa fa-search")
    .style("margin-left", "-1.3em");

  let list = barDiv
    .append("ul")
    .style("padding", "0")
    .style("max-height", "60vh")
    .style("max-width", barWidth)
    .style("overflow", "auto")
    .style("list-style-type", "none");

  let maxResolution = d3.max(features, (d) => d.get("resolution"));
  bar.node().addEventListener("keyup", (e) => {
    let query = bar.node().value.toLowerCase();
    if (query.length == 0) {
      searched = undefined;
      list.selectAll("li").remove();
    } else {
      let hits = features.filter((d) =>
        d.get("label-full").toLowerCase().includes(query)
      );
      list.selectAll("li").remove();
      let items = list.selectAll("li")
        .data(hits)
        .enter()
        .append("li")
        .text((d) => d.get("label-full"));
      items.on("click", function () {
        searched = d3.select(this).datum();
        focus(map, searched, maxResolution / 20);
      });
    }
  });
}

export function draw(
  clusterData,
  clusterBoundaryData,
  edgeData,
  nodeData,
  center,
  resolution,
  nodeZoomLevels,
) {
  let clusterSource = new Vector({ url: clusterData, format: new GeoJSON() });
  let clusterLayer = new VectorLayer({
    source: clusterSource,
    style: clusterStyleFunction,
  });

  let clusterBoundaySource = new Vector({
    url: clusterBoundaryData,
    format: new GeoJSON(),
  });
  let clusterBoundayLayer = new VectorLayer({
    source: clusterBoundaySource,
    style: clusterBoundaryStyleFunction,
  });

  let nodeFeatures;
  // let nodeSource = new Vector({  url: nodeData,  format: new GeoJSON()});
  let nodeSource = new Vector({
    format: new GeoJSON(),
    loader: function (extent, resolution, projection, callee) {
      let url = nodeData;
      let xhr = new XMLHttpRequest();
      xhr.open("GET", url);
      xhr.onerror = function () {
        nodeSource.removeLoadedExtent(extent);
      };
      xhr.onload = function () {
        // const minResolution = 1.194328566955879;
        // const maxResolution = 405.7481131407050;
        const minResolution = map.getView().minResolution_;
        const maxResolution = map.getView().maxResolution_;
        console.log(minResolution, maxResolution);

        if (xhr.status == 200) {
          nodeFeatures = nodeSource.getFormat().readFeatures(xhr.responseText);
          global.nodeFeatures = nodeFeatures;
          let maxLevel = d3.max(nodeFeatures, (d) => +d.get("level"));
          sl = d3.scaleLinear().domain([1, maxLevel]).range([maxFont, minFont]);
          se = d3.scaleLinear().domain([1, maxLevel]).range([
            maxEdgeWidth,
            minEdgeWidth,
          ]);

          let trunc = 16;
          nodeFeatures.forEach((d) => {
            let l = d.get("label");
            d.set("label", l.slice(0, trunc));
            d.set("label-full", l);
          });

          if (nodeZoomLevels !== undefined) {
            //load precomputed resolutions
            nodeFeatures.forEach((d, i) => {
              d.set("resolution", nodeZoomLevels.resolutions[i]);
            });
          } else {
            utils.markBoundingBox(nodeFeatures, sl, FONT);
            utils.markNonOverlapResolution(
              nodeFeatures,
              undefined,
              minResolution,
              maxResolution,
            );
            // download resolutions as a json
            // let resolutions = nodeFeatures.map(d=>d.get('resolution'));
            // utils.exportJson({resolutions:resolutions}, 'node_zoom_levels.json');
          }

          graphMinResolution = d3.min(nodeFeatures, (d) => d.get("resolution"));
          graphMinResolution = Math.max(
            graphMinResolution,
            map.getView().minResolution_,
          );
          nodeSource.addFeatures(nodeFeatures);
        } else {
          onError();
        }
      };
      xhr.send();
    },
  });
  let nodesLayer = new VectorLayer({
    source: nodeSource,
    style: nodeStyleFunction,
  });

  let edgeFeatures;
  let edgeSource = new Vector({
    // url: edgeData,
    format: new GeoJSON(),
    loader: function (extent, resolution, projection, callee) {
      let url = edgeData;
      let xhr = new XMLHttpRequest();
      xhr.open("GET", url);
      xhr.onerror = function () {
        edgeSource.removeLoadedExtent(extent);
      };
      xhr.onload = function () {
        if (xhr.status == 200) {
          edgeFeatures = edgeSource.getFormat().readFeatures(xhr.responseText);

          let intervalId = setInterval(() => { // use asynchronous data 'nodeFeatures'
            if (nodeFeatures !== undefined) {
              let level2resolution = {};
              nodeFeatures.forEach((d) => {
                let l = +d.get("level");
                let r = d.get("resolution");
                if (level2resolution[l] === undefined) {
                  level2resolution[l] = r;
                } else {
                  level2resolution[l] = Math.max(r, level2resolution[l]);
                }
              });
              //force level2resolution to be monochronically decreasing
              let levels = Object.keys(level2resolution).sort((a, b) => b - a);
              let currentResolution = level2resolution[levels[0]];
              for (let l of levels.slice(1)) {
                level2resolution[l] = Math.max(
                  currentResolution,
                  level2resolution[l],
                );
                currentResolution = level2resolution[l];
              }
              edgeFeatures.forEach((d) => {
                if (d.get("level") !== undefined) {
                  d.set("level", +d.get("level"));
                  d.set("resolution", level2resolution[d.get("level")]);
                }
              });
              edgeSource.addFeatures(edgeFeatures);
              clearInterval(intervalId);
            }
          }, 100);
        } else {
          onError();
        }
      };
      xhr.send();
    },
  });
  let edgesLayer = new VectorLayer({
    source: edgeSource,
    style: edgeStyleFunction,
  });

  //let geolayer = new TileLayer({  source: new OSM()});
  // ClusterLayer,clusterBoundayLayer,
  let map = new Map({
    controls: defaultControls().extend([
      new OverviewMap({
        layers: [clusterLayer, clusterBoundayLayer],
        view: new View({
          center: center || [0, 0],
          resolution: resolution || 5,
          maxResolution: 500,
          minResolution: 100,
        }),
      }),
    ]),
    layers: [clusterLayer, clusterBoundayLayer, edgesLayer, nodesLayer],
    target: "map",
    view: new View({
      // center: [0, 0],
      // minResolution,
      // maxResolution,
      // zoom: 11,
      // maxZoom: 17,
      // minZoom: 9,

      //for topics large
      center: center || [0, 0],
      resolution: resolution || 50,
    }),
  });

  let intervalId = setInterval(() => {
    if (nodeFeatures !== undefined) {
      initSearchBar(map, nodeFeatures);
      clearInterval(intervalId);
    }
  }, 100);

  global.map = map;

  let popup = new Overlay({
    element: document.getElementById("popup"),
  });
  map.addOverlay(popup);

  global.popup = popup;

  let nodeSelectPointerMove = new Select({
    condition: pointerMove,
    layers: [nodesLayer],
    style: selectStyleFunctionForNode,
  });
  map.addInteraction(nodeSelectPointerMove);

  // let edgeSelectPointerMove = new Select({
  //   condition: pointerMove,
  //   layers: [edgesLayer],
  //   style:selectStyleFunctionForEdge
  // });
  // map.addInteraction(edgeSelectPointerMove);

  map.on("click", function (evt) {
    let element = popup.getElement();
    $(element).popover("destroy");
    let feature = map.forEachFeatureAtPixel(
      evt.pixel,
      (feature, layer) => feature,
    );
    if (feature) {
      let element = popup.getElement();
      let geometry = feature.getGeometry();
      let fid = feature.getId();
      let ftype = feature.getGeometry().getType();

      if (fid && fid.search("cluster") > -1) {
        return 0;
      }

      if (ftype == "LineString") {
        return 0;
      }

      let label = feature.get("label-full");
      let level = feature.get("level");
      $(element)[0].title = label;
      let content = `
        Level: ${level}<br>
      `;

      $(element).popover("destroy");
      popup.setPosition(evt.coordinate);
      $(element).popover({
        placement: "top",
        animation: true,
        html: true,
        content: content,
      });
      $(element).popover("show");
    }
  });
}
