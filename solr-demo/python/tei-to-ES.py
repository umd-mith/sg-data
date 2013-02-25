#! /usr/bin/env python
# coding=UTF-8
""" Index fields from SGA TEI to a Solr instance"""

import os, sys
from pyes import *
import xml.sax
 
def setUp (index):
    """ Do basic setup when needed """

    text_field_map = {"type" : u"string", "index" : "analyzed", "term_vector" : "with_positions_offsets", "index_options" : "positions" } 
    mapping = { u"id" : {"type" : u"string", "index" : "not_analyzed"},
              u"shelfmark" : {"type" : u"string", "index" : "not_analyzed"},
              u"text" : text_field_map
              #u"added" : text_field_map,
              #u"deleted" : text_field_map
            }

    conn.create_index_if_missing(index)
    conn.put_mapping(doc_name, {"properties":mapping}, ["sga"])

    test_id = "ox-ms_abinger_c56-0119"
    test_shelfmark = "ox-ms_abinger_c56"
    test_text = u""""149 57 Chap. 14 The next day, contrary to the prognostics of our guides, 
    was fine although clouded. ‸ We visited the source of the Aveiron and rode about the valley 
    the whole da y until evening . These sublime and magnificent scenes afforded me the greatest 
    consolation that I was capable of receiving They elevated me from all littleness of feeling and 
    although they d did not remove my grief they t subdued and tranquilized it. In some degree, also 
    they diverted my mind from the thoughts ‸ over which it had brooded over for the last months. 
    I returned in the evening, fatigued but less unhappy and convered with the family t with more 
    cheerfulness than I had been accustomed to my custom for some time. My fa ther was pleased and 
    Elizabeth overjoyed; "My dear Cousin," said she, "You see what happiness you diffuse when you 
    are cheerful happy ; do not relapse again!— The following morning the rain poured down in torrents 
    and thick mists hid the summits of the mountains. I rose early but felt unusually melancholy. The 
    rain depressed my ‸ me , my old feelings recurred and I was miserable. I knew how my father would be 
    dissapointed at this sudden change and I wished to avoid him untill I had rev recovered myself so far 
    as to conceal the feelings that overpowered me — I knew that they would remain that day at the inn and 
    as I had
    """

    conn.index({"id" : test_id, "shelfmark" : test_shelfmark, "text" : test_text}, index, doc_name, 1) #last value is uid. If possible set it to TEI id.

    conn.refresh([index])


if __name__ == "__main__":

    
    location = "localhost:9200"
    index = "sga"
    doc_name = "TEI"

    conn = ES(location)
    if not conn.exists_index(index):
        setUp(index)

    q = Search(StringQuery("feelings"))
    q.add_highlight("text", number_of_fragments=0) #number_of_fragments=0 forces to return the entire matched field with highlights

    results = conn.search(q, indices=index)

    for r in results:
        print r._meta.highlight

# To query directly with JSON do:

# curl "localhost:9200/sga/_search?pretty=true" -d '{
#     "query": {
#         "query_string": {
#             "query": "feelings"
#         }
#     },
#     "highlight": {
#         "fields": {
#             "text": {"number_of_fragments": 0}
#         }
#     }    
# }'
