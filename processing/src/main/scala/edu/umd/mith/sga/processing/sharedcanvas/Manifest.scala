/*
 * #%L
 * Utilities for the Shelley-Godwin Archive data repository
 * %%
 * Copyright (C) 2011 - 2012 University of Maryland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.umd.mith.sga.sharedcanvas

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDinteger
import com.hp.hpl.jena.rdf.model.{ Model, ModelFactory, RDFNode, Resource }
import com.hp.hpl.jena.vocabulary.{ DC_11, DCTypes, RDF, RDFS }

import edu.umd.mith.sga.processing.util.XmlLabeler

import scala.collection.JavaConverters._
import scala.xml._

class Manifest(base: String, id: String, title: String, teiSurfaces: Seq[Elem]) {
  val surfaces = teiSurfaces.map(XmlLabeler.addCharOffsets)

  val imageDerivBase = "http://sga.mith.org/images/derivatives/"
  //val teiBase = "https://github.com/umd-mith/sg-data/blob/master/data/tei/"
  val teiBase = "http://sga.mith.org/sc-demo/tei/"

  val nss = Map(
    "rdf"  -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "sc"   -> "http://www.shared-canvas.org/ns/",
    "dms"  -> "http://dms.stanford.edu/ns/",
    "ore"  -> "http://www.openarchives.org/ore/terms/",
    "exif" -> "http://www.w3.org/2003/12/exif/ns#",
    "tei"  -> "http://www.tei-c.org/ns/1.0/",
    //"oa"   -> "http://www.openannotation.org/ns/",
    "oa"   -> "http://www.w3.org/ns/openannotation/core/",
    "oax"  -> "http://www.w3.org/ns/openannotation/extension/",
    "sga"  -> "http://www.shelleygodwinarchive.org/ns1#"
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
  val hasSource = property("oa", "hasSource")
  val hasSelector = property("oa", "hasSelector")
  val width = property("exif", "width")
  val height = property("exif", "height")
  val offsetBegin = property("oax", "begin")
  val offsetEnd = property("oax", "end")

  val Annotation = resource("oa", "Annotation")
  val SpecificResource = resource("oa", "SpecificResource")
  val TextOffsetSelector = resource("oax", "TextOffsetSelector")
  val Canvas = resource("sc", "Canvas")
  val Manifest = resource("sc", "Manifest")
  val Sequence = resource("sc", "Sequence")
  val Zone = resource("sc", "Zone")
  val ContentAnnotation = resource("sc", "ContentAnnotation")
  val AnnotationList = resource("sc", "AnnotationList")
  val Aggregation = resource("ore", "Aggregation")
  val ResourceMap = resource("ore", "ResourceMap")

  sealed trait SourceAnno {
    def anno: Resource
    def file: Resource
  }

  case class ImageAnno(anno: Resource, file: Resource) extends SourceAnno
  case class TextAnno(
    anno: Resource,
    file: Resource,
    resource: Resource,
    selector: Resource,
    other: List[Resource]
  ) extends SourceAnno

  val (canvases, imageAnnos, textAnnos) = surfaces.map { surface =>
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

    val imageFile = model.createResource(imageDerivBase + lib + "/" + id + ".jpg")
    imageFile.addProperty(RDF.`type`, DCTypes.Image)
    imageFile.addProperty(RDF.`type`, resource("dms", "ImageBody"))
    imageFile.addProperty(DC_11.format, "image/jpeg")
    imageFile.addProperty(width, intLiteral(w))
    imageFile.addProperty(height, intLiteral(h))

    val imageAnno = model.createResource(base + "imageanno/image-" + seqId)
    imageAnno.addProperty(RDF.`type`, Annotation)
    imageAnno.addProperty(RDF.`type`, resource("dms", "ImageAnnotation"))
    imageAnno.addProperty(hasBody, imageFile)
    imageAnno.addProperty(hasTarget, canvas)

    val textFile = model.createResource(teiBase + lib + "/" + id + ".xml")
    textFile.addProperty(DC_11.format, "application/tei+xml")

    val textSelector = model.createResource()
    textSelector.addProperty(RDF.`type`, TextOffsetSelector)
    textSelector.addProperty(offsetBegin, intLiteral((surface \ "@{http://mith.umd.edu/util/1#}b").head.toString.toInt))
    textSelector.addProperty(offsetEnd, intLiteral((surface \ "@{http://mith.umd.edu/util/1#}e").head.toString.toInt))
    
    val textResource = model.createResource()
    textResource.addProperty(RDF.`type`, SpecificResource)
    textResource.addProperty(hasSource, textFile)
    textResource.addProperty(hasSelector, textSelector)

    val textAnno = model.createResource(base + "textanno/text-" + seqId)
    textAnno.addProperty(RDF.`type`, Annotation)
    textAnno.addProperty(RDF.`type`, ContentAnnotation)
    textAnno.addProperty(hasBody, textResource)
    textAnno.addProperty(hasTarget, canvas)

    (canvas, ImageAnno(imageAnno, imageFile), TextAnno(textAnno, textFile, textResource, textSelector, Nil))
  }.toList.unzip3

  val sequence = model.createResource(base + "NormalSequence")
  sequence.addProperty(RDF.`type`, Sequence)
  sequence.addProperty(RDF.`type`, RDF.List)
  sequence.addProperty(RDF.`type`, Aggregation)
  sequence.addProperty(RDF.`type`, resource("dms", "Sequence"))
  sequence.addProperty(RDFS.label, model.createLiteral("The editorial sequence"))

  val imageAnnoList = model.createResource(base + "ImageAnnotations")
  imageAnnoList.addProperty(RDF.`type`, AnnotationList)
  imageAnnoList.addProperty(RDF.`type`, RDF.List)
  imageAnnoList.addProperty(RDF.`type`, Aggregation)
  imageAnnoList.addProperty(RDF.`type`, resource("dms", "ImageAnnotationList"))

  val textAnnoList = model.createResource(base + "TextAnnotations")
  textAnnoList.addProperty(RDF.`type`, AnnotationList)
  textAnnoList.addProperty(RDF.`type`, RDF.List)
  textAnnoList.addProperty(RDF.`type`, Aggregation)

  val manifest = model.createResource(base + "Manifest")
  manifest.addProperty(RDF.`type`, Manifest)
  manifest.addProperty(RDF.`type`, Aggregation)
  manifest.addProperty(RDF.`type`, resource("dms", "Manifest"))
  manifest.addProperty(DC_11.title, model.createLiteral(title))
  manifest.addProperty(RDFS.label, model.createLiteral(title))
  manifest.addProperty(property("tei", "idno"), model.createLiteral(id))
  manifest.addProperty(aggregates, sequence)
  manifest.addProperty(aggregates, imageAnnoList)

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
    m.add(model.listStatements(imageAnnoList, null, null))

    imageAnnos.foreach { case ImageAnno(anno, file) =>
      m.add(m.createStatement(imageAnnoList, aggregates, anno))
      m.add(model.listStatements(anno, null, null))
      m.add(model.listStatements(file, null, null))
    }
    imageAnnos match {
      case first :: rest =>
        m.add(m.createStatement(imageAnnoList, RDF.first, first.anno))
        m.add(m.createStatement(imageAnnoList, RDF.rest, m.createList(
          rest.map(_.anno.asInstanceOf[RDFNode]).toArray
        )))
      case _ => ()
    }

    textAnnos.foreach { case TextAnno(anno, file, resource, selector, others) =>
      m.add(m.createStatement(textAnnoList, aggregates, anno))
      m.add(model.listStatements(anno, null, null))
      m.add(model.listStatements(file, null, null))
      m.add(model.listStatements(resource, null, null))
      m.add(model.listStatements(selector, null, null))
      others.foreach(other => m.add(model.listStatements(other, null, null)))
    }
    textAnnos match {
      case first :: rest =>
        m.add(m.createStatement(imageAnnoList, RDF.first, first.anno))
        m.add(m.createStatement(imageAnnoList, RDF.rest, m.createList(
          rest.map(_.anno.asInstanceOf[RDFNode]).toArray
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

