<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    
    <ns prefix="tei" uri="http://www.tei-c.org/ns/1.0"/>
    <ns prefix="xsl" uri="http://www.w3.org/1999/XSL/Transform"/>
    
    <!-- Validate pointer targets. Ie not valid id, element exist w this id -->
    <!-- Validate that ptrs target add & addSpan; modify transform accordingly if needed -->
    <pattern>        
        <rule context="tei:ptr">
            <assert test="substring(@target, 1, 1) = '#'">The @target of <name/> does not begin with #.</assert>
            <report test="not(//*[@xml:id=substring-after(current()/@target, '#')])">The @target of <name/> does not point to any ID in the document.</report>
            <assert test="//*[@xml:id=substring-after(current()/@target, '#')][local-name()='add' or local-name()='addSpan']" role="warn"><name/> does not point to an add or addSpan.</assert>
        </rule>
    </pattern>
    
    <!-- Do milestones & anchors pair evenly? (Warning) -->
    <pattern>
        <rule context="/">
            <report test="count(descendant::tei:milestone) != count(descendant::tei:anchor)" role="warn">There is an odd number of milestones and anchors.</report>
        </rule>
    </pattern>
    
    <!-- Are ids valid and correctly referred in milestones? -->
    <pattern>
        <rule context="tei:milestone[@spanTo]">
            <assert test="substring(@spanTo, 1, 1) = '#'">The @spanTo of <name/> does not begin with #.</assert>
            <report test="not(//tei:anchor[@xml:id=substring-after(current()/@spanTo, '#')])">The @spanTo of <name/> does not point to any anchor ID in the document.</report>
        </rule>
    </pattern>
    
    <!-- Make sure that milestones do not overlap with each other -->
    <pattern>
        <rule context="tei:anchor[@xml:id = //tei:milestone/substring-after(@spanTo, '#')]">  
            <let name="cur_a_id" value="@xml:id"/>
            <let name="prev_a_id" value="preceding::tei:anchor[@xml:id = //tei:milestone/substring-after(@spanTo, '#')]
                                                   [preceding::tei:milestone[substring-after(@spanTo, '#')=$cur_a_id]][1]/@xml:id"/>
            <!-- The milestone corresponding to the immediately preceding anchor MUST occur after the current anchor's milestone
                (or, in other words, it MUST have the current anchor's milestone as a preceding node -->
            <assert test="if ($prev_a_id != '') then
                preceding::tei:milestone[substring-after(@spanTo, '#')=$prev_a_id][preceding::tei:milestone[substring-after(@spanTo, '#') = $cur_a_id]]
                else true()
                ">Overlap detected in milestone-anchor area marked by anchors with IDs <value-of select="$prev_a_id"/> and <value-of select="$cur_a_id"/>.</assert>
        </rule>
    </pattern>
    
    <!-- Validate that addSpan and closing anchor are at same level of tree -->
    <pattern>
        <rule context="tei:addSpan">
            <assert test="substring(@spanTo, 1, 1) = '#'">The @spanTo of <name/> does not begin with #.</assert>
            <assert test="generate-id(parent::*) = generate-id(//tei:anchor[@xml:id=substring-after(current()/@spanTo, '#')]/parent::*)"><name/> is not at the same level of its anchor.</assert>
        </rule>
    </pattern>
    
</schema>