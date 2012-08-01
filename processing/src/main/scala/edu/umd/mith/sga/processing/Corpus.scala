package edu.umd.mith.sga.processing

import scala.util.parsing.combinator._

object LineParser extends RegexParsers {
  override val skipWhitespace = false
  def line = (marked | (text ^^ (Right(_)))).*
  val marked = "[" ~> text <~ "]" ^^ (Left(_))
  val text = "[^\\[\\]]+".r
  def parse(s: String) = this.parseAll(line, s.trim)
}

class CorpusReader(lines: Iterator[String]) {
  def consecutives[A](xs: Seq[Option[A]]): Seq[Seq[A]] =
    xs.foldLeft(Seq.empty[A] :: Nil) {
      case (y :: ys, Some(x)) => (y :+ x) :: ys
      case (ys, _) => Seq.empty[A] :: ys
    }.reverse.filter(_.nonEmpty)

  val Line = """^(.+)/(.+):(.+):(.+):(.+):(.+):(.+):([VPM]):([HT])/$""".r
  var last: (String, String) = _
  val content = lines.drop(2).map {
    case Line(content, shelf, page, ln, pub, pn, title, cat, ht) =>
      last = (shelf, page)
      Right((shelf, page) -> Some(content))
    case s if s.trim.isEmpty => Right(last -> None)
    case s => Left(s)
  }.flatMap(_.right.toOption).toSeq.groupBy(_._1).mapValues(
    lines => consecutives(lines.map(_._2))
  )

  def createStubTEI(shelf: String, page: String, id: String) =
    this.content.get(shelf -> page).map { zones =>
      <surface xml:id={id}
        xmlns="http://www.tei-c.org/ns/1.0"
        xmlns:sga="http://sga.mith.org/ns/1.0">
        <graphic url={"../../images/%s/%s.tif".format(id.substring(0, 2), id)}/>
        { zones.map { zone =>
          <zone type="main">
            { zone.map { line =>
              <line>{
                (LineParser.parse(line).get match {
                  case ss if ss.last.isLeft => ss.dropRight(1)
                  case ss => ss
                }).map {
                  case Right(t) => xml.Text(t)
                  case Left(t) => <del rend="strikethrough">{t}</del>
                }
              }</line>
            }}
          </zone>
        }}
      </surface>
    }
}

object CorpusReader extends App {
  val r = new CorpusReader(io.Source.fromFile(args(0)).getLines)
  val p = new xml.PrettyPrinter(80, 2)
  println(p.format(r.createStubTEI("BODe4", "11r", "ox-ms_shelley_e4-11r").get))
}

