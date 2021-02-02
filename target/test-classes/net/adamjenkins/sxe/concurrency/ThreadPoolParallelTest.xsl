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
        <xsl:variable name="tp">
            <concurrent:threadPool minSize="3" maxSize="3" timeout="1000"/>
        </xsl:variable>
        <xsl:apply-templates select="industry-details/sector/group/industry/segment">
            <xsl:with-param name="pool" select="$tp"/>
        </xsl:apply-templates>
        <concurrent:waitForThreads/>
        <assert:finalize/>
    </xsl:template>

    <xsl:template match="segment">
        <xsl:param name="pool"/>
        <concurrent:parallel pool="$pool">
            <thread-count>
                <bean:invoke target="$threadholder" method="captureCurrentThread"/>
            </thread-count>
        </concurrent:parallel>
    </xsl:template>

</xsl:stylesheet>
