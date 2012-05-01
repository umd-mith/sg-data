import java.io._
import scala.xml._

class MilestoneExpander(val doc: Elem) {

  private def expand: Node => Node = {
    case e: Elem => e.copy(child = this.expandChildren(e))
    case n => n
  }

  def save(writer: Writer) {
    XML.write(writer, this.expand(this.doc), "UTF-8", true, null)
    writer.close()
  }
}

class MilestoneExpander(val doc: Elem) {
  val ns = "http://sga.mith.org/ns/1.0"

  def expand: Node => Node = {
    case e: Elem => e.copy(child = this.expandChildren(e))
    case n => n 
  }

  private def expandChildren(e: Elem): Seq[Node] = {
    val (m, cs) = e.child.foldLeft[(Option[Elem], Seq[Node])](None, Vector()) {
      case ((m, cs), nm: Elem) if nm.label == "milestone" =>
        val Array(pre, name) = nm.attribute("unit").get.text.split(':')
        val attrs = nm.attribute(this.ns, "att-target").map { target =>
          new UnprefixedAttribute(
            target.text,
            nm.attribute(this.ns, "att-value").map(_.text).getOrElse(""),
            Null
          )
        }.getOrElse(Null)

        (Some(new Elem(null, name, attrs, doc.scope)), cs ++ m)

      case ((Some(m), cs), c) =>
        (Some(m.copy(child = m.child :+ this.expand(c))), cs)

      case ((None, cs), c) => (None, cs :+ this.expand(c))
    }

    cs ++ m
  }

  def save(writer: Writer) {
    XML.write(writer, this.expand(this.doc), "UTF-8", true, null)
    writer.close()
  }
}

val me = new MilestoneExpander(XML.loadFile(new File(args(0))))
me.save(new File(args(1)))

