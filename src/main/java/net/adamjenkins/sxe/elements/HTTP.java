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

import static net.adamjenkins.sxe.util.XSLTUtil.logError;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.UnImplNode;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.WebClient;

import net.adamjenkins.sxe.util.XMLUtils;
import net.adamjenkins.sxe.util.XSLTUtil;

/**
 * The http elements are a group of elements to make working with http and urls easier.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Http Framework</h3>
 *
 * <br/><br/>
 * A range of http utility extensions.
 * 
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:http="xalan://net.adamjenkins.sxe.elements.HTTP" extension-element-prefixes="http" ... &gt;
 * </code>
 * <br/><br/>
 *
 * <h3>DO NOT USE THIS ELEMENT -- UNDER CONSTRUCTION</h3>
 * 
 * To use the query string element:
 * <br/><br/>
 * <code><pre>
 * &lt;xsl:variable name="queryString"&gt;
 *    &lt;http:queryString&gt;       
 *       &lt;param name="Language" value="en"/&gt;
 *       &lt;param name="lLocationID" value="$myvar"/&gt;
 *       &lt;param name="lPage" value="3"/&gt;
 *     &lt;/http:queryString>
 * &lt;/xsl:variable&gt;
 * </pre></code>
 * <br/><br/>
 * You can then access your query string anywhere using a standard xslt variable reference:
 * <br/><br/>
 * <code>&lt;xsl:value-of select="$queryString"/&gt;</code>
 * <br/><br/>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class HTTP {
    
    private static final Logger log = LoggerFactory.getLogger(HTTP.class);
    
    public void input(XSLProcessorContext context, final ElemExtensionCall extensionElement){
        try{
            context.outputToResultTree(context.getStylesheet(), extensionElement);
        }catch(Exception e){
            logError(this.getClass(), "Error overriding input. " + e.getMessage(), context.getTransformer().getErrorListener());            
        }            
    }    
    
    public void formSubmit(XSLProcessorContext context, final ElemExtensionCall extensionElement){
        try{
            log.debug("performing form submit from xslt");
            NodeList inputOverrides = extensionElement.getChildNodes();
            XNodeSet formNode = (XNodeSet)XSLTUtil.getXObject("form", context, extensionElement);
            NodeList formNodeList = formNode.nodelist();
            Node fn = formNodeList.item(0);            
            XObject client = XSLTUtil.getXObject("client", context, extensionElement);
            WebClient httpClient = (WebClient)client.object();
            Node actionAttr = fn.getAttributes().getNamedItem("action");
            if(actionAttr == null){
                //we can't do a thing without an action attribute
                //sometimes forms have no acction attribute if they're being submitted by
                //javascript -- we can't do anything in this case.
                logError(this.getClass(), "Form has no 'action' attribute (line: " + extensionElement.getLineNumber() + " column: " + extensionElement.getColumnNumber() + ")", context.getTransformer().getErrorListener());                            
            }
            Node methodAttr = fn.getAttributes().getNamedItem("method");
            String action = actionAttr.getNodeValue();
            String method = methodAttr != null ? methodAttr.getNodeValue() : "get";
            if(log.isDebugEnabled()){
                log.debug("Form submission: action: "+ action + " method: " + method);
            }
            XNodeSet inputNodes = (XNodeSet)XSLTUtil.getXObject("input", context, extensionElement);
            HashMap<String,String> input = new HashMap<String,String>();
            if(inputNodes != null){                
                NodeList inputNodeList = inputNodes.nodelist();
                for(int i = 0; i < inputNodeList.getLength(); i++){
                    Node n = inputNodeList.item(i);
                    String name = n.getAttributes().getNamedItem("name").getNodeValue();
                    String value = n.getAttributes().getNamedItem("value").getNodeValue();
                    if(log.isDebugEnabled()){
                        log.debug("Base Input: " + name + "=" + value);
                    }
                    input.put(name, value);
                }
            }
            Set<String> toExclude = new HashSet<String>();
            //process the overrides
            for(int i = 0; i < inputOverrides.getLength(); i++){
                Node n = inputOverrides.item(i);
                String s = n.getNodeName();
                if(n.getNodeName().equals("http:input")){      
                    ElemExtensionCall override = (ElemExtensionCall)n;
                    String name = override.getAttribute("name");
                    String selectString = override.getAttribute("select");
                    String value;
                    if(selectString != null && selectString.length() > 0){
                        value = XSLTUtil.getXPath("select", context, override);
                    }else{
                        value = "";
                    }
                    if(log.isDebugEnabled()){
                        log.debug("Input Override: " + name + "=" + value);
                    }
                    input.put(name, value);
                }else if(n.getNodeName().equals("http:exclude")){
                    ElemExtensionCall override = (ElemExtensionCall)n;
                    String name = override.getAttribute("name");
                    if(name != null){
                        toExclude.add(name);
                    }else{
                        String selectString = override.getAttribute("select");                    
                        if(selectString != null){
                            XObject value = XSLTUtil.getXObject("select", context, override);
                            if(value instanceof XNodeSet){
                                XNodeSet excludeNodeSet = (XNodeSet)value;
                                NodeList excludeNodeList = excludeNodeSet.nodelist();
                                for(int j = 0; j < excludeNodeList.getLength(); j++){
                                    toExclude.add(excludeNodeList.item(j).getNodeValue());
                                }
                            }else{
                                toExclude.add(value.toString());
                            }
                        }
                    }
                }
            }
            //figure out the url
            URL base = new URL(XSLTUtil.getXPath("base", context, extensionElement).toString());
            URL newUrl = new URL(base, action);
            if(log.isDebugEnabled()){
                log.debug("Form submission to " + newUrl.toExternalForm());
            }
            if(method.equalsIgnoreCase("get")){
                context.outputToResultTree(context.getStylesheet(), XMLUtils.getTidyXHTMLFromFormGet(httpClient, newUrl.toExternalForm(), input));
            }else{
                context.outputToResultTree(context.getStylesheet(), XMLUtils.getTidyXHTMLFromFormPost(httpClient, newUrl.toExternalForm(), input));
            }
        }catch(Exception e){
            logError(this.getClass(), "Error in form submission. " + e.getMessage(), context.getTransformer().getErrorListener());            
        }
    }
    
    public void queryString(XSLProcessorContext context, final ElemExtensionCall extensionElement) {        
        StringBuilder builder = new StringBuilder("?");
        NodeList params = extensionElement.getChildNodes();
        for (int i = 0; i < params.getLength(); i++) {
            UnImplNode child = (UnImplNode) params.item(i);
            if ("param".equals(child.getNodeName())) {
                String attrName = child.getAttribute("name");
                String attrValue = child.getAttribute("value");
                String name;
                String value;
                if (attrName.startsWith("$") && !attrName.startsWith("${")) {
                    try {
                        name = context.getTransformer().getXPathContext().getVarStack().getVariableOrParam(context.getTransformer().getXPathContext(), new QName(attrName.substring(1))).str();
                    } catch (Throwable t) {
                        logError(this.getClass(), "Error looking up param name '" + attrName + "': " + t.getMessage(), context.getTransformer().getErrorListener());
                        name = attrName;
                    }
                } else {
                    name = attrName;
                }
                if (attrValue.startsWith("$") && !attrValue.startsWith("${")) {
                    try {
                        value = context.getTransformer().getXPathContext().getVarStack().getVariableOrParam(context.getTransformer().getXPathContext(), new QName(attrValue.substring(1))).str();
                    } catch (Throwable t) {
                        logError(this.getClass(), "Error looking up param value '" + attrValue + "': " + t.getMessage(), context.getTransformer().getErrorListener());
                        value = attrValue;
                    }
                } else {
                    value = attrValue;
                }
                builder.append(name);
                builder.append('=');
                builder.append(value);
                builder.append('&');
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        try{
            context.outputToResultTree(context.getStylesheet(), builder.toString());
        } catch (Exception e){
            logError(this.getClass(), "Error writing query string to output. " + e.getMessage(), context.getTransformer().getErrorListener());
        }
    }    

}
