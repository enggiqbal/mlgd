import Map from 'ol/Map.js';
   import View from 'ol/View.js';
   import GeoJSON from 'ol/format/GeoJSON.js';
   import MultiPoint from 'ol/geom/MultiPoint.js';
   import VectorLayer from 'ol/layer/Vector.js';
   import VectorSource from 'ol/source/Vector.js';
   import {Circle as CircleStyle, Fill, Stroke, Style} from 'ol/style.js';
   import Text from 'ol/style/Text';
   import {transform} from 'ol/proj.js';
   import Circle from 'ol/geom/Circle';
   import Feature from 'ol/Feature.js';
   import {Tile as TileLayer, VectorL} from 'ol/layer.js';
   import {OSM, Vector } from 'ol/source.js';
   import Overlay from 'ol/Overlay';
//import data from './map.geojson'
//import data from './T8.geojson'
import data from './cluster.geojson'


   var geojsonObject = {
     'type': 'FeatureCollection',
     'crs': {
       'type': 'name',
       'properties': {
         'name': 'EPSG:3857'
       }
     },
     'features': [{
       'type': 'Feature',
       'geometry': {
         'type': 'Polygon',
         'coordinates': [[[-5e6, 6e6], [-5e6, 8e6], [-3e6, 8e6],
           [-3e6, 6e6], [-5e6, 6e6]]]
       }
     }, {
       'type': 'Feature',
       'geometry': {
         'type': 'Polygon',
         'coordinates': [[[-2e6, 6e6], [-2e6, 8e6], [0, 8e6],
           [0, 6e6], [-2e6, 6e6]]]
       }
     }, {
       'type': 'Feature',
       'geometry': {
         'type': 'Polygon',
         'coordinates': [[[221134, 211134], [1e6, 8e6], [3e6, 8e6],
           [3e6, 6e6], [1e6, 6e6]]]
       }
     }, {
       'type': 'Feature',
       'geometry': {
         'type': 'Polygon',
         'coordinates': [[[-2e6, -1e6], [-1e6, 1e6],
           [0, -1e6], [-2e6, -1e6]]]
       }
     }]
   };


var getlabelFuction= function(feature)
{
return  'feature.get("label")'
}

   var styleFunction = function(feature,resolution) {
          var type=feature.getGeometry().getType()
      var  nodestyle=  new Style({
              stroke: new Stroke({
                color: 'red',
                width: 2
              }),
              fill: new Fill({
                color: 'rgba(255,0,0,0.2)'
              }),
               text: createTextStyle(feature.get("label"),feature.get("fontsize"),feature.get("level"),resolution)
            });
          return nodestyle;
        };


 var getText=function (lbl,resolution,level)
 {
   return lbl
   var txt=""
   level=parseInt(level)
   if (resolution> 1.19 && level<2)
     txt=lbl
   if (resolution> 1.1 && level<3)
     txt=lbl
     if (resolution> 1.08 && level<4)
       txt=lbl
       if (resolution> 1.06 && level<5)
         txt=lbl
         if (resolution> 1.04 && level<6)
           txt=lbl

// if   ( resolution < parseFloat(level))
  //txt=""
   console.log(resolution,level);
   return txt;
 };

            var createTextStyle = function(lbl,fontsize,level,resolution) {

              return new Text({
                font: parseFloat(  fontsize)   + 'px sans-serif',
                text: getText(lbl,resolution,level), //lbl,//getlabelFuction(),//"this is atest",
                fill: new Fill({color: "black"}),
                stroke: new Stroke({color: "black", width: 1}),
                offsetX: 10,
                offsetY: 10,
              });
            };



   var styles = {
     'Polygon': new Style({
           stroke: new Stroke({
             color: 'red',
             width: 2
           }),
           fill: new Fill({
             color: 'rgba(255,0,0,0.2)'
           }),
            text: createTextStyle()
         })
}






         var source = new Vector({
             url: data,
             format: new  GeoJSON()
         });



         var vectorLayer = new VectorLayer({
             source: source,
             style: styleFunction
         });

//console.log(vectorLayer)

global.source=source
var geolayer=new TileLayer({
 source: new OSM()
});

         var map = new Map({
           layers: [  vectorLayer   ],
           target: 'map',
           view: new View({
             center:  [16737.547785056177, -12158.50770793947],
             zoom: 12//17
           })
         });

global.map=map

         var popup = new Overlay({
           element: document.getElementById('popup')
         });
        // overlay.setPosition();

global.popup=popup

         map.addOverlay(popup);

   map.on('click', function(evt){
       var feature = map.forEachFeatureAtPixel(evt.pixel,
         function(feature, layer) {

          //  alert(feature.getGeometry().getCoordinates())
           return feature;
         });
       if (feature) {
           var geometry = feature.getGeometry();
           global.mygeometry=geometry
           console.log(geometry)
           var coord = geometry.getCoordinates();


           var content = '<h3>' + feature.get('label') + '</h3>'    ;
        //   content += '<h5>' + feature.get('AREA') + '</h5>';

      //     content_element.innerHTML = content;
           var element = popup.getElement();

      //     overlay.setPosition(coord);

         $(element).popover('destroy');
         var coordinates = evt.coordinate;
      popup.setPosition(coordinates);
      $(element).popover({
        placement: 'top',
        animation: false,
        html: true,
        content:  content
      });
     $(element).popover('show');



           console.log(coord)
           console.info(feature.getProperties());
       }
   });
