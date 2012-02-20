/*
 * #%L
 * Utilities for the Shelley-Godwin Archive data repository
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
package edu.umd.mith.sga.processing

import java.io.File
import java.io.PrintWriter
import org.apache.sanselan._
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.client._
import org.eclipse.egit.github.core.service._

import scala.collection.JavaConverters._

object CleanUp extends App {
  val client = new GitHubClient
  client.setCredentials(args(0), args(1))
  val service = new IssueService(client)

  service.pageIssues("umd-mith", "sg-data").iterator.asScala.map(_.asScala).flatten.foreach { issue =>
    println(issue.getTitle)
    issue.setTitle(issue.getTitle.replace("_c58", "_c56"))
    issue.setBody(issue.getBody.replace("_c58", "_c56"))
    service.editIssue("umd-mith", "sg-data", issue)
  }
}

