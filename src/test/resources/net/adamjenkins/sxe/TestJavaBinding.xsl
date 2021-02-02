<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mock="http://xml.apache.org/xalan/java/net.adamjenkins.sxe.bean.MockBean"
    xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean"
    extension-element-prefixes="bean"
    exclude-result-prefixes="mock">

  <xsl:template match="/">
   <xsl:variable name="mockBean" select="mock:new('hello world', true, 1.1)"/>
   <xsl:variable name="mockBean2" select="mock:new('hello world2', false, 2.2)"/>
   ToString: <xsl:value-of select="$mockBean"/>
   Text: <xsl:value-of select="mock:getText($mockBean)"/>
   ToString: <xsl:value-of select="$mockBean2"/>
   Text: <xsl:value-of select="mock:getText($mockBean2)"/>
    <xsl:variable name="mockBean3">
        <bean:instantiate class="net.adamjenkins.sxe.bean.MockBean">
            <bean:parameter select="'text'"/>
            <bean:parameter select="true()"/>
            <bean:parameter select="1.1"/>
        </bean:instantiate>
    </xsl:variable>
    <bean:getProperty target="$mockBean3" property="text"/>
    <bean:getProperty target="$mockBean3" property="bool"/>
    <bean:getProperty target="$mockBean3" property="decimal"/>
  </xsl:template>

</xsl:stylesheet>