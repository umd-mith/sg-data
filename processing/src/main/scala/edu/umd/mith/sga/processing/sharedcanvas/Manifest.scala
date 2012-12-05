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
  //println(surfaces)

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
  val Highlight = property("oax", "Highlight")
  val TextOffsetSelector = resource("oax", "TextOffsetSelector")
  val FragmentSelector = resource("oa", "FragmentSelector")
  val Canvas = resource("sc", "Canvas")
  val Manifest = resource("sc", "Manifest")
  val Sequence = resource("sc", "Sequence")
  val Zone = resource("sc", "Zone")
  val ContentAnnotation = resource("sc", "ContentAnnotation")
  val AnnotationList = resource("sc", "AnnotationList")
  val Aggregation = resource("ore", "Aggregation")
  val ResourceMap = resource("ore", "ResourceMap")
  val LineAnnotation = resource("sga", "LineAnnotation")
  val AdditionAnnotation = resource("sga", "AdditionAnnotation")
  val DeletionAnnotation = resource("sga", "DeletionAnnotation")

  /*sealed trait SourceAnno {
    def anno: Resource
    def file: Resource
  }*/

  case class ImageAnno(anno: Resource, file: Resource)
  case class TextAnno(
    file: Resource,
    annos: List[ZoneAnno],
    other: List[Resource]
  )

  case class ZoneAnno(
    anno: Resource,
    resource: Resource,
    selector: Resource,
    regionResource: Resource,
    regionSelector: Resource
  )

  val (canvases, imageAnnos, textAnnos) = surfaces.map { surface =>
    val attrs = surface.attributes.asAttrMap
    val id = attrs("xml:id")
    val lib = id.split("-").head
    val seqId = id.split("-").last
    val w = attrs("lrx").toInt
    val h = attrs("lry").toInt

    val annotationExtractor = AnnotationExtractor(surface)

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

    val lines = (surface \\ "line").toList.zipWithIndex.flatMap { case (line, i) =>
      val attrs = line.attributes.asAttrMap
      val lineSelector = model.createResource()
      lineSelector.addProperty(RDF.`type`, TextOffsetSelector)
      lineSelector.addProperty(offsetBegin, intLiteral(attrs("mu:b").toInt))
      lineSelector.addProperty(offsetEnd, intLiteral(attrs("mu:e").toInt))

      val lineResource = model.createResource()
      lineResource.addProperty(RDF.`type`, SpecificResource)
      lineResource.addProperty(hasSource, textFile)
      lineResource.addProperty(hasSelector, lineSelector)

      val lineAnno = model.createResource(base + "textanno/text-" + seqId + "-%04d".format(i))
      lineAnno.addProperty(RDF.`type`, Annotation)
      lineAnno.addProperty(RDF.`type`, LineAnnotation)
      lineAnno.addProperty(RDF.`type`, Highlight)
      lineAnno.addProperty(hasTarget, lineResource)

      lineSelector :: lineResource :: lineAnno :: Nil
    }

    val additions = annotationExtractor.additions.toList.flatMap { case (b, e) =>
      val selector = model.createResource()
      selector.addProperty(RDF.`type`, TextOffsetSelector)
      selector.addProperty(offsetBegin, intLiteral(b))
      selector.addProperty(offsetEnd, intLiteral(e))

      val resource = model.createResource()
      resource.addProperty(RDF.`type`, SpecificResource)
      resource.addProperty(hasSource, textFile)
      resource.addProperty(hasSelector, selector)

      val anno = model.createResource()
      anno.addProperty(RDF.`type`, Annotation)
      anno.addProperty(RDF.`type`, AdditionAnnotation)
      anno.addProperty(RDF.`type`, Highlight)
      anno.addProperty(hasTarget, resource)

      selector :: resource :: anno :: Nil
    }

    val deletions = annotationExtractor.deletions.toList.flatMap { case (b, e) =>
      val selector = model.createResource()
      selector.addProperty(RDF.`type`, TextOffsetSelector)
      selector.addProperty(offsetBegin, intLiteral(b))
      selector.addProperty(offsetEnd, intLiteral(e))

      val resource = model.createResource()
      resource.addProperty(RDF.`type`, SpecificResource)
      resource.addProperty(hasSource, textFile)
      resource.addProperty(hasSelector, selector)

      val anno = model.createResource()
      anno.addProperty(RDF.`type`, Annotation)
      anno.addProperty(RDF.`type`, DeletionAnnotation)
      anno.addProperty(RDF.`type`, Highlight)
      anno.addProperty(hasTarget, resource)

      selector :: resource :: anno :: Nil
    }

    val leftMarginCount = (surface \\ "zone").filter(
      zone => (zone \ "@type").text == "left_margin"
    ).size

    val zoneAnnos = (surface \\ "zone").foldLeft((List.empty[ZoneAnno], 0)) {
      case ((zones, leftMarginIdx), zone) =>
        val t = (zone \ "@type").text
        val isLeftMargin = (t == "left_margin")

        val coords = t match {
          case "top" => Some((0.4, 0.0) -> (0.2, 0.05))
          case "pagination" => Some((0.8, 0.0) -> (0.1, 0.05))
          case "library" => Some((0.9, 0.0) -> (0.1, 0.05))
          case "left_margin" => Some(
            (0.0,  0.05 + 0.95 * (leftMarginIdx.toDouble / leftMarginCount)) ->
            (0.25, 0.95 * (1.0 / leftMarginCount))
          )
          case "main" => Some((0.25, 0.05) -> (0.75, 0.95))
          case other => { println("Unknown zone: " + other); None }
        }

        coords match {
          case Some(((x, y), (zw, zh))) =>
            val xywh = "xywh=%d,%d,%d,%d".format(
             (x * w).toInt, (y * h).toInt, (zw * w).toInt, (zh * h).toInt
            )

            val attrs = zone.attributes.asAttrMap
            if (seqId == "0017") println(attrs("mu:b") + " " + attrs("mu:e"))

            val zoneSelector = model.createResource()
            zoneSelector.addProperty(RDF.`type`, TextOffsetSelector)
            zoneSelector.addProperty(offsetBegin, intLiteral(attrs("mu:b").toString.toInt))
            zoneSelector.addProperty(offsetEnd, intLiteral(attrs("mu:e").toString.toInt))

            val zoneResource = model.createResource()
            zoneResource.addProperty(RDF.`type`, SpecificResource)
            zoneResource.addProperty(hasSource, textFile)
            zoneResource.addProperty(hasSelector, zoneSelector)

            val zoneRegionSelector = model.createResource()
            zoneRegionSelector.addProperty(RDF.`type`, FragmentSelector)
            zoneRegionSelector.addProperty(RDF.value, model.createLiteral(xywh))

            val zoneRegionResource = model.createResource()
            zoneRegionResource.addProperty(RDF.`type`, SpecificResource)
            zoneRegionResource.addProperty(hasSource, canvas)
            zoneRegionResource.addProperty(hasSelector, zoneRegionSelector)

            val zoneAnno = model.createResource()
            zoneAnno.addProperty(RDF.`type`, Annotation)
            zoneAnno.addProperty(RDF.`type`, ContentAnnotation)
            zoneAnno.addProperty(hasBody, zoneResource)
            zoneAnno.addProperty(hasTarget, zoneRegionResource)

            (
              zones :+ ZoneAnno(zoneAnno, zoneResource, zoneSelector, zoneRegionResource, zoneRegionSelector),
              leftMarginIdx + (if (isLeftMargin) 1 else 0)
            )
          case _ => (zones, leftMarginIdx)
        }
    }._1

    (canvas, ImageAnno(imageAnno, imageFile), TextAnno(textFile, zoneAnnos, lines ::: additions ::: deletions))
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

    textAnnos.foreach {
      case TextAnno(file, zoneAnnos, others) =>
        zoneAnnos.foreach {
          case ZoneAnno(zoneAnno, zoneResource, zoneSelector, zoneRegionResource, zoneRegionSelector) =>
            m.add(m.createStatement(textAnnoList, aggregates, zoneAnno))
            m.add(model.listStatements(file, null, null))
            m.add(model.listStatements(zoneAnno, null, null))
            m.add(model.listStatements(zoneResource, null, null))
            m.add(model.listStatements(zoneSelector, null, null))
            m.add(model.listStatements(zoneRegionResource, null, null))
            m.add(model.listStatements(zoneRegionSelector, null, null))
            others.foreach(other => m.add(model.listStatements(other, null, null)))
        }
    }

    textAnnos.flatMap(_.annos) match {
      case first :: rest =>
        m.add(m.createStatement(textAnnoList, RDF.first, first.anno))
        m.add(m.createStatement(textAnnoList, RDF.rest, m.createList(
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

