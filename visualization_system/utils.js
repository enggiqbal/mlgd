import * as d3 from 'd3';


export function markNonOverlapResolution(features, levels=undefined, minResolution=1, maxResolution=2000){
  if(levels === undefined){
    levels = Array.from(
      new Set(features.map(d=>+d.get('level')))
    ).sort((a,b)=>a-b);
  }
  let l0 = 0;
  for (let l of levels){
    console.log(l);
    let nodes_l = features
    .filter(d=>l0 < +d.get('level') && +d.get('level') <= l)
    .sort((a,b)=>+a.get('level')-(+b.get('level')));
    let higher = features.filter(d=>+d.get('level') <= l0)
    .sort((a,b)=>+a.get('level')-(+b.get('level')));
    markScale(nodes_l, higher, features, minResolution, maxResolution);
    l0 = l;
  }
}


export function markBoundingBox(features, sl, font){
  let canvas = document.createElement('canvas');
  let ctx = canvas.getContext('2d');
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  //compute bbox
  features.forEach((d,i)=>{
    d.set('index', i);
    // let [x,y] = d.get('pos').split(',').map(d=>+d);
    let [x,y] = d.values_.geometry.flatCoordinates;
    ctx.font = `${sl(+d.get('level'))}px ${font}`;
    let m = ctx.measureText(d.get('label'));
    let width = m.actualBoundingBoxRight + m.actualBoundingBoxLeft;
    let height = m.actualBoundingBoxDescent + m.actualBoundingBoxAscent;
    d.set('bbox', {
      x, y, width, height
    });
  });
}


function markScale(nodes, higher, all, minResolution, maxResolution, niter=20){
  let sx = d=>d.get('bbox').x;
  let sy = d=>d.get('bbox').y;
  let id = d=>d.get('index');
  let higherEqual = nodes.concat(higher);
  // let tree = d3.quadtree(higherEqual, sx, sy);
  // let rx = maxResolution * d3.max(all, d=>d.get('bbox').width) * 2;//depends on min zoom extent
  // let ry = maxResolution * d3.max(all, d=>d.get('bbox').height) * 2;
  const min0 = 1/maxResolution;
  const max0 = 1/minResolution;
  let current = new Set(higher.map(d=>d.get('index')));
  for(let n of nodes){
    let bi = n.get('bbox');
    let [x,y] = [bi.x, bi.y];

    // let neighbors = searchQuadtree(tree, sx, sy, id, x-rx, x+rx, y-ry, y+ry);
    // neighbors = neighbors.filter(i=>current.has(i));
    let neighbors = current;
    let scale = min0;
    for(let j of neighbors){
      if(n.index === j){
        continue;
      }
      let min = min0;
      let max = max0;    
      let bj = all[j].get('bbox');
      let mid;// = (min+max)/2;
      for(let k=0; k<niter; k++){
        mid = (min+max)/2;
        if(isRectCollide2(bi, bj, mid)){
          [min,max] = [mid, max];
        }else{
          [min,max] = [min, mid];
        }
      }
      scale = Math.max(scale, max);
    }
    n.set('resolution', 1/scale);
    current.add(n.get('index'));
  }
}




export function isRectCollide2(rect1, rect2, scale=1){
  let rx = rect1.width/2;
  let ry = rect1.height/2;
  let x = rect1.x * scale;
  let y = rect1.y * scale;
  let rect1_left = x - rx;
  let rect1_right = x + rx;
  let rect1_top = y - ry;
  let rect1_bottom = y + ry;

  rx = rect2.width/2;
  ry = rect2.height/2;
  x = rect2.x * scale;
  y = rect2.y * scale;
  let rect2_left = x - rx;
  let rect2_right = x + rx;
  let rect2_top = y - ry;
  let rect2_bottom = y + ry;

  return (
       rect1_left <= rect2_right
    && rect1_right >= rect2_left
    && rect1_top <= rect2_bottom
    && rect1_bottom >= rect2_top
  );
}




//https://github.com/d3/d3-quadtree
export function searchQuadtree(quadtree, xGetter, yGetter, idGetter, xmin, xmax, ymin, ymax) {
  const results = [];
  quadtree.visit(function(node, x1, y1, x2, y2) {
    if (!node.length) {
      do {
        var d = node.data;
        // let bb = d.getBoundingClientRect();
        // let x = bb.x + bb.width/2;
        // let y = bb.y + bb.height/2;
        let x = xGetter(d);
        let y = yGetter(d);
        if (x >= xmin && x < xmax && y >= ymin && y < ymax) {
          results.push(idGetter(d));
        }
      } while (node = node.next);
    }
    return x1 >= xmax || y1 >= ymax || x2 < xmin || y2 < ymin;
  });
  return results;
}


