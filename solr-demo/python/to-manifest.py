#! /usr/bin/env python
# coding=UTF-8
""" Receive search result and map it to original TEI file """

# Mapping to TEI

import os, sys, re
import solr

def replace_entities (TEI):
    entities = {"&#x2038;" : "‸",
                "&#x2014;" : "—"
                }
    for e in entities:
        TEI = TEI.replace(e, entities[e])

    return TEI

if __name__ == "__main__":

    
    source = file("../../data/tei/ox/ox-ms_abinger_c56-0119.xml", "r")
    TEI = source.read()
    source.close()

    TEI = replace_entities(TEI)

    sample_txt = """

  
  149
  57
  
    Chap. 14
  
  
    
    The next day, contrary to the prognostics of our
    guides, was fine although clouded. 
        ‸
        We
       visited the
    source of the Aveiron and rode about the
    valley 
        the whole da
        y
        until evening
      . These sublime and
    magnificent scenes afforded me the greatest
    consolation that I was capable of receiving
    They elevated me from all littleness of feeling
    and although they d did not remove my
    grief they t subdued and tranquilized it.
    In some degree, also they diverted my
      mind
    from the thoughts 
        ‸
        over which
       it had brooded over
    for the last months. I returned in the
    evening, fatigued but less unhappy and convered
    with the family t with more cheerfulness than
    I had been 
        accustomed
        to
        my custom
       for some time. My fa
    ther was pleased and Elizabeth overjoyed; "My
    dear Cousin," said she, "You see what happiness
    you diffuse when you are 
        cheerful
        happy
      ; do
    not relapse again!—
    
    The following morning the rain poured
    down in torrents and thick mists hid
    the summits of the mountains. I rose early
    but felt unusually melancholy. The rain
    depressed 
        my
        ‸
        me
      , my old feelings recurred
    and I was miserable. I knew how my father
    would be dissapointed at this sudden
    change and I wished to avoid him
    untill I had rev recovered myself so far
    as to conceal the feelings that overpowered
    me — I knew that they would remain
    that day at the inn and as I had

"""
    
    sample_hl =  """

  
  149
  57
  
    Chap. 14
  
  
    
    The next day, contrary to the prognostics of our
    guides, was fine although clouded. 
        ‸
        We
       visited the
    source of the Aveiron and rode about the
    valley 
        the whole da
        y
        until evening
      . These sublime and
    magnificent scenes afforded me the greatest
    consolation that I was capable of receiving
    They elevated me from all littleness of <em>feeling</em>
    and although they d did not remove my
    grief they t subdued and tranquilized it.
    In some degree, also they diverted my
      mind
    from the thoughts 
        ‸
        over which
       it had brooded over
    for the last months. I returned in the
    evening, fatigued but less unhappy and convered
    with the family t with more cheerfulness than
    I had been 
        accustomed
        to
        my custom
       for some time. My fa
    ther was pleased and Elizabeth overjoyed; "My
    dear Cousin," said she, "You see what happiness
    you diffuse when you are 
        cheerful
        happy
      ; do
    not relapse again!—
    
    The following morning the rain poured
    down in torrents and thick mists hid
    the summits of the mountains. I rose early
    but felt unusually melancholy. The rain
    depressed 
        my
        ‸
        me
      , my old <em>feelings</em> recurred
    and I was miserable. I knew how my father
    would be dissapointed at this sudden
    change and I wished to avoid him
    untill I had rev recovered myself so far
    as to conceal the <em>feelings</em>; that overpowered
    me — I knew that they would remain
    that day at the inn and as I had

"""

    start_size = 4
    end_size = 5

    hl_pos = []

    for i, m in enumerate(re.finditer(r'<em>([^<]+)</em>', sample_hl)):
        cur_hl = len(m.group(1))
        start = m.start() - (start_size + end_size) * i
        end = start + (cur_hl)
        hl_pos.append((start, end, cur_hl))

    # for hl in hl_pos:
    #     print sample_txt[hl[0]:hl[1]]

    TEI_hl = []

    text_count = 0
    is_text = False
    for n, c in enumerate(TEI):

        if c == "<": is_text = False

        if is_text:
            text_count += 1
            for i, hl in enumerate(hl_pos):
                if text_count == hl[0]:
                    print TEI[n+1:n+hl[2]+1]
                    TEI_hl.append((n+1,n+hl[2]+1))

        if c == ">" and TEI[n+1] != '<': is_text = True

    print TEI_hl


        


