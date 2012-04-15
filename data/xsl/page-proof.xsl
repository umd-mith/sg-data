<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei" version="2.0">

  <xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
    doctype-system="http://www.w3.org/TR/html4/strict.dtd" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <html lang="en">
      <head>
        <meta charset="utf-8"/>
          <title>
            <xsl:text>Page proof: </xsl:text>
            <xsl:value-of select="tei:surface/@xml:id"/>
          </title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <meta name="description" content="HTML rendering of a page from the Shelley-Godwin Archive project—for proofreading purposes only"/>
              <meta name="author" content="Shelley-Godwin Archive"/>
                
        <!-- Le styles -->
        <link href="../assets/css/bootstrap.css" rel="stylesheet"/>
          <style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
                  <link href="../assets/css/bootstrap-responsive.css" rel="stylesheet"/>
                    
                    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
                    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
                    
                    <!-- Le fav and touch icons -->
                    <link href="http://fonts.googleapis.com/css?family=Antic+Slab" rel="stylesheet"
                      type="text/css"/>
                      <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png"/>
                        <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png"/>
                          <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png"/>
      </head>
      <body>
        
        <div class="navbar navbar-fixed-top">
          <div class="navbar-inner">
            <div class="container">
              <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </a>
              <a class="brand" href="#">Shelley-Godwin Archive</a>
              <div class="nav-collapse">
                <ul class="nav">
                  <li class="active"><a href="http://umd-mith.github.com/sg-data/docs/">Docs</a></li>
                  <li><a href="https://github.com/umd-mith/sg-data">Github</a></li>
                  <li><a href="mailto:mith@umd.edu">Contact</a></li>
                </ul>
              </div><!--/.nav-collapse -->
            </div>
          </div>
        </div>
        
        <div class="container-fluid">
          <div class="row-fluid">
            <div class="span5">
              <div class="sidebar-nav">
                <img src="http://sga.mith.org/images/derivatives/ox/{tei:surface/@xml:id}.jpg" alt="placeholder" style="max-width:100%"/>
              </div><!--/.well -->
            </div><!--/span-->
            <div class="span7">
              <div class="row-fluid">
                <div class="span7">
                  <h3>Page Proof: <xsl:value-of select="tei:surface/@xml:id"/></h3>
                  <p>Generated at <xsl:value-of select="current-dateTime()"/></p>
                </div><!--/span-->
                <div class="span7">
                  <!-- need to work through lines. every time a marginal insertion comes up, create a row-fluid split 3:9 -->
                  <xsl:apply-templates/>
                  <!--<div class="row-fluid">                   
                    <div class="span3"><p>This is a placeholder for marginal notes</p></div>
                  <div class="span9"><xsl:apply-templates/></div>
                  </div><!-\-/row-\->-->
                </div><!--/span--> 
              </div><!--/row-->
            </div><!--/span-->
          </div><!--/row-->
          
          <hr/>
            
            <footer>
              <p>Shelley-Godwin Archive: For Internal Use Only</p>
            </footer>
            
        </div><!--/.fluid-container-->
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="graphs">
    <xsl:for-each-group select="child::*" group-ending-with="tei:milestone[@unit='tei:p']">
      <p>
        <xsl:for-each select="current-group()">
          <xsl:call-template name="lb"/>
        </xsl:for-each>
      </p>
    </xsl:for-each-group>
  </xsl:template>
  
  <xsl:template name="lb" match="tei:line">
    <xsl:apply-templates/>
    <br/>
  </xsl:template>
  <!--<xsl:template match="tei:zone">
    <xsl:choose>
      <xsl:when test="@type='main'">
        <div class="{@type} span8">
          <xsl:call-template name="graphs"/>
        </div>
      </xsl:when>
      <xsl:when test="@type='left_margin'">
        <div class="{@type} span4">
          <xsl:apply-templates/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="{@type}">
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="tei:del[@rend='strikethrough']">
    <span class="del">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="tei:add">
    <xsl:choose>
      <xsl:when test="@place='superlinear'">
        <sup>
          <xsl:apply-templates/>
        </sup>
      </xsl:when>
      <xsl:when test="@place='sublinear'">
        <sub>
          <xsl:apply-templates/>
        </sub>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
-->

</xsl:stylesheet>
