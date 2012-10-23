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
package edu.umd.mith.sga.processing.util

import scala.xml._
import scalaz.{ Node => _, _ }, Scalaz._

object XmlLabeler {
  def addCharOffsets(e: Elem): Elem = {
    def process(i: Int, n: Node): (Int, Node) = n match {
      case t @ Text(d) => (i + d.size, t)
      case p @ PCData(d) => (i + d.size, p)
      case e @ EntityRef(_) => (i + 1, e)
      case e: Elem => {
        val (j, processedChildren) = e.child.toList.mapAccumLeft(i, process)
        val processedAttrs = new PrefixedAttribute("mu", "b", i.toString,
          new PrefixedAttribute("mu", "e", j.toString, e.attributes)
        )

        (j, e.copy(attributes = processedAttrs, child = processedChildren))
      }
      case o => (i, o)
    }

    val nss = NamespaceBinding("mu", "http://mith.umd.edu/util/1#", e.scope)

    process(0, e.copy(scope = nss))._2.asInstanceOf[Elem]
  }
}

