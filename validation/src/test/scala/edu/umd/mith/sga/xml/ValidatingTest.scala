package edu.umd.mith.mishnah.xml

import edu.umd.mith.util.xml.ValidatingTestBase

class ValidatingTest(name: String) extends ValidatingTestBase(name) {
  val schema = "/derivatives/shelley_godwin_odd.rnc"
  val docs = Seq("/tei/SGA_Esdaile Notebook.xml")
}

