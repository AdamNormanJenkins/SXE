# SXE
Some Xalan Extensions (SXE)

Adds functionality to Xalan that I've found helpful over the last 20 years of contracting.

```
<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:log="xalan://net.adamjenkins.sxe.elements.Logging"
                xmlns:java="http://xml.apache.org/xslt/java"
                exclude-result-prefixes="java"
                extension-element-prefixes="log">
    <xsl:output method="xml"/>
    
    <xsl:variable name="testvar">hello world</xsl:variable>
    
    <xsl:template match="/">
    	<log:debug message="hello test"/>
		<log:debug select="$testvar"/>
    </xsl:template>            
</xsl:stylesheet>
```
