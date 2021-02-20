# SXE
Some Xalan Extensions (SXE)

Adds functionality to Xalan that I've found helpful over the last 20 years of contracting.<br/>

_Logging in XSLT with Xalan_

See the example below for how to add logging to your XSLT.  For further information see the javadoc which is extensive.

```
<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:log="xalan://net.adamjenkins.sxe.elements.Logging"
                xmlns:java="http://xml.apache.org/xslt/java"
                exclude-result-prefixes="java"
                extension-element-prefixes="log">
    <xsl:output method="xml"/>
    
    <xsl:variable name="var">world</xsl:variable>
    
    <xsl:template match="/">
    	<log:debug message="hello {$var} here is an attribute {./someelement/@someattribute}"/>
	<log:info select="./someotherelement/@someotherattribute"/>
    </xsl:template>            
</xsl:stylesheet>
```
See this article for more on XSLT: https://www.linkedin.com/pulse/logging-xsltxalan-adam-jenkins/
