# wimuT
SQUIN extension

Experiments:
nohup java -Xmx10G -jar wimuT.jar queries.txt <TYPE> &

Where <TYPE> can be:
  wimut -> To execute only wimuT
  squin -> To execute only SQUIN
  lodalot -> To execute only SPARQLaLOT
  all -> To execute wimuT + SQUIN + SPARQLatLOT

Measuring Memmory, CPU and Disk consumption:
python prodimem.py <PID> 60 > prodimem.log 2>&1 &
