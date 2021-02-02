<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
                xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion"
                extension-element-prefixes="bean" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:variable name="mockBean">
            <bean:instantiate class="net.adamjenkins.sxe.bean.MockBean"/>
        </xsl:variable>
        <xsl:variable name="result">
            <bean:invoke target="$mockBean" method="configure">
                <bean:parameter select="'text'"/>
                <bean:parameter select="true()"/>
                <bean:parameter select="1.1"/>
            </bean:invoke>
        </xsl:variable>
        <assert:equal arg1="'passed'" arg2="$result"/>
        <xsl:value-of select="$mockBean"/>
    </xsl:template>

</xsl:stylesheet>
