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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.VarBridge;
import org.apache.xalan.trace.EndSelectionEvent;
import org.apache.xalan.trace.ExtensionEvent;
import org.apache.xalan.trace.GenerateEvent;
import org.apache.xalan.trace.SelectionEvent;
import org.apache.xalan.trace.TraceListenerEx3;
import org.apache.xalan.trace.TracerEvent;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.adamjenkins.sxe.util.XSLTErrorListener;
import net.adamjenkins.sxe.util.XSLTUtil;

/**
 * Abstract superclass of all SXE extension elements.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public abstract class AbstractExtensionElement implements TraceListenerEx3{

    private static final Logger log = LoggerFactory.getLogger(AbstractExtensionElement.class);

    private Map<Thread, ErrorListener> listenerHolder = new HashMap<Thread, ErrorListener>();

    public AbstractExtensionElement(){
        
    }

    protected XSLTErrorListener resetErrorListener(XSLProcessorContext context){
        XSLTErrorListener returnValue = (XSLTErrorListener)context.getTransformer().getErrorListener();
        context.getTransformer().setErrorListener(listenerHolder.get(Thread.currentThread()));
        listenerHolder.put(Thread.currentThread(), null);
        return returnValue;
    }

    protected void swapErrorListener(XSLProcessorContext context){
        listenerHolder.put(Thread.currentThread(), context.getTransformer().getErrorListener());
        context.getTransformer().setErrorListener(new XSLTErrorListener());
    }

    protected String processBody(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, ParserConfigurationException, IOException{
        Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty("omit-xml-declaration", "yes");
        Serializer ser = SerializerFactory.getSerializer(props);
        StringWriter writer = new StringWriter();
        ser.setWriter(writer);
        context.getTransformer().executeChildTemplates(extensionElement, ser.asContentHandler());
        return writer.toString();
    }

    public void trace(TracerEvent event) {
        
    }

    public void traceEnd(TracerEvent event) {
    }

    public void selected(SelectionEvent event) throws TransformerException {
    }

    public void generated(GenerateEvent event) {
    }

    public void extension(ExtensionEvent event) {
    }

    public void extensionEnd(ExtensionEvent event) {
    }

    public void selectEnd(EndSelectionEvent event) throws TransformerException {
    }

    protected boolean hasAttribute(ElemExtensionCall elem, String attributeName) {
        return XSLTUtil.hasAttribute(elem, attributeName);
    }

    protected XObject getXObject(final String xpathAttributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement) throws TransformerException{
        return XSLTUtil.getXObject(xpathAttributeName, context, extensionElement);
    }

    protected String getXPath(final String xpathAttributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement){
        return XSLTUtil.getXPath(xpathAttributeName, context, extensionElement);
    }

    protected String getAttribute(final String attributeName, final XSLProcessorContext context, final ElemExtensionCall extensionElement) {
        return XSLTUtil.getAttribute(attributeName, context, extensionElement);
    }

    protected void logError(TransformerException ex, ErrorListener listener){
        try{
            listener.error(ex);
        } catch (Throwable t) {
            log.error("Could not notify the transformer about errors.", t);
        }
    }

    protected void logError(TransformerException ex, XSLProcessorContext context) {
        logError(ex, context.getTransformer().getErrorListener());
    }

    protected void logError(Class caller, String message, XSLProcessorContext context){
        logError(caller, message, context.getTransformer().getErrorListener());
    }

    protected void logError(Class caller, String message, ErrorListener listener){
        logError(caller, null, message, listener);
    }

    protected void logError(Class caller, ElemExtensionCall elem, String message, XSLProcessorContext context){
        logError(caller, elem, message, context.getTransformer().getErrorListener());
    }

    protected void logError(Class caller, ElemExtensionCall elem, String message, ErrorListener listener){
        XSLTUtil.logError(caller, elem, message, listener);
    }

    protected void debug(ElemExtensionCall extensionElement) {
        XSLTUtil.debug(log, extensionElement);
    }

    protected boolean passesAttributeValidation(ElemExtensionCall extensionElement, XSLProcessorContext context, String ... attributes){
        return XSLTUtil.passesAttributeValidation(this.getClass(), extensionElement, context.getTransformer().getErrorListener(), attributes);
    }

    protected boolean isNull(XObject obj){
        return XSLTUtil.isNull(obj);
    }

    protected boolean setVariableIfPossible(TransformerImpl transformer, Object variable, ElemExtensionCall thisElement) throws TransformerException{
        if(thisElement.getParentElem() instanceof ElemVariable){
            ElemVariable varElement = (ElemVariable)thisElement.getParentElem();
            //int sourceNode = transformer.getXPathContext().getCurrentNode();
            //XObject tmp = varElement.getValue(transformer, 0);
            //get the current variable
            
            //transformer.getXPathContext().getVarStack().setLocalVariable(varElement.getIndex(), new XObject(variable));
            
            varElement.removeChild(thisElement);
            varElement.setSelect(new XPath(new XObject(variable)));
            
            //varElement.execute(transformer);
            /*StylesheetRoot root = varElement.getStylesheetRoot();
            ElemVariable newVar = new ElemVariable();
            newVar.setSelect(new XPath(new XObject(variable)));
            newVar.setParentElem(varElement.getParentElem());
            varElement.getParentElem().appendChild(newVar);
            varElement.getParentElem().removeChild(varElement);
            newVar.recompose(root);
            newVar.execute(transformer);*/
            //varElement.recompose(varElement.getStylesheetRoot());
            //varElement.appendChild(new XPath(XObject(variable)));
            //varElement.overrideChildDocument(new XPath(new XObject(variable)));
            return true;
        }
        return false;
    }

    protected int getIntegerXPath(String attributeName, XSLProcessorContext context, ElemExtensionCall extensionElement, int defaultValue){
        if(!hasAttribute(extensionElement, attributeName)) return defaultValue;
        String evalString = getXPath(attributeName, context, extensionElement);
        try{
            return Integer.parseInt(evalString);
        }catch(NumberFormatException e){
            logError(this.getClass(), extensionElement, 
                    "Attribute " + attributeName + " must evaluate to a valid integer ('" +
                    extensionElement.getAttribute(attributeName) + "' evaluated to '" + evalString + "')",
                    context);
            return defaultValue;
        }
    }

    protected InitialContext getInitialContext(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException{
        if(hasAttribute(extensionElement, "context")){
            XObject ctx = getXObject("context", context, extensionElement);
            InitialContext resolvedContext;
            if(ctx.object() instanceof InitialContext){
                resolvedContext = (InitialContext)ctx.object();
                return resolvedContext;
            }else{
                logError(this.getClass(), extensionElement, "'context' attribute must resolve to a previously configured <jndi:context/> object", context);
                return null;
            }
        }else return new InitialContext();
    }

    protected int countChildElementsOfType(ElemExtensionCall elem, String namespace, String name){
        NodeList list = elem.getChildNodes();
        int count = 0;
        for(int i = 0; i < list.getLength(); i++){
            Node n = list.item(i);
            if(n instanceof ElemExtensionCall){
                ElemExtensionCall call = (ElemExtensionCall)n;
                if(namespace.equals(call.getNamespace()) &&
                        name.equals(n.getLocalName())) count++;
            }
        }
        return count;
    }

    protected void setVariableOrWriteToOutput(Object value, XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, IOException{
        if(!setVariableIfPossible(context.getTransformer(), value, extensionElement)){
            context.outputToResultTree(context.getStylesheet(), value);
        }
    }

}
