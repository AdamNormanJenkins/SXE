<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:java="http://xml.apache.org/xslt/java"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <assert:init/>
        <xsl:apply-templates select="*"/>
        <assert:finalize/>
    </xsl:template>

    <xsl:template match="industry-details">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="sector">
        <xsl:variable name="limit" select="'2'"/>
        <assert:applyLimit name="mylimit" maxLimit="$limit"/>
    </xsl:template>

</xsl:stylesheet>
