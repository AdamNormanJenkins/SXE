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
package net.adamjenkins.sxe.util;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.utils.QName;
import org.apache.xpath.Expression;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNull;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.adamjenkins.sxe.elements.concurrency.EmbeddedStylesheetDefinition;

/**
 * Utilities for working with Xalan extension functions and extension elements.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class XSLTUtil {
    
    private static final Logger log = LoggerFactory.getLogger(XSLTUtil.class);
    
    /**
     * Variable reference.
     */
    public static class VariableReference{
    	private boolean global;
    	private int index;
		public boolean isGlobal() {
			return global;
		}
		public void setGlobal(boolean global) {
			this.global = global;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
    	
    }
    
    public static XObject getXObject(final String xpathAttributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement){
        XPathContext xCtx = context.getTransformer().getXPathContext();    
        String selectExpressionString = null;
        boolean namespacePushed = false;
        boolean expressionPushed = false;
        try{           
            selectExpressionString = extensionElement.getAttribute(xpathAttributeName);           
            XPath xpath = new XPath(selectExpressionString, xCtx.getSAXLocator(), extensionElement, XPath.SELECT);

            xCtx.pushNamespaceContext(extensionElement);            
            namespacePushed = true;
            int current = xCtx.getCurrentNode();
            xCtx.pushCurrentNodeAndExpression(current, current);        
            expressionPushed=true;
            Expression expr = xpath.getExpression();
            return expr.execute(xCtx);  
        }catch(Throwable t){
            log.error("Error evaluating xpath attribute " + xpathAttributeName + " [" + selectExpressionString + "]", t);
            return null;
        }
        finally
        {            
            if(namespacePushed) xCtx.popNamespaceContext();
            if(expressionPushed) xCtx.popCurrentNodeAndExpression();         
        }          
    }

    public static String getXPath(final String xpathAttributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement){
        XPathContext xCtx = context.getTransformer().getXPathContext();    
        String selectExpressionString = null;
        boolean namespacePushed = false;
        boolean expressionPushed = false;
        try{           
            selectExpressionString = extensionElement.getAttribute(xpathAttributeName);           
            XPath xpath = new XPath(selectExpressionString, xCtx.getSAXLocator(), extensionElement, XPath.SELECT);        
            xCtx.pushNamespaceContext(extensionElement);            
            namespacePushed = true;
            int current = xCtx.getCurrentNode();
            xCtx.pushCurrentNodeAndExpression(current, current);        
            expressionPushed=true;
            Expression expr = xpath.getExpression();
            XObject obj = expr.execute(xCtx);  
            if(obj instanceof XNodeSet && ((XNodeSet)obj).getLength() > 1){
                XNodeSet ns = (XNodeSet)obj;                
                java.util.Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
                Serializer ser = SerializerFactory.getSerializer(props);
                StringWriter writer = new StringWriter();
                ser.setWriter(writer);
                DOMSerializer dser = ser.asDOMSerializer();                 
                for(int i = 0; i < ns.nodelist().getLength(); i++){
                    Node n = (Node)ns.nodelist().item(i);
                    dser.serialize(n);                                    
                }         
                return writer.toString();
            }else{
                return obj.toString();
            }
        }catch(Throwable t){
            StringBuilder errorStringBuilder = new StringBuilder();
            errorStringBuilder.append("Error evaluating xpath attribute ");
            errorStringBuilder.append(xpathAttributeName);
            errorStringBuilder.append(" on extension element ");
            errorStringBuilder.append(extensionElement.getNodeName());
            errorStringBuilder.append(" [");
            errorStringBuilder.append(selectExpressionString);
            errorStringBuilder.append("]");
            errorStringBuilder.append(" (line: ");
            errorStringBuilder.append(extensionElement.getLineNumber());
            errorStringBuilder.append(" column: ");
            errorStringBuilder.append(extensionElement.getColumnNumber());
            errorStringBuilder.append("): ");
            errorStringBuilder.append(t.getMessage());
            String errorString = errorStringBuilder.toString();
            log.error(errorString, t);            
            logError(XSLTUtil.class, errorString, context.getTransformer().getErrorListener());                        
            return null;
        }
        finally
        {            
            if(namespacePushed) xCtx.popNamespaceContext();
            if(expressionPushed) xCtx.popCurrentNodeAndExpression();         
        }        
    }
    
    public static String getAttribute(final String attributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement) {
        try{
            Node contextNode = context.getContextNode();
            return extensionElement.getAttribute(attributeName, contextNode, context.getTransformer());
        }catch(Throwable t){
            log.error("Error translating attribute " + attributeName, t);
            return null;
        }
    } 
    
    public static void logError(Class caller, String message, ErrorListener listener){
        logError(caller, null, message, listener);
    }
    
    public static void logError(Class caller, ElemExtensionCall elem, String message, ErrorListener listener){
        Logger localLog = LoggerFactory.getLogger(caller);
        try {
            if(elem == null && listener != null){
                 listener.error(                  
                        new TransformerException(message)
                );                    
            }else if(listener != null){
                 listener.error(                  
                        new TransformerException(
                            message, 
                            new ExtensionElementSourceLocator(elem)
                         )
                );
            }else{
                localLog.error(message);
            }
        } catch (TransformerException ex) {
            log.error("Could not register transformer exception with error listener.", ex);
        }        
    }

    public static void debug(Logger l, ElemExtensionCall extensionElement) {
        if (l.isDebugEnabled()) {
            l.debug("Element Call: " + extensionElement.getNodeName());
            NamedNodeMap map = extensionElement.getAttributes();
            for (int i = 0; i < map.getLength(); i++) {
                String attributeName = map.item(i).getLocalName();
                l.debug("Attribute " + attributeName + "=" + extensionElement.getAttribute(attributeName));
            }
            l.debug("********************");
        }
    }

    public static boolean hasAttribute(ElemExtensionCall elem, String attributeName) {
        String attrVal = elem.getAttribute(attributeName);
        return !StringUtils.isEmpty(attrVal);
    }

    public static boolean passesAttributeValidation(Class callerClass, ElemExtensionCall extensionElement, ErrorListener listener, String ... attributes){
        boolean passed = true;
        for(String s : attributes){
            if(!hasAttribute(extensionElement, s)){
                logError(callerClass, extensionElement, "Missing required attribute '" + s + "'", listener);
                passed = false;
            }
        }
        return passed;
    }

    public static boolean isNull(XObject obj){
        return (
                obj == null ||
                (obj instanceof XNull) ||
                ((obj instanceof XNodeSet) && ((XNodeSet) obj).getLength() == 0) ||
                obj.object() == null
               );
    }

    public static EmbeddedStylesheetDefinition createNewStylesheetFromChildren(XSLProcessorContext context, Node currentElement) throws ParserConfigurationException{
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document stylesheet = builder.newDocument();
        Set<String> params = new HashSet<String>();
        //TODO: BUILD THE STYLESHEET HERE
        return new EmbeddedStylesheetDefinition(stylesheet, params);
    }
    
}
