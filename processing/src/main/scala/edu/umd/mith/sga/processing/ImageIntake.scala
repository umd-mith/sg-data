/*
 * #%L
 * MITH Utilities for GitHub
 * %%
 * Copyright (C) 2011 - 2012 Maryland Institute for Technology in the Humanities
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
package edu.umd.mith.github

import java.io.File
import java.io.PrintWriter
import org.apache.sanselan._
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.client._
import org.eclipse.egit.github.core.service._

/**
 * Usage: ImageIntake username password tiffDir teiDir (limit) 
 */
object ImageIntake extends App {
  assert(args.length > 4)

  val limit = if (args.length == 4) Int.MaxValue else args(4).toInt

  val client = new GitHubClient
  client.setCredentials(args(0), args(1))

  def createImageURL(id: String) =
    "http://ec2-23-20-49-205.compute-1.amazonaws.com/sga/images/derivatives/%s/%s.jpg".format(id.substring(0, 2), id)

  def createStubURL(id: String) =
    "https://github.com/umd-mith/sg-data/blob/master/data/tei/%s/%s.xml".format(id.substring(0, 2), id)

  def createStub(id: String, tiff: File) = {
    val image = Sanselan.getBufferedImage(tiff)
"""<?xml version="1.0" encoding="UTF-8"?>
<?xml-model
  href="../derivatives/shelley-godwin-page.rnc"
  type="application/relax-ng-compact-syntax"?>
<surface
  xmlns="http://www.tei-c.org/ns/1.0" xml:id="%s"
  ulx="0" uly="0" lrx="%d" lry="%d">
  <graphic url="../../images/%s/%s.tif"/>
    <zone>
    </zone>
</surface>
""".format(id, image.getWidth, image.getHeight, id.substring(0, 2), id)
  }

  def createSGAIssue(id: String) {
    val issue = new Issue
    issue.setTitle("New image: " + id)
  issue.setBody(
"""A [new image](%s) is ready to be transcribed, and a [TEI stub file](%s) has been generated

Please comment on this issue to claim the file before you begin working.
""".format(this.createImageURL(id), this.createStubURL(id)))
    val service = new IssueService(client)
    println(service.createIssue("umd-mith", "sg-data", issue))
  }

  val tiffDir = new File(args(2))
  assert(tiffDir.exists && tiffDir.isDirectory)

  val teiDir = new File(args(3))
  assert(teiDir.exists && teiDir.isDirectory)

  tiffDir.listFiles.sorted.take(limit).foreach { file =>
    val id = file.getName.replaceAll("\\.tif", "")
    println(id)
    val pw = new PrintWriter(new File(teiDir, "%s.xml".format(id)))
    pw.println(this.createStub(id, file))
    pw.close()
    this.createSGAIssue(id)
  }
}

