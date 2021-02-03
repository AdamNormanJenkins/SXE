<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:ThreadHolder="xalan://net.adamjenkins.sxe.concurrency.ThreadHolder"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:log="xalan://net.adamjenkins.sxe.elements.Logging"
                xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
                xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert concurrent bean log ThreadHolder">
    <xsl:output method="xml"/>

    <xsl:param name="threadholder"/>

    <xsl:template match="/">
    	<log:debug message="matched root"/>
        <assert:init/>
        <log:debug message="initializing thread pool"/>
        <xsl:variable name="tp">
            <concurrent:threadPool minSize="3" maxSize="3" timeout="1000"/>
        </xsl:variable>
        <log:debug message="selecting segments"/>
        <xsl:apply-templates select="industry-details/sector/group/industry/segment">
            <xsl:with-param name="pool" select="$tp"/>
        </xsl:apply-templates>
        <concurrent:waitForThreads/>
        <assert:finalize/>
    </xsl:template>

    <xsl:template match="segment">
        <xsl:param name="pool"/>
        <log:debug message="matched segment"/>
        <concurrent:parallel pool="$pool">
            <thread-count>
            	<xsl:value-of select="ThreadHolder:captureCurrentThread()"/>
            </thread-count>
        </concurrent:parallel>
    </xsl:template>

</xsl:stylesheet>
