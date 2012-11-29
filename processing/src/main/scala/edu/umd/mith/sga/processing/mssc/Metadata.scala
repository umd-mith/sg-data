package edu.umd.mith.sga.processing.mssc

import scala.io.Source

class Metadata {
  private[this] val WorksLine = "^(\\S+) (.*)$".r

  val works = {
    val r = this.getClass.getResourceAsStream("/edu/umd/mith/sga/mssc/works.txt")
    val s = Source.fromInputStream(r)
    val m = s.getLines.filterNot(_.startsWith("#")).collect {
      case WorksLine(abbrev, name) => abbrev -> name
    }.toMap
    r.close()
    m
  }
}

