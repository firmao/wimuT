# wimuT
Given a SPARQL query, which dataset(s) can execute such query? A traversal SPARQL query machine is able to do this, but the state-of-the-art has limitations, for instance, URIs should be dereferenceable, and the results are not complete since 43% of the URIs from the most used datasets are not dereferenceable anymore. This work, dubbed wimuT, provides an approach that overcomes the state-of-the-art, providing a new Traversal SPARQL query machine more complete and reliable overcoming the limitations from the state-of-the-art. The critical point in our method is to make available all datasets for a given URI obtained from an updated index on the web called [WIMU](https://github.com/dice-group/wimu). Our evaluation shows that our approach is the most complete, for example, wimuT brings, at least, 40\% more than results than the state-of-art. We also provide an API able to run a SPARQL query on the [LOD-A-LOT API](https://hdt.lod.labs.vu.nl/), without download the HDT dump-file. The limitations of Traversal approaches are indeed complex, for instance ranking URIs, querying datasets, run-time issues, among other points, here due to our experiments and observations we were able to provide a way to simplify this complexity and a method to improve the source selection.

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

I case you want to try some query we have a prototype (here)[http://139.18.8.58:8081/SQUIN-0.1.4/]
