./eba/kmeans -action=clustering -C=modularity $1>out2
gvmap -e  -c 1 out2 > out3
neato -Nshape=rectangle -GforcelabelsX=false -Ecolor=grey -Gstart=123  -n2 -Tsvg out3 > map.svg
