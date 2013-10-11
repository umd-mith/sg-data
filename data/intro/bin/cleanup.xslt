<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xs="http://www.w3.org/2001/XMLSchema"
>

  <xsl:output method="html" encoding="utf-8" indent="yes" standalone="yes"/>

  <!--
    - By default, we copy everything we don't care about.
    -->
  <xsl:template match="@*|text()|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="style" />
  
  <!--
    - Content we don't want, like <font/> tags
    -->

<!-- 
  <xsl:template match="font">
    <xsl:apply-templates select="text()|node()" />
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates select="text()|node()" /></p>
  </xsl:template>
-->

  <!--
    - Pour the content into a simple HTML5 wrapper
    -->
    <!--
  <xsl:template match="/">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>&#xa;</xsl:text>
    <html lang="en">
      <head>
      </head>
      <body>
        <article>
          <xsl:apply-templates select="//body/*" />
        </article>
      </body>
    </html>
  </xsl:template>
-->

</xsl:stylesheet>
