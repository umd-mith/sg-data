#! /usr/bin/env python
# coding=UTF-8
""" Receive search result and map it to original TEI file """

# Mapping to TEI

import os, sys, re
import solr

def replace_entities (text):
    # Use dictionary to replace with real characters
    # entities = {"&#x2038;" : "‸",
    #             "&#x2014;" : "—"
    #             }
    # for e in entities:
    #     text = text.replace(e, entities[e])

    # Or just replace any entity with 1 character (?)
    text = re.sub(r'\&[^;]+;', r'?', text)

    return text

def toManifest (hl, TEI_id, source_dir="../../data/tei/ox/"):

    # Fetch TEI file, clean up entities
    source = file(source_dir+TEI_id+".xml", "r")
    TEI = source.read()
    source.close()
    TEI = replace_entities(TEI)

    # Find positions of highlighted text excluding markers

    start_size = 4
    end_size = 5

    hl_pos = []

    for i, m in enumerate(re.finditer(r'<em>([^<]+)</em>', hl)):
        cur_hl = len(m.group(1))
        start = m.start() - (start_size + end_size) * i
        end = start + (cur_hl)
        # Add 1 to make up for positions starting 0 when mapping to TEI
        hl_pos.append((start+1, end+1, cur_hl, m.group(1)))

    # Find highlighted text in source TEI

    TEI_hl = []

    text_count = 0
    is_text = False
    for n, c in enumerate(TEI):

        if c == "<": is_text = False

        if is_text:
            text_count += 1
            for i, hl in enumerate(hl_pos):
                if text_count == hl[0]:
                    TEI_hl.append((n, n+hl[2], TEI[n:n+hl[2]]))

        if n+1 != len(TEI) and c == ">" and TEI[n-1] != '?' and TEI[n+1] != '<': is_text = True

    # Return positions
    return TEI_hl

if __name__ == "__main__":

    # Connect to solr instance
    s = solr.SolrConnection('http://localhost:8080/solr')

    # Query now hardcoded, will actually come from manifest
    response = s.query('text:feelings', fields="text", highlight=True, hl_fragsize='0')

    for TEI_id in response.highlighting:
        hl = response.highlighting[TEI_id]['text'][0]
        print TEI_id, toManifest(hl, TEI_id)