<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:spring="xalan://net.adamjenkins.sxe.elements.Spring"
                extension-element-prefixes="spring" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <spring:context base="CLASSPATH">
            <spring:resource location="/net/java/dev/sxe/spring/spring-config.xml"/>
        </spring:context>
        <spring:bean id="mock"/>
    </xsl:template>

</xsl:stylesheet>
