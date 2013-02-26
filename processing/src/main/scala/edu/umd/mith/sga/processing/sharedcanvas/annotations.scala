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

import scala.xml._

import edu.umd.mith.sga.processing.util.XmlLabeler

case class AnnotationExtractor(doc: Elem) {
  def attrEquals(name: String, value: String)(node: Node) =
    node.attributes.asAttrMap.get(name).exists(_ == value)

  def additions = {
    (doc \\ "add").map { e =>
      val attrs = e.attributes.asAttrMap
      (attrs("mu:b").toInt, attrs("mu:e").toInt)
    } ++ (doc \\ "addSpan").flatMap { e =>
      val attrs = e.attributes.asAttrMap
      val anchor = (doc \\ "anchor").filter(attrEquals("xml:id", attrs("spanTo").tail)).headOption
      anchor.map(a => (attrs("mu:b").toInt, a.attributes.asAttrMap("mu:b").toInt))
    }
  }

  def deletions = {
    (doc \\ "del").map { e =>
      val attrs = e.attributes.asAttrMap
      (attrs("mu:b").toInt, attrs("mu:e").toInt)
    } ++ (doc \\ "delSpan").flatMap { e =>
      val attrs = e.attributes.asAttrMap
      val anchor = (doc \\ "anchor").filter(attrEquals("xml:id", attrs("spanTo").tail)).headOption
      anchor.map(a => (attrs("mu:b").toInt, a.attributes.asAttrMap("mu:b").toInt))
    }
  }
}

