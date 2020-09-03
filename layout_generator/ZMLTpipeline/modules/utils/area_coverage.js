function area_coverage(coordinates){
  let max_x = coordinates[0].x;
  let min_x = coordinates[0].x;
  let max_y = coordinates[0].y;
  let min_y = coordinates[0].y;
  for(var i=0;i<coordinates.length;i++){
    if(max_x<coordinates[i].x)
      max_x = coordinates[i].x;
    if(min_x>coordinates[i].x)
      min_x = coordinates[i].x;
    if(max_y<coordinates[i].y)
      max_y = coordinates[i].y;
    if(min_y>coordinates[i].y)
      min_y = coordinates[i].y;
  }
  console.log("Area coverage:", (max_x-min_x)*(max_y-min_y)/(coordinates.length*1600));
}

my_coords = [{x:0, y:0}, {x:1000, y:0}, {x:1000, y:1000}, {x:0, y:1000}];
area_coverage(my_coords);

