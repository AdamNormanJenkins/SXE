<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:spring="xalan://net.adamjenkins.sxe.elements.Spring"
                extension-element-prefixes="spring" version="1.0">
    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:variable name="ctx">
			<spring:context base="CLASSPATH">
                <spring:resource location="/net/adamjenkins/sxe/spring/spring-config.xml"/>
            </spring:context>
        </xsl:variable>    
        <spring:bean id="mock" context="$ctx"/>
    </xsl:template>

</xsl:stylesheet>
