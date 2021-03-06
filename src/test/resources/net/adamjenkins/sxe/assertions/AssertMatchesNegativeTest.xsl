<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <assert:init/>
        <assert:matches pattern="'\d{3}'" test="industry-details/sector[1]/group[1]/industry[1]/segment[1]/@name"/>
        <assert:finalize/>
    </xsl:template>

</xsl:stylesheet>
