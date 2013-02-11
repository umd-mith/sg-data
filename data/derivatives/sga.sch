<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
<!-- 
    Validation of milestones
    OK - Validate use of correct TEI element names in milestone element
    Validate pointer targets. Ie not valid id, element exist w this id
    Do milestones & anchors pair evenly? (Warning)
    Are ids valid and correctly referred in milestones?
    Make sure that milestones do not overlap with each other
-->
    
    <ns prefix="tei" uri="http://www.tei-c.org/ns/1.0"/>
    <ns prefix="xsl" uri="http://www.w3.org/1999/XSL/Transform"/>
    
    <!-- List of TEI elements used in milestones (this should be derived from the ODD) -->
    <let name="tei_elements" value="('div','p','lg','date','speaker','q','stage','seg','note')"/>
    
    
    <pattern>
        <let name="tei_elements_match" value="concat('^tei:(', string-join($tei_elements,'|'),')$')"/>
        <rule context="tei:milestone">
            <report test="not(matches(@unit, $tei_elements_match))"><name/> does not appear to have a valid TEI element as @unit.</report>
        </rule>
    </pattern>
    
    <pattern>        
        <rule context="tei:ptr">
            <assert test="substring(@target, 1, 1) = '#'">The @target of <name/> does not begin with #.</assert>
            <report test="not(//*[@xml:id=substring-after(current()/@target, '#')])">The @target of <name/> does not point to any ID in the document.</report>
        </rule>
    </pattern>
    
</schema>