/*
 * Copyright 2020 Adam Norman Jenkins.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package net.adamjenkins.sxe.elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xpath.objects.XObject;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.portlet.context.XmlPortletApplicationContext;

import net.adamjenkins.sxe.util.XSLTUtil;

/**
 * Element for loading spring beans into an XSLT stylesheet.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Spring Framework</h3>
 * This framework provides elements for configuring a spring context and loading spring beans that can then be
 * manipulated and have methods invoked on them using the {@link net.adamjenkins.sxe.elements.JavaBean} elements.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:spring="xalan://net.adamjenkins.sxe.elements.Spring" extension-element-prefixes="spring" ... &gt;
 * </code>
 * <br/><br/>
 * Example Usage:
 * <br/><br/>
 * <code><pre>
 * &lt:xsl:variable name="mybean"&gt;
 *  &lt;spring:bean id="myspringid"/&gt;
 * &lt;/xsl:variable&gt;
 * </pre></code>
 * Example Usage:
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a.
 */
public class Spring extends AbstractExtensionElement{

    private enum Base { FILE, CLASSPATH, WEBAPP, PORTLET }

    private List<String> resources;

    private AbstractApplicationContext defaultContext;

    /**
     * Configures a spring context.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="context"&gt;
     *  &lt;spring:context base="CLASSPATH"&gt;
     *      &lt;spring:resource location="com/blah/someresource.xml"/&gt;
     *  &lt;/spring:context&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <b>Note:</b> If the context is not placed within a <code>xsl:variable</code> tag, then the default context will be set.
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>base</td><td>Template</td><td>The type of context to configure.  Either FILE, CLASSPATH, PORTLET or WEBAPP (case sensitive).</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void context(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "base")) throw new TransformerException("Missing attribute 'base'");
        resources = new ArrayList<String>();
        context.getTransformer().executeChildTemplates(extensionElement, true);
        Base base = Base.valueOf(getAttribute("base", context, extensionElement));
        AbstractApplicationContext springContext;
        switch(base){
            case FILE:
                springContext = new FileSystemXmlApplicationContext(resources.toArray(new String[0]));
                break;
            case WEBAPP:
                springContext = new XmlWebApplicationContext();
                break;
            case PORTLET:
                springContext = new XmlPortletApplicationContext();
                break;
            case CLASSPATH:
            default:
                springContext = new ClassPathXmlApplicationContext(resources.toArray(new String[0]));
                break;
        }
        //if the parent is a variable element then return the value, otherwise set the default
        //context and return null
        if(extensionElement.getParentElem() instanceof ElemVariable) {
        	XSLTUtil.setVariable(context, extensionElement, springContext);
        }else {
        	defaultContext = springContext;        	
        }
    }

    /**
     * A spring resource.  This tag must be placed within a context tag.
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="context"&gt;
     *  &lt;spring:context base="CLASSPATH"&gt;
     *      &lt;spring:resource location="com/blah/someresource.xml"/&gt;
     *  &lt;/spring:context&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>location</td><td>Template</td><td>The location of the spring resource.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void resource(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(!hasAttribute(extensionElement, "location")) return;
        if(resources == null) {
            logError(this.getClass(),
                    extensionElement,
                    "<spring:resource> tag must be inside a <spring:context> tag",
                    context);
        }
        resources.add(getAttribute("location", context, extensionElement));
    }

    /**
     * Loads a spring bean.
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mybean"&gt;
     *  &lt;spring:bean id="myBean" context="$myContext"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>id</td><td>Template</td><td>The id of the bean to load.</td><td>Yes</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The spring context.</td><td>No, if not the default context will be used.</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void bean(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "id")) return;
        AbstractApplicationContext ctx;
        if(hasAttribute(extensionElement, "context")){
        	XObject xObj = XSLTUtil.getXObject("context", context, extensionElement);
            ctx = (AbstractApplicationContext)xObj.object();
        }else{
            if(defaultContext == null){
                logError(this.getClass(),
                        extensionElement,
                        "No spring context configured",
                        context);
                return;
            }else{
                ctx = defaultContext;
            }
        }
        Object bean = ctx.getBean(getAttribute("id", context, extensionElement));
        if(setVariableIfPossible(context.getTransformer(), bean, extensionElement)) return;
        else{
            context.outputToResultTree(context.getStylesheet(), bean);
        }
    }

}
