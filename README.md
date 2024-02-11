# Generating maps from layouts
## TL; DR
To generate map from an existing layout (e.g., computed externally and dumped into a networkx readable file)
checkout the notebook in javascript_to_dot-mw.ipynb
The notebook calls binaries and scripts from
` map_generator/` and `geojson_generator/`
and output the geojson file under `visualization_system/` that is ready to visualize with 
```
cd visualization_system
npm install # run once
npm start # test run. For build, run npm run build
```


---

# Old readme
# Multi-level tree based approach for interactive graph visualization with semantic zoom (ZMLT)

[Live system] (http://uamap-dev.arl.arizona.edu:8086)
paper: (https://arxiv.org/pdf/1906.05996.pdf)

# Step1: Multi-Layer extraction

look `ml_tree_extractor/readme.md`

# Step2: ZMLT layout

Please look the instructions in `layout_generator/ZMLTPipeline/Readme.md`

# Step3:  Create map
Please look the instruction in  `mapgenerator/readme.md`

# Step4:  Create geojson
Please look the instruction in  `geojson_generator/readme.md`

# Step5: Visualization system
visualization system  uses nodejs and openlayers to visualize multi layer trees. see source in `visualization_system`

# Step6: layout quality measurement
look the instruction in `layout_quality_measurement/readme.md`

## License
This project is licensed under the Apache License, Version 2.0 - see the license.txt file in individual modules for details.



## Team
Iqbal Hossain,
Felice De Luca https://github.com/felicedeluca,
Stephen Kobourov, and
Kathryn Gray
University of Arizona
