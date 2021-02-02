# SXE
Some Xalan Extensions (SXE)

This project is a bunch of extensions to Xalan (Java) XSLT processing that I've found useful over 20 years of contracting.  

It adds simple but handy stuff to Xalan like logging, assertions, error checking, interaction with things like spring etc etc

For usage see the javadoc (which is extensive).  A quick example below is shown for how to add java logging to your XSLT.

<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0"
                xmlns:log="xalan://net.adamjenkins.sxe.elements.Logging"
                xmlns:java="http://xml.apache.org/xslt/java"
                exclude-result-prefixes="java"
                extension-element-prefixes="log">
    <xsl:output method="xml"/>
    
    <xsl:variable name="var1">World</xsl:variable>
    <xsl:variable name="var2">hello world</xsl:variable>
    
    <xsl:template match="/">
    <log:debug message="hello {$testvar} and attribute {./someelement/@someattribute}"/>
		<log:debug select="./someelement/@someattribute"/>
    </xsl:template>            
</xsl:stylesheet>

The following packages are part of SXE:

Assertion:   Provides a range of assertion like functionality for rock solid XSLT integration</td>
Charting:    Provides a way to create Bar, Line, Pie, Timeseries and many more graphs/charts from XML and have them output to either a SVG, PNG or JPG file.
Concurrency: Multithreaded XSLT processing
EJB:         A range of elements for looking up Stateless and Stateful Entity Beans</td>
Hibernate:   A range of elements for interacting with hiberante (loading, storing entities, running queries etc) and incorporating the hibernate entities into your XSLT.</td>
Http:        A range of elements for working with HTTP query strings and form submissions.</td>
JMS:         A range of elements for publish to, and subscribing to, java compliant messaging servers.</td>
JNDI:        A range of elements for reading things from, and binding things to, a JNDI tree.</td>
JPA:         A range of elements for interacting with EJB 3.0 JPA Entites and running EJBQL Queries and incorporating the entities into your XSLT.</td>
JavaBean:    Utilities for calling methods on java beans and incorporating the results into your XSLT processing as well as
Logging:     A logging framework for XSLT built ontop of SLF4j.</td>
Spring:      A way to reference spring beans from XSLT</td>
WebServices: A way to incorporate webservice calls into your XSLT processing</td>

