<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mock="xalan://net.adamjenkins.sxe.bean.MockBean"
                xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
                xmlns:mockelement="xalan://net.adamjenkins.sxe.var.MockElement"
                extension-element-prefixes="mock mockelement bean" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
    	<xsl:variable name="testmock" select="mock:new()"/>
    	<xsl:variable name="testmock2">
	        <bean:instantiate class="net.adamjenkins.sxe.bean.MockBean"/>
	    </xsl:variable>
        <xsl:value-of select="mock:aMethod($testmock)"/>      
    </xsl:template>

</xsl:stylesheet>
