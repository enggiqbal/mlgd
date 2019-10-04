//see license.txt
import Ol from 'ol'
import Map from 'ol/Map.js';
import View from 'ol/View.js';
import GeoJSON from 'ol/format/GeoJSON.js';
import MultiPoint from 'ol/geom/MultiPoint.js';
import VectorLayer from 'ol/layer/Vector.js';
import VectorSource from 'ol/source/Vector.js';
import Select from 'ol/interaction/Select.js';
import {click, pointerMove, altKeyOnly} from 'ol/events/condition.js';
import {  Circle as CircleStyle,  Fill,  Stroke,  Style} from 'ol/style.js';
import Text from 'ol/style/Text';
import {  transform } from 'ol/proj.js';
import Circle from 'ol/geom/Circle';
import Feature from 'ol/Feature.js';
import {Tile as TileLayer,  VectorL} from 'ol/layer.js';
import {OSM,Vector} from 'ol/source.js';
import Overlay from 'ol/Overlay';
import {  defaults as defaultControls,  OverviewMap,  LayerSwitcher, FullScreen} from 'ol/control.js';


//import data

import clusterData from './geojson/impred_lastfm/im_cluster.geojson'
import clusterBoundaryData from './geojson/impred_lastfm/im_cluster_boundary.geojson'
import edgeyData from './geojson/impred_lastfm/im_edges.geojson'
import nodeData from './geojson/impred_lastfm/im_nodes.geojson'

var clusterStyleFunction = function(feature, resolution) {
  var clusterStyle = new Style({
    stroke: new Stroke({  color:  feature.get("stroke"),  width: 1  }),
    fill: new Fill({ color: feature.get("fill")      })
  });
  return clusterStyle; };

var clusterBoundaryStyleFunction = function(feature, resolution) {
  var clusterStyle = new Style({  stroke: new Stroke({  color: feature.get("stroke"),  width: 1    }),
    fill: new Fill({  color: feature.get("fill")  })
  });
  return clusterStyle; };

var edgeStyleFunction = function(feature, resolution) {
  var l=feature.get("level")
  var w=5*l/resolution
  if (resolution<5 ) w=l/2;
  //else w=l/resolution
  w=(10-l)/2
  var edgeStyle = new Style({  stroke: new Stroke({      color: feature.get("stroke"),    width: w  })  });

  var empytStyle=new Style({});
  var stlye=empytStyle;
  if (getVisible(l,resolution))   stlye= edgeStyle;
  return stlye;
  };



var nodeStyleFunction = function(feature, resolution) {
  var nodestyle = new Style({  stroke: new Stroke({  color: 'rgba(0,0,0,0.5)',  width: 1  }),
    fill: new Fill({    color: 'rgba(255,255,255,0.5)'  }),
    text: createTextStyle(feature.get("label"), feature.get("fontsize"), feature.get("level"), feature.get("height"),feature.get("weight") ,resolution)
    });
  var stlye=new Style({});
  var l=parseInt(feature.get("level"))

  if (getVisible(l,resolution))  stlye= nodestyle;
  return stlye;
};

var selectStyleFunction=function(feature, resolution) {
  var nodestyle = new Style({
    stroke: new Stroke({    color: 'rgba(0,0,0,0.5)',    width: 1  }) ,
    fill: new Fill({    color: 'rgba(255,255,255,0.5)'  })
  });
};



var selectStyleFunctionForNode=function(feature, resolution) {

  var style=nodeStyleFunction(feature, resolution);
  if (style.getFill()){
  style.setFill(new Fill({    color: 'rgba(255,255,255,1)'  }));
  style.setStroke( new Stroke({ color: 'rgba(255,0,0,0.5)',  width: 2 }))
}
  return style;

};


var selectStyleFunctionForEdge=function(feature, resolution) {
  var style=edgeStyleFunction(feature, resolution);
 var stk=  style.getStroke()
 if (stk){
 stk.width_=stk.width_ + 2
 stk.color_="red"
 style.setStroke(stk);
 }
 //console.log(stk)
  return style;
};




function getVisible(l,resolution)
{
  var visiable=false
  if (l == 1)  visiable= true;
  if (l == 2 && resolution< 20) visiable= true;
  if (l == 3 && resolution< 15)  visiable= true;
  if (l == 4 && resolution< 10)   visiable= true;
  if (l == 5 && resolution< 8)  visiable= true;
  if (l == 6 && resolution< 6)  visiable= true;
  if (l == 7 && resolution< 5)  visiable= true;
  if (l == 8 && resolution< 4)  visiable= true;
  return visiable
}



var createTextStyle = function(lbl, fontsize, level, boxheight,weight,resolution) {
  var fsize=  parseFloat(fontsize) /resolution;

  if (level==1 && resolution> 20){
fsize=fontsize * resolution ;
//console.log(fsize)
  }
  var nodetext=
     new Text({  font:  fsize + 'px arial',  text: lbl,
      fill: new Fill({      color: 'rgba(0,0,0,0.5)'    }),
      stroke: new Stroke({  color: 'rgba(0,0,0,0.5)', width: 1  }),
      offsetX: 0,
      offsetY: 0,//boxheight/2,
    });
    return nodetext;
  };




var clusterSource = new Vector({  url: clusterData,  format: new GeoJSON() });
var clusterLayer = new VectorLayer({  source: clusterSource,  style: clusterStyleFunction });

var clusterBoundaySource = new Vector({ url: clusterBoundaryData, format: new GeoJSON() });
var clusterBoundayLayer = new VectorLayer({   source: clusterBoundaySource,   style: clusterBoundaryStyleFunction });

var edgeSource = new Vector({  url: edgeyData,  format: new GeoJSON() });
var edgesLayer = new VectorLayer({  source: edgeSource,  style: edgeStyleFunction});

var nodeSource = new Vector({  url: nodeData,  format: new GeoJSON()});
var nodesLayer = new VectorLayer({  source: nodeSource,  style: nodeStyleFunction});


//var geolayer = new TileLayer({  source: new OSM()});
// ClusterLayer,clusterBoundayLayer,
var map = new Map({
  controls: defaultControls().extend([new OverviewMap()]),
  layers: [clusterLayer,clusterBoundayLayer,  edgesLayer, nodesLayer],
  target: 'map',
  view: new View({center:  [17759.391499406964, -10439.758404798833],
      zoom: 17,//12, //17
      maxZoom: 18,
      minZoom: 10  })
});

global.map = map

var popup = new Overlay({  element: document.getElementById('popup') });
map.addOverlay(popup);


global.popup =popup


var edgeSelectPointerMove = new Select({
  condition: pointerMove,
  layers: [edgesLayer],
  style:selectStyleFunctionForEdge
  });


  var nodeSelectPointerMove = new Select({
    condition: pointerMove,
    layers: [nodesLayer],
       style: selectStyleFunctionForNode
    });

map.addInteraction(edgeSelectPointerMove);
map.addInteraction(nodeSelectPointerMove);

map.on('click', function(evt) {
  var element = popup.getElement();
  $(element).popover('destroy');
  var feature = map.forEachFeatureAtPixel(evt.pixel,
      function(feature, layer) {      return feature;  });
      if (feature) {
          var element = popup.getElement();
        var geometry = feature.getGeometry();
        var fid=feature.getId()
        var ftype = feature.getGeometry().getType()
        if ( fid &&  fid.search("cluster")>-1 ) return 0;

        $(element)[0].title =feature.get('label')
       var content =     feature.get('label') + " <br> Weight: " +  feature.get('weight')  ;

        $(element).popover('destroy');
        popup.setPosition(evt.coordinate);
        $(element).popover({ placement: 'top',  animation: false,  html: true,  content: content,    });
        $(element).popover('show');
  }
});
