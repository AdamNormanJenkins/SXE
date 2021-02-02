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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Element for easily working with java objects.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE JavaBean Framework</h3>
 * These are some elements for interacting with java beans, i.e. binding data, getting/setting parameters and
 * invoking methods.  It is most often used in conjunction with the EJB, Hibernate or Spring extension elements to
 * interact with a larger system.  See the element declarations below for more information.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:bean="xalan://net.adamjenkins.sxe.elements.JavaBean" extension-element-prefixes="bean" ... &gt;
 * </code>
 * <br/><br/>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class JavaBean extends AbstractExtensionElement{

    private HashMap<Thread, List<Object>> parameterCapture = new HashMap<Thread, List<Object>>();

    /**
     * Instantiates a java object.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="myobject"&gt;
     *  &lt;bean:instantiate class="com.something.SomeClass"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="myobject"&gt;
     *  &lt;bean:instantiate class="com.something.SomeClass"&gt;
     *      &lt;bean:parameter select="@id"&gt;
     *  &lt;/bean:instantiate&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>class</td><td>Template</td><td>The class to create a new instance of.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void instantiate(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, FileNotFoundException, ClassNotFoundException, MalformedURLException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "class")) return;
        String className = getAttribute("class", context, extensionElement);
        try{
            parameterCapture.put(Thread.currentThread(), new ArrayList<Object>());
            context.getTransformer().executeChildTemplates(extensionElement, true);
            int expected = countParams(extensionElement);
            int actual = parameterCapture.get(Thread.currentThread()).size();
            if(expected != actual){
                logError(this.getClass(),
                        extensionElement,
                        "Problem with parameters, invalid number of parameters interpreted",
                        context);
                return;
            }
            Object newObject = ConstructorUtils.invokeConstructor(Class.forName(className), parameterCapture.get(Thread.currentThread()).toArray());
            if(!setVariableIfPossible(newObject, extensionElement)) context.outputToResultTree(context.getStylesheet(), newObject);
        }finally{
            parameterCapture.remove(Thread.currentThread());
        }
    }

    /**
     * Invokes a method on a java bean, and adds the result (if anything is returned from the method) to the output tree.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;xsl:variable name="username"&gt;
     *  &lt;bean:invoke target="$myBean" method="getUserName"&gt;
     *      &lt;parameter select="@id"/&gt;
     *  &lt;/bean:invoke&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>target</td><td>XPath</td><td>The target object.</td><td>Yes</td></tr>
     *  <tr><td>method</td><td>Template</td><td>The method to call.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void invoke(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "target", "method")) return;
        XObject target = getXObject("target", context, extensionElement);
        String methodName = getAttribute("method", context, extensionElement);
        if(isNull(target)){
            logError(this.getClass(),
                    extensionElement,
                    "Target cannot evaluate to null",
                    context);
            return;
        }
        Object bean = target.object();
        Object result = null;
        try{
            parameterCapture.put(Thread.currentThread(), new ArrayList<Object>());
            context.getTransformer().executeChildTemplates(extensionElement, true);
            int expected = countParams(extensionElement);
            int actual = parameterCapture.get(Thread.currentThread()).size();
            if(expected != actual){
                logError(this.getClass(),
                        extensionElement,
                        "Problem with parameters, invalid number of parameters interpreted",
                        context);
                return;
            }
            result = MethodUtils.invokeMethod(bean, methodName, parameterCapture.get(Thread.currentThread()).toArray());
        }finally{
            parameterCapture.remove(Thread.currentThread());
        }
        if(result != null) {
            setVariableOrWriteToOutput(result, context, extensionElement);
        }
    }

    /**
     * Supplies parameters to an invoke element.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;xsl:variable name="username"&gt;
     *  &lt;bean:invoke target="$myBean" method="getUserName"&gt;
     *      &lt;bean:parameter select="@id"/&gt;
     *      &lt;bean:parameter select="/location"/&gt;
     *  &lt;/bean:invoke&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>select</td><td>XPath</td><td>The value of the parameter.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void parameter(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "select")) return;
        XObject obj = getXObject("select", context, extensionElement);
        List<Object> params = parameterCapture.get(Thread.currentThread());
        if(isNull(obj)){
            params.add(null);
        }else{
            params.add(obj.object());
        }
    }

     /**
     * Sets a property on a java object.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;bean:setProperty target="$myObject" property="username" value="/someelement/@someAttribute"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>target</td><td>XPath</td><td>The object to get the property from.</td><td>Yes</td></tr>
     *  <tr><td>property</td><td>Template</td><td>The property to read.</td><td>Yes</td></tr>
      * <tr><td>value</td><td>XPath</td><td>The value to set the property to.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void setProperty(XSLProcessorContext context, ElemExtensionCall extensionElement) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "target", "property", "value")) return;
        Object target = getXObject("target", context, extensionElement).object();
        String propertyName = getAttribute("property", context, extensionElement);
        Object value = getXObject("value", context, extensionElement).object();
        PropertyUtils.setProperty(target, propertyName, value);
    }

     /**
     * Gets a property from a java object.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;xsl:variable name="username"&gt;
     *  &lt;bean:getProperty target="$myObject" property="username"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>target</td><td>XPath</td><td>The object to get the property from.</td><td>Yes</td></tr>
     *  <tr><td>property</td><td>Template</td><td>The property to read.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void getProperty(XSLProcessorContext context, ElemExtensionCall extensionElement) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "target", "property")) return;
        Object target = getXObject("target", context, extensionElement).object();
        String propertyName = getAttribute("property", context, extensionElement);
        Object returnValue = PropertyUtils.getProperty(target, propertyName);
        if(!setVariableIfPossible(returnValue, extensionElement)){
            context.outputToResultTree(context.getStylesheet(), returnValue.toString());
        }
    }

     /**
     * Populates an objects properties using the values of the attributes.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>
     * <pre>
     *  &lt;bean:bind target="$customer" firstName="contact/@first-name" lastName="contact/@last-name" address="address/text()"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>target</td><td>XPath</td><td>The object to bind the parameters to.</td><td>Yes</td></tr>
     * </table>
     * <b>Note:</b> All other attributes will be evaluated as <i>XPath</i> attributes and the values will be set to the
     * property that corresponds to the attribute name (e.g. firstName="contact/@first-name" will take the value from the
     * <code>first-name</code> attribute on the <code>contact</code> element and call <code>setFirstName</code> on the target
     * object, passing in the value).
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void bind(XSLProcessorContext context, ElemExtensionCall extensionElement) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "target")) return;
        Object target = getXObject("target", context, extensionElement).object();
        NamedNodeMap attributes = extensionElement.getAttributes();
        for(int i= 0; i < attributes.getLength(); i++){
            Attr attr = (Attr)attributes.item(i);
            if(attr.getName().equals("target")) continue;
            Object value = getXObject(attr.getName(), context, extensionElement).object();
            PropertyUtils.setProperty(target, attr.getName(), value);
        }
    }

    private int countParams(ElemExtensionCall elem){
        return countChildElementsOfType(elem, "xalan://net.adamjenkins.sxe.elements.JavaBean", "parameter");
    }

}
