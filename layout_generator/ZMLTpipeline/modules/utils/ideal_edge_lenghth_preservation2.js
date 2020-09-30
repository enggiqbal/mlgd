function idealEdgeLengthPreservation2(links, ideal_lengths){
  let total_difference = 0;
  for (let i = 0; i < links.length; i++) {
    let x1 = links[i].source.x;
    let y1 = links[i].source.y;
    let x2 = links[i].target.x;
    let y2 = links[i].target.y;
    let dist = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    let diff = Math.abs(ideal_lengths[i] - dist);
    total_difference += Math.pow(diff / ideal_lengths[i], 2);
  }
  let average_difference = Math.sqrt(total_difference / links.length);
  return 1-average_difference;
}
