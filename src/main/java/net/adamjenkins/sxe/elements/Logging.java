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

import java.io.Writer;
import java.util.HashMap;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Element for logging (built on <a href="http://www.slf4j.org">SLF4j</a>).
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Logging Framework</h3>
 * These are some elements for providing logging information to a java system from your XSLT.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:log="xalan://net.adamjenkins.sxe.elements.Logging" extension-element-prefixes="log" ... &gt;
 * </code>
 * <br/><br/>
 * 
 * You may then, in your xslt document, add the following when you want messages to be logged
 * <br/><br/>
 * <code><pre>
 * &lt;log:trace message="some message"/&gt; or &lt;log:trace select="some xpath"/&gt;
 * &lt;log:debug message="some message"/&gt; or &lt;log:debug select="some xpath"/&gt;
 * &lt;log:info message="some message"/&gt; or &lt;log:info select="some xpath"/&gt;
 * &lt;log:warn message="some message"/&gt; or &lt;log:warn select="some xpath"/&gt;
 * &lt;log:error message="some message"/&gt; or &lt;log:error select="some xpath"/&gt;
 * </pre></code>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Logging extends AbstractExtensionElement{

    private static final Logger staticLog = LoggerFactory.getLogger(Logging.class);
    
    public enum Level {TRACE, DEBUG, INFO, WARN, ERROR}        
    
    private static final HashMap<Long,Level> outputLevels =
            new HashMap<Long,Level>();
    
    private static final HashMap<Long,Writer> outputChannel =
            new HashMap<Long,Writer>();
    
    
    /** start web ui method **/
    public static final void pushConfiguration(Writer writer, Level level){
        outputLevels.put(Thread.currentThread().getId(), level);
        outputChannel.put(Thread.currentThread().getId(), writer);
    }
    
    public static final void popConfiguration(){
        outputLevels.remove(Thread.currentThread().getId());
        outputChannel.remove(Thread.currentThread().getId());
    }
    /** end web ui methods **/
    

    /**
     * Sends a TRACE message to your underlying logging system.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;log:trace message="some message"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>A message to send.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>An XPath to evaluate and send the result to the logging framework.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>category</td><td>Template</td><td>A optional logging category.</td><td>No.</td></tr>
     * </table>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void trace(XSLProcessorContext context, ElemExtensionCall extensionElement){
        log(context, extensionElement, Level.TRACE);
    }

    /**
     * Sends a DEBUG message to your underlying logging system.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;log:debug message="some message"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>A message to send.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>An XPath to evaluate and send the result to the logging framework.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>category</td><td>Template</td><td>A optional logging category.</td><td>No.</td></tr>
     * </table>
     * <br/><br/>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void debug(XSLProcessorContext context, ElemExtensionCall extensionElement){
        log(context, extensionElement, Level.DEBUG);
    }

    /**
     * Sends a INFO message to your underlying logging system.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;log:info message="some message"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>A message to send.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>An XPath to evaluate and send the result to the logging framework.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>category</td><td>Template</td><td>A optional logging category.</td><td>No.</td></tr>
     * </table>
     * <br/><br/>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void info(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        log(context, extensionElement, Level.INFO);
    }    

    /**
     * Sends a WARN message to your underlying logging system.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;log:warn message="some message"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>A message to send.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>An XPath to evaluate and send the result to the logging framework.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>category</td><td>Template</td><td>A optional logging category.</td><td>No.</td></tr>
     * </table>
     * <br/><br/>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void warn(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        log(context, extensionElement, Level.WARN);
    }  

    /**
     * Sends a ERROR message to your underlying logging system.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;log:error message="some message"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>A message to send.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>An XPath to evaluate and send the result to the logging framework.</td><td>No (you must have either a <i>message</i> attribute or a <i>select</i> attribute.</td></tr>
     *  <tr><td>category</td><td>Template</td><td>A optional logging category.</td><td>No.</td></tr>
     * </table>
     * <br/><br/>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void error(XSLProcessorContext context, ElemExtensionCall extensionElement){
        log(context, extensionElement, Level.ERROR);
    }      
    
    private void log(XSLProcessorContext context, ElemExtensionCall extensionElement, Level logLevel) {                
        StringBuilder logData = new StringBuilder();
        String message = null;
        String select = null;
        int lineNumber = extensionElement.getLineNumber();
        try{
            if(hasAttribute(extensionElement, "message")) message = getAttribute("message", context, extensionElement);
        }catch(Throwable t){
            message = "Error getting message attribute (" + extensionElement.getLineNumber() + ":" + extensionElement.getColumnNumber() + "): " + t.getMessage();
        }
        try{            
            if(hasAttribute(extensionElement, "select")) select = getXPath("select", context, extensionElement);
        }catch(Throwable t){
            select = "Error getting select attribute(" + extensionElement.getLineNumber() + ":" + extensionElement.getColumnNumber() + "): " + t.getMessage();
        }
        if(message != null){
            logData.append(message);
            if(select != null) logData.append(": ");
        }
        if(select != null){
            logData.append(select);
        }
        Logger log;
        if(hasAttribute(extensionElement, "category")){
            log = LoggerFactory.getLogger(getAttribute("category", context, extensionElement));
        }else{
            log = staticLog;
        }
        switch(logLevel){
            case TRACE:
                log.trace(getLogString(lineNumber, logData.toString()));
                logInternal(Level.TRACE, log.getName(), logData.toString(), lineNumber);
                break;
            case DEBUG:
                log.debug(getLogString(lineNumber, logData.toString()));
                logInternal(Level.DEBUG, log.getName(), logData.toString(), lineNumber);
                break;
            case INFO:
                log.info(getLogString(lineNumber, logData.toString()));
                logInternal(Level.INFO, log.getName(), logData.toString(), lineNumber);
            case WARN:
                log.warn(getLogString(lineNumber, logData.toString()));
                logInternal(Level.WARN, log.getName(), logData.toString(), lineNumber);
                break;
            case ERROR:
                log.error(getLogString(lineNumber, logData.toString()));
                logInternal(Level.ERROR, log.getName(), logData.toString(), lineNumber);
                break;
        }
    }        

    private void logInternal(Level level, String category, String logData, int lineNumber){
        Level internalLevel = outputLevels.get(Thread.currentThread().getId());
        Writer internalWriter = outputChannel.get(Thread.currentThread().getId());
        if(internalLevel != null && internalWriter != null){
            if(level.ordinal() >= internalLevel.ordinal()){ 
                try{
                    internalWriter.write(category + "::");
                    internalWriter.write(level.toString());
                    internalWriter.write(getLogString(lineNumber, logData));
                    internalWriter.write("\r\n");
                }catch(Throwable failSilently){                    
                }
            }
        }
    }

    private String getLogString(int lineNumber, String logData)  {
        StringBuilder builder = new StringBuilder();
        builder.append("(Line: ");
        builder.append(Integer.toString(lineNumber));
        builder.append(")");
        builder.append(": ");
        builder.append(logData);
        return builder.toString();
    }
    
}
