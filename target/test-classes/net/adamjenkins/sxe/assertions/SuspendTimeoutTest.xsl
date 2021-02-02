<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert concurrent">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <assert:init timeout="1000"/>
        <assert:suspendTimeout/>
        <concurrent:wait milliseconds="1001"/>
        <assert:finalize/>
    </xsl:template>

</xsl:stylesheet>
