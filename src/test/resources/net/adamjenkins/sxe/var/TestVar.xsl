<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:scratch="xalan://net.adamjenkins.sxe.elements.scratchpad.Scratchpad"
                extension-element-prefixes="scratch" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:variable name="test1">blah</xsl:variable>
        <scratch:setVar/>
        <xsl:value-of select="$test1"/>
        <xsl:value-of select="$test"/>
    </xsl:template>

</xsl:stylesheet>
