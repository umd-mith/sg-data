<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei" version="2.0">

  <xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
    doctype-system="http://www.w3.org/TR/html4/strict.dtd" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:text>Page proof for </xsl:text>
          <xsl:value-of select="tei:surface/@xml:id"/>
        </title>
        <link href="http://fonts.googleapis.com/css?family=Antic+Slab" rel="stylesheet"
          type="text/css"/>
        <style type="text/css"><xsl:value-of select="$bootstrap-css"/></style>
      </head>
      <body>
        <div class="navbar navbar-fixed-top page_head">
          <h3>Page Proof: <xsl:value-of select="tei:surface/@xml:id"/></h3>
          <p>Generated at <xsl:value-of select="current-dateTime()"/></p>
        </div>
        <div class="container-fluid">
          <xsl:apply-templates/>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="tei:zone">
    <xsl:choose>
      <xsl:when test="@type='main'">
        <div id="{@type}" class="span8">
          <xsl:apply-templates/>
        </div>
      </xsl:when>
      <xsl:when test="@type='left_margin'">
        <div id="{@type}" class="span4">
          <xsl:apply-templates/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div id="{@type}">
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="tei:line">
    <p>
      <xsl:apply-templates/>
    </p>
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

  <xsl:variable name="bootstrap-css">/*! * Bootstrap v2.0.2 * * Copyright 2012 Twitter, Inc *
    Licensed under the Apache License v2.0 * http://www.apache.org/licenses/LICENSE-2.0 * * Designed
    and built with all the love in the world @twitter by @mdo and @fat. */ .clearfix { *zoom: 1; }
    .clearfix:before, .clearfix:after { display: table; content: ""; } .clearfix:after { clear:
    both; } .hide-text { overflow: hidden; text-indent: 100%; white-space: nowrap; }
    .input-block-level { display: block; width: 100%; min-height: 28px; /* Make inputs at least the
    height of their button counterpart */ /* Makes inputs behave like true block-level elements */
    -webkit-box-sizing: border-box; -moz-box-sizing: border-box; -ms-box-sizing: border-box;
    box-sizing: border-box; } article, aside, details, figcaption, figure, footer, header, hgroup,
    nav, section { display: block; } audio, canvas, video { display: inline-block; *display: inline;
    *zoom: 1; } audio:not([controls]) { display: none; } html { font-size: 100%;
    -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; } a:focus { outline: thin dotted
    #333; outline: 5px auto -webkit-focus-ring-color; outline-offset: -2px; } a:hover, a:active {
    outline: 0; } sub, sup { position: relative; font-size: 75%; line-height: 0; vertical-align:
    baseline; } sup { top: -0.5em; } sub { bottom: -0.25em; } img { height: auto; border: 0;
    -ms-interpolation-mode: bicubic; vertical-align: middle; } button, input, select, textarea {
    margin: 0; font-size: 100%; vertical-align: middle; } button, input { *overflow: visible;
    line-height: normal; } button::-moz-focus-inner, input::-moz-focus-inner { padding: 0; border:
    0; } button, input[type="button"], input[type="reset"], input[type="submit"] { cursor: pointer;
    -webkit-appearance: button; } input[type="search"] { -webkit-appearance: textfield;
    -webkit-box-sizing: content-box; -moz-box-sizing: content-box; box-sizing: content-box; }
    input[type="search"]::-webkit-search-decoration,
    input[type="search"]::-webkit-search-cancel-button { -webkit-appearance: none; } textarea {
    overflow: auto; vertical-align: top; } body { text-rendering: optimizeLegibility; margin: 0;
    font-family: "Antic Slab", serif; font-size: 19px; line-height: 1.4em; color: #333333;
    background-color: #ffffff; } a { color: #0088cc; text-decoration: none; } a:hover { color:
    #005580; text-decoration: underline; } .row { margin-left: -20px; *zoom: 1; } .row:before,
    .row:after { display: table; content: ""; } .row:after { clear: both; } [class*="span"] { float:
    right; margin-right: 20px; } .container, .navbar-fixed-top .container, .navbar-fixed-bottom
    .container { width: 940px; } .span12 { width: 940px; } .span11 { width: 860px; } .span10 {
    width: 780px; } .span9 { width: 700px; } .span8 { width: 620px; } .span7 { width: 540px; }
    .span6 { width: 460px; } .span5 { width: 380px; } .span4 { width: 300px; } .span3 { width:
    220px; } .span2 { width: 140px; } .span1 { width: 60px; } .offset12 { margin-left: 980px; }
    .offset11 { margin-left: 900px; } .offset10 { margin-left: 820px; } .offset9 { margin-left:
    740px; } .offset8 { margin-left: 660px; } .offset7 { margin-left: 580px; } .offset6 {
    margin-left: 500px; } .offset5 { margin-left: 420px; } .offset4 { margin-left: 340px; } .offset3
    { margin-left: 260px; } .offset2 { margin-left: 180px; } .offset1 { margin-left: 100px; }
    .row-fluid { width: 100%; *zoom: 1; } .row-fluid:before, .row-fluid:after { display: table;
    content: ""; } .row-fluid:after { clear: both; } .row-fluid > [class*="span"] { float: left;
    margin-left: 2.127659574%; } .row-fluid > [class*="span"]:first-child { margin-left: 0; }
    .row-fluid > .span12 { width: 99.99999998999999%; } .row-fluid > .span11 { width: 91.489361693%;
    } .row-fluid > .span10 { width: 82.97872339599999%; } .row-fluid > .span9 { width:
    74.468085099%; } .row-fluid > .span8 { width: 65.95744680199999%; } .row-fluid > .span7 { width:
    57.446808505%; } .row-fluid > .span6 { width: 48.93617020799999%; } .row-fluid > .span5 { width:
    40.425531911%; } .row-fluid > .span4 { width: 31.914893614%; } .row-fluid > .span3 { width:
    23.404255317%; } .row-fluid > .span2 { width: 14.89361702%; } .row-fluid > .span1 { width:
    6.382978723%; } .container { margin-left: auto; margin-right: auto; *zoom: 1; }
    .container:before, .container:after { display: table; content: ""; } .container:after { clear:
    both; } .container-fluid { margin-top: 40px; padding-left: 20px; padding-right: 20px; *zoom: 1;
    min-width: 1000px; } .container-fluid:before, .container-fluid:after { display: table; content:
    ""; } .container-fluid:after { clear: both; } .del { text-decoration: line-through; }
    #pagination { float: right; } #left_margin { padding-top: 100px; } .page_head { color: #ffffff;
    background-color: rgb(48,86,135); } .page_head h3, p { padding-left: 40px; }</xsl:variable>

</xsl:stylesheet>
