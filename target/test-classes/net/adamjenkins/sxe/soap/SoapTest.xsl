<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soap="xalan://net.adamjenkins.sxe.elements.SOAP"
                extension-element-prefixes="soap" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <soap:invoke endPoint="http://www50.brinkster.com/vbfacileinpt/np.asmx" action="http://microsoft.com/webservices/GetPrimeNumbers">
            <soap:body>
              <GetPrimeNumbers xmlns="http://microsoft.com/webservices/">
                 <max>10</max>
              </GetPrimeNumbers>
            </soap:body>
        </soap:invoke>
    </xsl:template>

</xsl:stylesheet>
