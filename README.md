# wimuQ
Over the last years, the Web of Data has grown significantly. Various interfaces such as LOD Stats, LOD Laudromat, SPARQL endpoints provide access to the hundered of thousands of RDF datasets, representing billions of facts. These datasets are available in different formats (e.g., raw data dumps, HDT files) or directly accessible via SPARQL endpoints. Querying such large amount of distributed data is particularly challenging. In addition, many of these datasets are available as raw data dumps or HDT files and cannot be directly queried using the SPARQL query language. To tackle these problems, we present WimuQ, an approach to execute SPARQL queries over large amount of heterogeneous RDF data sources. At present, WimuQ is able to execute both federated and non-federated SPARQL queries over a total of 668,166 datasets from LOD Stats and LOD Laudromat as well as 559 active SPARQL endpoints. These data sources represent a total of 221.7 billion triples from more than 5 terabytes of information from datasets retrieved using the service 'Where is My URI' (WIMU). Our evaluation on state-of-the-art real-data benchmarks shows that WimuQ brings at least three times more results than previous approaches. 

Experiments:
`nohup java -Xmx10G -jar wimuT.jar queries.txt <TYPE> &`

(wimuT.jar)[https://doi.org/10.6084/m9.figshare.7117052]

Where `<TYPE>` can be:
 - wimut -> To execute only wimuT
 - squin -> To execute only SQUIN
 - lodalot -> To execute only SPARQLaLOT
 - all -> To execute wimuT + SQUIN + SPARQLatLOT

Measuring Memmory, CPU and Disk consumption:
- `python prodimem.py <PID> 60 > prodimem.log 2>&1 &`

Stable version of all Source code, experiments and web version:
(StableVersionV1)[https://doi.org/10.6084/m9.figshare.7370945]

A prototype is available here (here)[https://w3id.org/wimuq/]

About the code:
The main class is 'src/org/wimu/datasetselection/parallelv1/MainParallelv1.java'
