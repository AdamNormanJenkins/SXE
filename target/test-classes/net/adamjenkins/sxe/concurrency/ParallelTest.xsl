<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
                xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert concurrent bean">
    <xsl:output method="xml"/>

    <xsl:param name="threadholder"/>

    <xsl:template match="/">
        <assert:init/>
        <xsl:apply-templates select="*"/>
        <concurrent:waitForThreads/>
        <assert:finalize/>
    </xsl:template>

    <xsl:template match="industry-details">
        <xsl:apply-templates select="sector/group/industry/segment"/>
    </xsl:template>

    <xsl:template match="segment">
        <name><xsl:value-of select="@name"/></name>
        <concurrent:parallel>
            <thread-count>
                <bean:invoke target="$threadholder" method="captureCurrentThread"/>
                <xsl:text>:</xsl:text>
                <name><xsl:value-of select="@name"/></name>
            </thread-count>
        </concurrent:parallel>
    </xsl:template>

</xsl:stylesheet>
