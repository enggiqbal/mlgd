function areaCoverage(labelDoms){
  // labelDoms - a list of DOM elements for the labels.
  // If you are using d3, it is yourLabelSelection.nodes(), see:
  // https://github.com/d3/d3-selection/blob/v2.0.0/README.md#selection_nodes
  // d.getBoundingClientRect() gives you the bounding box (={x, y, width, height, ...}) of the label element in pixels, and it automatically considers the current zoom level of the view.
  let labelArea = 0;
  let xmin = +Infinity,
      xmax = -Infinity,
      ymin = +Infinity,
      ymax = -Infinity;
  for(let d of labelDoms){
    let bbox = d.getBoundingClientRect();
    labelArea += bbox.width * bbox.height;
    if(bbox.x < xmin){
      xmin = bbox.x;
    }
    if(bbox.x + bbox.width > xmax){
      xmax = bbox.x + bbox.width;
    }
    if(bbox.y < ymin){
      ymin = bbox.y;
    }
    if(bbox.y + bbox.height > ymax){
      ymax = bbox.y + bbox.height;
    }
  }
  let boundingArea = (ymax - ymin) * (xmax - xmin);
  return labelArea / boundingArea;
}
