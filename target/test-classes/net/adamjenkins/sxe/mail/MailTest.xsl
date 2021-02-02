<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mail="xalan://net.adamjenkins.sxe.elements.Mail"
                extension-element-prefixes="mail" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <xsl:variable name="email" select="'email'"/>
        <mail:session/>
        <mail:email from="test@test.com" subject="test email">
            <mail:to><xsl:text>mail@test.com</xsl:text></mail:to>
            <mail:body type="text/plain">
                <test>This is an <xsl:value-of select="$email"/></test>
            </mail:body>
        </mail:email>
    </xsl:template>

</xsl:stylesheet>
