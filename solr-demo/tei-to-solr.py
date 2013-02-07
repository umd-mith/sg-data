#! /usr/bin/env python
# coding=UTF-8
""" Index fields from SGA TEI to a SOLR instance"""

import os, sys
import solr
import xml.sax
 
class Doc :
    def __init__(self, solr="", shelfmark="", doc_id=None, hand="ms", text=[], start=0, end=0):
      
        self.solr = solr

        self.shelfmark = shelfmark
        self.doc_id = doc_id

        self.hand = hand
        self.text = text
        self.start = start
        self.end = end

    def commit(self):
        print "id: %s\nshelf: %s\nhand: %s\nstart: %s\nend: %s\ntext: %s\n" % (self.doc_id, self.shelfmark, self.hand, self.start, self.end, self.text)
        self.solr.add(id=self.doc_id, shelfmark=self.shelfmark, hand=self.hand, start=self.start, end=self.end, text=self.text)
        self.solr.commit()

class GSAContentHandler(xml.sax.ContentHandler):
    def __init__(self, s):
        xml.sax.ContentHandler.__init__(self)

        self.solr = s
        self.hands = ["ms"]
        self.path = [] # name, hand
        self.shelfmark = ""
        self.tei_id = ""
        self.pos = 0
        self.doc = Doc(
            solr = self.solr,
            start = self.pos,
            hand = self.hands[-1])
 
    def startElement(self, name, attrs):
        # add to stack
        self.path.append([name, self.hands[-1]])
        
        if name == "surface":
            partOf = attrs["partOf"]
            self.shelfmark = partOf[1:] if partOf[0] == "#" else partOf
            self.doc.shelfmark = self.shelfmark
            self.tei_id = attrs["xml:id"]
            self.doc.doc_id = self.tei_id+"_"+str(self.pos)
        if "hand" in attrs:
            hand = attrs["hand"]
            if hand[0]=="#": hand = hand[1:]
            self.hands.append(hand)
            self.path[-1][-1] = hand
 
    def endElement(self, name):
        # Remove hand from hand stack if this is the last element with that hand    
        if len(self.path) > 1 and self.hands[-1] != self.path[-2][-1]:
            self.hands.pop()
        # Remove the element from element stack
        self.path.pop()

        # If we're through the document, close and commit the last doc
        if len(self.path) == 0:
            self.doc.end = self.pos
            self.doc.doc_id = self.doc.doc_id +"_"+ str(self.doc.end)
            self.doc.commit()
            print "**** end" 

 
    def characters(self, content):
        if self.doc.hand != self.hands[-1]:
            self.doc.end = self.pos
            self.doc.doc_id = self.doc.doc_id +"_"+ str(self.doc.end)
            self.doc.commit()

            # Set up new doc
            self.doc = Doc(
                solr = self.solr,
                shelfmark = self.shelfmark,
                hand = self.hands[-1],
                start = self.pos,
                doc_id = self.tei_id +"_"+ str(self.pos),
                text = [content])

            self.pos += len(content)
        # If not, append text to same doc, update position
        else:
            self.doc.text.append(content)
            self.pos += len(content) 
 
if __name__ == "__main__":

    if len(sys.argv) != 2:
        print 'Usage: ./tei-to-solr.py path'
        sys.exit(1)


    # Connect to solr instance
    s = solr.SolrConnection('http://localhost:8080/solr')

    # Walk provided directory for xml files; parse them and create/commit documents
    xml_dir = os.path.normpath(sys.argv[1]) + os.sep
    for f in os.listdir(xml_dir):
        if f.endswith('.xml'):
            source = open(xml_dir + f)
            xml.sax.parse(source, GSAContentHandler(s))
            source.close()