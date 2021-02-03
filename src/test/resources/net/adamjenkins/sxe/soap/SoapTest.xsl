<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soap="xalan://net.adamjenkins.sxe.elements.SOAP"
                extension-element-prefixes="soap" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <soap:invoke endPoint="http://www.dneonline.com/calculator.asmx" action="http://tempuri.org/Add">
            <soap:body>
			    <Add xmlns="http://tempuri.org/">
			      <intA>1</intA>
			      <intB>1</intB>
			    </Add>
            </soap:body>
        </soap:invoke>
    </xsl:template>

</xsl:stylesheet>
