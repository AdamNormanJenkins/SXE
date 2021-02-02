<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:safe="xalan://net.adamjenkins.sxe.elements.SafeProcessing"
                xmlns:spring="xalan://net.adamjenkins.sxe.elements.Spring"
                extension-element-prefixes="safe spring" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <safe:safely>
            <safe:run>
                <!--try to access a spring bean without a context configured-->
                <spring:bean id="nobean"/>
            </safe:run>
            <safe:onError>Hi</safe:onError>
            <safe:after>Ho</safe:after>
        </safe:safely>
    </xsl:template>

</xsl:stylesheet>