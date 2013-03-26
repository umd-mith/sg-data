#!/bin/sh
echo "1. Combining Ajax-Solr Javascript"

AJAXSOLR_DIR='./lib/ajax-solr'

cat ${AJAXSOLR_DIR}/core/Core.js \
	${AJAXSOLR_DIR}/core/AbstractManager.js \
	${AJAXSOLR_DIR}/managers/Manager.jquery.js\
	${AJAXSOLR_DIR}/core/Parameter.js \
	${AJAXSOLR_DIR}/core/ParameterStore.js \
	${AJAXSOLR_DIR}/core/ParameterStore.js \
	${AJAXSOLR_DIR}/core/AbstractWidget.js \
	${AJAXSOLR_DIR}/core/AbstractTextWidget.js >> build/tmp.js

echo "2. Compressing with YUI"
java -jar support/yuicompressor.jar --type js -o build/ajax-solr.js build/tmp.js

echo "3. Complile CoffeScript"

coffee -c -o build src/search.coffee

echo "4. Copy to demo"
cp build/ajax-solr.js demo/js/
cp build/search.js demo/js/
cp lib/jquery.js demo/js/
cp lib/jquery-ui.js demo/js/

rm build/tmp.js


