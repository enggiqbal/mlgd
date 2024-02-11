function randomNumber(min, max) {  
    min = Math.ceil(min); 
    max = Math.floor(max); 
    return Math.floor(Math.random() * (max - min + 1)) + min; 
}

//var initialize = function () {
var addCrossingForce = function () {
  //console.log(graph.graphData.links);
  links = graph.graphData.links;
  m = links.length;
  res = linkCrossings();
  console.log("Number of crossings:", res.length);
  if(res.length>0){
    // select a crossing pair randomly
    var crossingIndex = randomNumber(0, res.length-1);
    var crossingPair = res[crossingIndex];
    // swap the edges with probability .5
    var coin = randomNumber(0, 1);
    if(coin==0){
      var t = crossingPair[0];
      crossingPair[0] = crossingPair[1];
      crossingPair[1] = t;
    }
    // swap the points with probability .5
    var coin = randomNumber(0, 1);
    if(coin==0){
      var t = crossingPair[0][0];
      crossingPair[0][0] = crossingPair[0][1];
      crossingPair[0][1] = t;
    }
    var moveAmountUp = crossingPair[0][0].x-graph.graphData.nodes[0].x;
    var moveAmountRight = crossingPair[0][0].y-graph.graphData.nodes[0].y;
    var nodeId = crossingPair[0][1].index;
    console.log("Moving ", id_to_label[nodeId], " to ", moveAmountRight, moveAmountUp);
    movePar(nodeId, moveAmountUp, moveAmountRight);
  }
  else{
    stopCrossingForce();
  }
}

function stopCrossingForce() {
  clearInterval(crossingForceInterval);
}

function direction (pi, pj, pk) {
    var p1 = [pk[0] - pi[0], pk[1] - pi[1]];
    var p2 = [pj[0] - pi[0], pj[1] - pi[1]];
    return p1[0] * p2[1] - p2[0] * p1[1];
}

// Is point k on the line segment formed by points i and j?
// Inclusive, so if pk == pi or pk == pj then return true.
function onSegment (pi, pj, pk) {
    return Math.min(pi[0], pj[0]) <= pk[0] &&
      pk[0] <= Math.max(pi[0], pj[0]) &&
      Math.min(pi[1], pj[1]) <= pk[1] &&
      pk[1] <= Math.max(pi[1], pj[1]);
}

function linesCross (line1, line2) {
    var d1, d2, d3, d4;

    // CLRS 2nd ed. pg. 937
    d1 = direction(line2[0], line2[1], line1[0]);
    d2 = direction(line2[0], line2[1], line1[1]);
    d3 = direction(line1[0], line1[1], line2[0]);
    d4 = direction(line1[0], line1[1], line2[1]);

    if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
      ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
      return true;
    } else if (d1 === 0 && onSegment(line2[0], line2[1], line1[0])) {
      return true;
    } else if (d2 === 0 && onSegment(line2[0], line2[1], line1[1])) {
      return true;
    } else if (d3 === 0 && onSegment(line1[0], line1[1], line2[0])) {
      return true;
    } else if (d4 === 0 && onSegment(line1[0], line1[1], line2[1])) {
      return true;
    }

    return false;
}

function linksCross (link1, link2) {
    // Self loops are not intersections
    if (link1.index === link2.index ||
      link1.source === link1.target ||
      link2.source === link2.target) {
      return false;
    }

    // Links cannot intersect if they share a node
    if (link1.source === link2.source ||
      link1.source === link2.target ||
      link1.target === link2.source ||
      link1.target === link2.target) {
      return false;
    }

    var line1 = [
      [link1.source.x, link1.source.y],
      [link1.target.x, link1.target.y]
    ];

    var line2 = [
      [link2.source.x, link2.source.y],
      [link2.target.x, link2.target.y]
    ];

    return linesCross(line1, line2);
}

function linkCrossings () {
    var i, j, c = 0, link1, link2, line1, line2;;
    var res = [];

    // Sum the upper diagonal of the edge crossing matrix.
    for (i = 0; i < m; ++i) {
      for (j = i + 1; j < m; ++j) {
        link1 = links[i], link2 = links[j];

        // Check if link i and link j intersect
        if (linksCross(link1, link2)) {
          line1 = [
            [link1.source.x, link1.source.y],
            [link1.target.x, link1.target.y]
          ];
          line2 = [
            [link2.source.x, link2.source.y],
            [link2.target.x, link2.target.y]
          ];
          ++c;
          //console.log(id_to_label[link1.source.index], ",", id_to_label[link1.target.index], " crosses ", id_to_label[link2.source.index], ",", id_to_label[link2.target.index]);
          res.push([[link1.source, link1.target], [link2.source, link2.target]]);
          //d += Math.abs(idealAngle - acuteLinesAngle(line1, line2));
        }
      }
    }

    return res;
}




