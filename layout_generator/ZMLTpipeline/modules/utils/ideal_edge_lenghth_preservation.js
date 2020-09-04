function ideal_edge_length_preservation(links, ideal_lengths)
{ 
    let val = 0;
    let total_dis = 0;
    let m = links.length;
    for (let i = 0; i < m; ++i) {
      let x1 = links[i].source.x;
      let y1 = links[i].source.y;
      let x2 = links[i].target.x;
      let y2 = links[i].target.y;
      let dis = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
      let diff = Math.abs(ideal_lengths[i] - dis);
      //let diff = Math.abs(edge_distance[links[i].index]+100 - dis);
      val = val + diff;
      total_dis = total_dis + dis;
    }
    val = val/m;
    let avg_dis = total_dis/m;
    return 1-(val/avg_dis);
}

links = [{source:{x:0, y:0}, target:{x:101, y:0}}, {source:{x:101, y:0}, target:{x:200, y:0}}, {source:{x:200, y:0}, target:{x:410, y:0}}];
ideal_lengths = [100, 100, 200];
ideal_edge_length_preservation(links, ideal_lengths);

