package edu.umd.mith.sga.xml

import edu.umd.mith.util.xml.ValidatingTestBase

class ValidatingTest(name: String) extends ValidatingTestBase(name) {
  val schema = "/schema/rng/shelley-godwin.rng"
  val docs = Seq("/tei/SGA_Esdaile Notebook.xml")
}

