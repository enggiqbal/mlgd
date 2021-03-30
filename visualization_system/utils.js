import * as d3 from 'd3';


export function markNonOverlapResolution(features, levels=undefined, minResolution=1, maxResolution=2000){
  if(levels === undefined){
    levels = Array.from(
      new Set(features.map(d=>+d.get('level')))
    ).sort((a,b)=>a-b);
  }
  let l0 = 0;
  let maxResolution_l = maxResolution;
  for (let l of levels){
    console.log(l);
    let nodes_l = features
      .filter(d=>l0 < +d.get('level') && +d.get('level') <= l)
      .sort((a,b)=>+a.get('level')-(+b.get('level')));
    let higher = features
      .filter(d=>+d.get('level') <= l0)
      .sort((a,b)=>+a.get('level')-(+b.get('level')));
    
    if(features.length > 10000){ //for large graphs
      maxResolution_l = markResolution(
        nodes_l, higher, features, minResolution, 
        Math.min(maxResolution,maxResolution_l*64)
      );
    }else{
      markResolution(nodes_l, higher, features, minResolution, maxResolution, true);
    }
    console.log(maxResolution_l);
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


function markResolution(nodes, higher, all, minResolution, maxResolution, quadTree=true, niter=undefined){
  const min0 = 1/maxResolution;
  const max0 = 1/minResolution;
  if (niter === undefined){
    niter = Math.ceil(Math.log2(max0-min0) * 1.5);
    console.log(niter);
  }

  let tree, sx, sy, id;
  if(quadTree){
    sx = d=>d.get('bbox').x;
    sy = d=>d.get('bbox').y;
    id = d=>d.get('index');
    let higherEqual = nodes.concat(higher);
    tree = d3.quadtree(higherEqual, sx, sy);
  }
  
  const maxBboxWidth = d3.max(all, d=>d.get('bbox').width);
  const maxBboxHeight = d3.max(all, d=>d.get('bbox').height);
  let resMinResolution = maxResolution;

  let current = new Set(higher.map(d=>d.get('index')));
  for(let n of nodes){
    let bi = n.get('bbox');
    let [x,y] = [bi.x, bi.y];
    
    let scale = min0;
    let neighbors;
    n.set('resolution', 1/scale);//init resolution
    if (quadTree){
      let rx = maxResolution * maxBboxWidth * 2;//depends current scale
      let ry = maxResolution * maxBboxHeight * 2;
      neighbors = searchQuadtree(tree, sx, sy, id, x-rx, x+rx, y-ry, y+ry);
      neighbors = neighbors.filter(i=>current.has(i));
    }else{
      neighbors = current;
    }

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
    resMinResolution = Math.min(1/scale, resMinResolution);
    current.add(n.get('index'));
  }
  return resMinResolution;
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


