package edu.umd.mith.sga.sharedcanvas

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDinteger
import com.hp.hpl.jena.rdf.model.{ Model, ModelFactory, RDFNode }
import com.hp.hpl.jena.vocabulary.{ DC_11, DCTypes, RDF, RDFS }

import scala.collection.JavaConverters._
import scala.xml._

class Manifest(base: String, id: String, title: String, surfaces: Seq[Elem]) {
  val imageDerivBase = "http://sga.mith.org/images/derivatives/"

  val nss = Map(
    "rdf"  -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "sc"   -> "http://www.shared-canvas.org/ns/",
    "dms"  -> "http://dms.stanford.edu/ns/",
    "ore"  -> "http://www.openarchives.org/ore/terms/",
    "exif" -> "http://www.w3.org/2003/12/exif/ns#",
    "tei"  -> "http://www.tei-c.org/ns/1.0/",
    "oa"   -> "http://www.openannotation.org/ns/"
  )

  val model = ModelFactory.createDefaultModel
  nss.foreach { case (pre, ns) => model.setNsPrefix(pre, ns) }

  def property(ns: String, name: String) =
    model.createProperty(this.nss(ns) + name)

  def resource(ns: String, name: String) =
    model.createResource(this.nss(ns) + name)

  def intLiteral(i: Int) = model.createTypedLiteral(i, XSDinteger)

  val describes = property("ore", "describes")
  val isDescribedBy = property("ore", "isDescribedBy")
  val aggregates = property("ore", "aggregates")
  val hasBody = property("oa", "hasBody")
  val hasTarget = property("oa", "hasTarget")
  val width = property("exif", "width")
  val height = property("exif", "height")

  val Annotation = resource("oa", "Annotation")
  val Canvas = resource("sc", "Canvas")
  val Manifest = resource("sc", "Manifest")
  val Sequence = resource("sc", "Sequence")
  val Zone = resource("sc", "Zone")
  val ContentAnnotation = resource("sc", "ContentAnnotation")
  val AnnotationList = resource("sc", "AnnotationList")
  val Aggregation = resource("ore", "Aggregation")
  val ResourceMap = resource("ore", "ResourceMap")

  val (canvases, images, annotations) = surfaces.map { surface =>
    val attrs = surface.attributes.asAttrMap
    val id = attrs("xml:id")
    val lib = id.split("-").head
    val seqId = id.split("-").last
    val w = attrs("lrx").toInt
    val h = attrs("lry").toInt
    val canvas = model.createResource(base + "image-" + seqId)
    canvas.addProperty(RDF.`type`, Canvas)
    canvas.addProperty(RDF.`type`, resource("dms", "Canvas"))
    canvas.addProperty(RDFS.label, model.createLiteral("Image " + seqId))
    canvas.addProperty(width, intLiteral(w))
    canvas.addProperty(height, intLiteral(h))

    val image = model.createResource(imageDerivBase + lib + "/" + id + ".jpg")
    image.addProperty(RDF.`type`, DCTypes.Image)
    image.addProperty(RDF.`type`, resource("dms", "ImageBody"))
    image.addProperty(DC_11.format, "image/jpeg")
    image.addProperty(width, intLiteral(w))
    image.addProperty(height, intLiteral(h))

    val annotation = model.createResource(base + "imageanno/image-" + seqId)
    annotation.addProperty(RDF.`type`, Annotation)
    annotation.addProperty(RDF.`type`, resource("dms", "ImageAnnotation"))
    annotation.addProperty(hasBody, image)
    annotation.addProperty(hasTarget, canvas)
    (canvas, image, annotation)
  }.toList.unzip3

  val sequence = model.createResource(base + "NormalSequence")
  sequence.addProperty(RDF.`type`, Sequence)
  sequence.addProperty(RDF.`type`, RDF.List)
  sequence.addProperty(RDF.`type`, Aggregation)
  sequence.addProperty(RDF.`type`, resource("dms", "Sequence"))
  sequence.addProperty(RDFS.label, model.createLiteral("The editorial sequence"))

  val imageAnnos = model.createResource(base + "ImageAnnotations")
  imageAnnos.addProperty(RDF.`type`, AnnotationList)
  imageAnnos.addProperty(RDF.`type`, RDF.List)
  imageAnnos.addProperty(RDF.`type`, Aggregation)
  imageAnnos.addProperty(RDF.`type`, resource("dms", "ImageAnnotationList"))

  val manifest = model.createResource(base + "Manifest")
  manifest.addProperty(RDF.`type`, Manifest)
  manifest.addProperty(RDF.`type`, Aggregation)
  manifest.addProperty(RDF.`type`, resource("dms", "Manifest"))
  manifest.addProperty(DC_11.title, model.createLiteral(title))
  manifest.addProperty(RDFS.label, model.createLiteral(title))
  manifest.addProperty(property("tei", "idno"), model.createLiteral(id))
  manifest.addProperty(aggregates, sequence)
  manifest.addProperty(aggregates, imageAnnos)

  def mainModel = {
    val m = ModelFactory.createDefaultModel
    m.setNsPrefixes(model.getNsPrefixMap)
    m.add(model.listStatements(manifest, null, null))


    // First for the sequence of canvases.
    m.add(model.listStatements(sequence, null, null))

    canvases.foreach { canvas =>
      m.add(m.createStatement(sequence, aggregates, canvas))
      m.add(model.listStatements(canvas, null, null))
    }

    canvases match {
      case first :: rest =>
        m.add(m.createStatement(sequence, RDF.first, first))
        m.add(m.createStatement(sequence, RDF.rest, m.createList(
          rest.map(_.asInstanceOf[RDFNode]).toArray
        )))
      case _ => ()
    }

    // And finally the image annotations.
    m.add(model.listStatements(imageAnnos, null, null))

    images.foreach { image =>
      m.add(model.listStatements(image, null, null))
    }

    annotations.foreach { annotation =>
      m.add(m.createStatement(imageAnnos, aggregates, annotation))
      m.add(model.listStatements(annotation, null, null))
    }
    annotations match {
      case first :: rest =>
        m.add(m.createStatement(imageAnnos, RDF.first, first))
        m.add(m.createStatement(imageAnnos, RDF.rest, m.createList(
          rest.map(_.asInstanceOf[RDFNode]).toArray
        )))
      case _ => ()
    }

    m
  }

  def addXmlMeta(m: Model) = {
    val meta = m.createResource(base + "Manifest.xml")
    meta.addProperty(RDF.`type`, ResourceMap)
    meta.addProperty(DC_11.format, m.createLiteral("application/rdf+xml"))
    meta.addProperty(describes, manifest)
    m
  }

  def addJsonMeta(m: Model) = {
    val meta = m.createResource(base + "Manifest.json")
    meta.addProperty(RDF.`type`, ResourceMap)
    meta.addProperty(DC_11.format, m.createLiteral("application/rdf+json"))
    meta.addProperty(describes, manifest)
    m
  }
}

object Manifest extends App {
  import java.io.File

  val dir = args(0)
  val name = args(1)
  val description = args(2)

  val surfaces = new File(dir).listFiles.filter(
    _.getName.contains(name)
  ).sorted.map(XML.loadFile)

  val base = "http://sga.mith.org/sc-demo/" + name + "/"
  val m = new Manifest(base, name, description, surfaces)

  // The XML serialization.
  val modelX = m.addXmlMeta(m.mainModel)

  val writerX = new java.io.PrintWriter("Manifest.xml")
  modelX.write(writerX)
  writerX.close()

  // And JSON.
  val modelJ = m.addJsonMeta(m.mainModel)

  val writerJ = new java.io.PrintWriter("Manifest.json")
  new org.openjena.riot.system.JenaWriterRdfJson().write(modelJ, writerJ, null)
  writerJ.close()
}

