<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
                extension-element-prefixes="bean" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:variable name="mockBean">
            <bean:instantiate class="net.adamjenkins.sxe.bean.MockBean">
                <bean:parameter select="'text'"/>
                <bean:parameter select="true()"/>
                <bean:parameter select="1.1"/>
            </bean:instantiate>
        </xsl:variable>
        <xsl:value-of select="$mockBean"/>
    </xsl:template>

</xsl:stylesheet>
