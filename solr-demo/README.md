Shelley-Godwin Archive - Search
======================

Populating the index
-------------------

In the directory indexing there is a configuration file for Solr and a python script to index the TEI files. Usage:

    python tei-to-solr.py FULL-PATH-TO-TEI

N.B. The indexing does not recurse through subdirecroties at the moment.

Building the example full-text search page
-------------------

Run build.sh to compile coffeescript and prepare the demo page. 

