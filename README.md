# SXE

Since Xalan has fallen out of regular use this project should be considered unsupported :)


Some Xalan Extensions (SXE)

Adds functionality to Xalan that I've found helpful over the last 20 years of contracting.<br/>

See the example below (this one is adding logging to your XSLT, but there are other helper elements and functions).  For further information see the javadoc which is pretty extensive.

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

For details on how to use SXE for XSLT logging, see this article: https://www.linkedin.com/pulse/logging-xsltxalan-adam-jenkins/

For details on how to use SXE for Assertions, see this article: https://www.linkedin.com/pulse/assertions-xslt-javaxalan-adam-jenkins

Maven: https://search.maven.org/artifact/net.adamjenkins/sxe/1.1/jar
