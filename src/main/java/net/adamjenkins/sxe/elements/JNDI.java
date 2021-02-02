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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;

/**
 * Elements for working with a JNDI server.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE JNDI Framework</h3>
 * To register the JMS elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:jndi="xalan://net.adamjenkins.sxe.elements.JNDI" extension-element-prefixes="jndi" ... &gt;
 * </code>
 * <br/><br/>
 *
 * Usage:
 * <br/><br/>
 * <code><pre>
 * &lt;xsl:variable name="mycontext"&gt;
 *     &lt;jndi:context&gt;
 *         &lt;property name="java.naming.security.principal" value="bob"/&gt;
 *         &lt;property name="java.naming.security.credentials" value="bob"/&gt;
 *         &lt;property name="java.naming.provider.url" value="{$someUrlVariable}"/&gt;
 *     &lt;/jndi:context&gt;
 * $lt;/xsl:variable&gt;
 * </pre></code>
 * <br/><br/>
 * <code><pre>
 * &lt;xsl:variable name="mailSession"&gt;
 *     &lt;jndi:lookup ref="mail/mySession"/&gt;
 * $lt;/xsl:variable&gt;
 * </pre></code>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class JNDI extends AbstractExtensionElement{

    private Context defaultContext;

    private Map<Thread, Properties> propertyCapture = new HashMap<Thread, Properties>();

    /**
     * Configures a new context for use.  The context will be placed on the top of the variable stack, so you
     * must use this in conjunction with a XSLT variable or with-param or similar element.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mycontext"&gt;
     *     &lt;jndi:context&gt;
     *         &lt;property name="java.naming.security.principal" value="bob"/&gt;
     *         &lt;property name="java.naming.security.credentials" value="bob"/&gt;
     *         &lt;property name="java.naming.provider.url" value="{$someUrlVariable}"/&gt;
     *     &lt;/jndi:context&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * If you want to use the default initial context, simply declare it with no properties
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mycontext"&gt;
     *     &lt;jndi:context/&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * <b>Note:</b> If this is not called within a variable tag, the default context will be set.
     * @param context               The XSLT Processor context
     * @param extensionElement      This element
     */
    public void context(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        propertyCapture.put(Thread.currentThread(), new Properties());
        context.getTransformer().executeChildTemplates(extensionElement, true);
        Properties configuration = propertyCapture.remove(Thread.currentThread());
        InitialContext ctx;
        if(configuration.size() > 0) ctx = new InitialContext(configuration);
        else ctx = new InitialContext();
        if(!setVariableIfPossible(ctx, extensionElement)){
            defaultContext = ctx;
        }
    }

    /**
     * Gets the currently configured default context.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mycontext"&gt;
     *     &lt;jndi:defaultContext/&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * @throws TransformerException 
     */
    public void defaultContext(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException{
        setVariableIfPossible(getInitialContext(context, extensionElement), extensionElement);
    }

    /**
     * A way to supply properties to a context creation.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mycontext"&gt;
     *     &lt;jndi:context&gt;
     *         &lt;property name="java.naming.security.principal" value="bob"/&gt;
     *         &lt;property name="java.naming.security.credentials" value="bob"/&gt;
     *         &lt;property name="java.naming.provider.url" value="{$someUrlVariable}"/&gt;
     *     &lt;/jndi:context&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * @param context               The XSLT Processor context
     * @param extensionElement      This element
     */
    public void property(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(!passesAttributeValidation(extensionElement, context, "name", "value")) return;
        propertyCapture.get(Thread.currentThread()).setProperty(
                getAttribute("name", context, extensionElement),
                getAttribute("value", context, extensionElement)
        );
    }

    /**
     * Looks up an object from JNDI.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mailSession"&gt;
     *     &lt;jndi:lookup ref="mail/mySession"/&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * You can either supply a context, or use the default one.
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="mailSession"&gt;
     *     &lt;jndi:lookup ref="mail/mySession" context="$context"/&gt;
     * $lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>ref</td><td>Template</td><td>The JNDI reference to lookup.</td><td>Yes</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The context to use.</td><td>No (if not supplied, the default context will be used)</td></tr>
     * </table>
     * @param context               The XSLT Processor context
     * @param extensionElement      This element
     */
    public void lookup(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "ref")) return;
        Context ctx = getInitialContext(context, extensionElement);
        Object value = ctx.lookup(getAttribute("ref", context, extensionElement));
        setVariableOrWriteToOutput(value, context, extensionElement);
    }

    /**
     * Creates a subcontext from a context.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     *  &lt;xsl:variable name="jdbcContext"&gt;
     *     &lt;jndi:subcontext ref="env/jdbc" context="$myContext"/&gt;
     *  &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * You can either supply a context, or use the default one.
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>ref</td><td>Template</td><td>The JNDI reference to the sub context.</td><td>Yes</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The context to use.</td><td>No (if not supplied, the default context will be used)</td></tr>
     * </table>
     * <b>Note:</b> If you do not put this inside a <code>&lt;xsl:variable&gt;</code>, the default context will be overridden.
     * @param context               The XSLT Processor context
     * @param extensionElement      This element
     * @throws TransformerException 
     */
    public void subcontext(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "ref")) return;
        Context ctx = getInitialContext(context, extensionElement);
        Context newContext = ctx.createSubcontext(getAttribute("ref", context, extensionElement));
        if(!setVariableIfPossible(newContext, extensionElement)){
            defaultContext = newContext;
        }
    }

    /**
     * Binds an object into JNDI.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     *     &lt;jndi:lookup ref="env/myvar" value="$myObject" context="$myContext"/&gt;
     * </pre></code>
     * <br/><br/>
     * You can either supply a context, or use the default one.
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>ref</td><td>Template</td><td>The JNDI reference to bind the object to.</td><td>Yes</td></tr>
     *  <tr><td>value</td><td>XPath</td><td>The value to bind.</td><td>Yes</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The context to use.</td><td>No (if not supplied, the default context will be used)</td></tr>
     * </table>
     * @param context               The XSLT Processor context
     * @param extensionElement      This element
     * @throws TransformerException 
     */
    public void bind(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "ref", "value")) return;
        Context ctx = getInitialContext(context, extensionElement);
        ctx.rebind(
                getAttribute("ref", context, extensionElement),
                getXObject("value", context, extensionElement).object()
        );
    }

    private Context getContext(XSLProcessorContext context, ElemExtensionCall element) throws NamingException, TransformerException{
        if(hasAttribute(element, "context")){
            return (Context)getXObject("context", context, element).object();
        }
        if(defaultContext != null) return defaultContext;
        else return new InitialContext();
    }

}
