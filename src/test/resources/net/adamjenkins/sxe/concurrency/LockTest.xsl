<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:bean="xalan://net.java.adamjenkins.sxe.elements.JavaBean"
                xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency"
                exclude-result-prefixes="java"
                extension-element-prefixes="assert concurrent bean">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <assert:init/>
        <xsl:variable name="lockAssertion">
            <bean:instantiate class="net.adamjenkins.sxe.concurrency.LockStateAssertion"/>
        </xsl:variable>
        <xsl:variable name="lock">
            <concurrent:readWriteLock/>
        </xsl:variable>
        <bean:invoke target="$lockAssertion" method="assertReadUnlocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <bean:invoke target="$lockAssertion" method="assertWriteUnlocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <concurrent:lockForReading lock="$lock"/>
        <bean:invoke target="$lockAssertion" method="assertReadLocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <concurrent:unLockForReading lock="$lock"/>
        <bean:invoke target="$lockAssertion" method="assertReadUnlocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <concurrent:lockForWriting lock="$lock"/>
        <bean:invoke target="$lockAssertion" method="assertWriteLocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <concurrent:unLockForWriting lock="$lock"/>
        <bean:invoke target="$lockAssertion" method="assertWriteUnlocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <bean:invoke target="$lockAssertion" method="assertReadUnlocked">
            <bean:parameter select="$lock"/>
        </bean:invoke>
        <assert:finalize/>
    </xsl:template>

</xsl:stylesheet>
