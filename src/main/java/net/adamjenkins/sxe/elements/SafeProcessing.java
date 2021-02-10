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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.adamjenkins.sxe.util.XSLTErrorListener;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.DOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Element for performing error handling within an XSLT file rather than having the transformation fail completely.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Safe Processing Framework</h3>
 * This framework provides the ability to perform error handling within an XSLT transformation (similar to java try/catch).
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:safe="xalan://net.adamjenkins.sxe.elements.SafeProcessing" extension-element-prefixes="safe" ... &gt;
 * </code>
 * <br/><br/>
 * Example Usage:
 * <br/><br/>
 * <code><pre>
 * &lt;safe:safely&gt;
 *  &lt:safe:run&gt;
 *      ...do something...
 *  &lt;/safe:run&gt;
 *  &lt;safe:onError&gt;
 *      ...handle error...
 *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveError/&gt;&lt;/xsl:variable&gt;
 *      &lt;log:error select="$myError"/&gt;
 *  &lt;/safe:onError&gt;
 *  &lt;safe:after&gt;
 *      ...similar to 'finally' in java
 *  &lt;/after&gt;
 * &lt;/safe:safely&gt;
 * </pre></code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class SafeProcessing extends AbstractExtensionElement {

    /**
     * Safely executes some code (similar to try/catch/finally in java).
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;safe:safely&gt;
     *  &lt:safe:run&gt;
     *      ...do something...
     *  &lt;/safe:run&gt;
     *  &lt;safe:onError&gt;
     *      ...handle error...
     *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveErrors/&gt;&lt;/xsl:variable&gt;
     *      &lt;log:error select="$myError"/&gt;
     *  &lt;/safe:onError&gt;
     *  &lt;safe:after&gt;
     *      ...similar to 'finally' in java
     *  &lt;/after&gt;
     * &lt;/safe:safely&gt;
     * </pre></code>
     * @param context
     * @param extensionElement
     */
    public void safely(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        swapErrorListener(context);
        context.getTransformer().executeChildTemplates(extensionElement, true);
        resetErrorListener(context);
    }

    /**
     * Specifies the transformation that should be run safely.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;safe:safely&gt;
     *  &lt:safe:run&gt;
     *      ...do something...
     *  &lt;/safe:run&gt;
     *  &lt;safe:onError&gt;
     *      ...handle error...
     *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveErrors/&gt;&lt;/xsl:variable&gt;
     *      &lt;log:error select="$myError"/&gt;
     *  &lt;/safe:onError&gt;
     *  &lt;safe:after&gt;
     *      ...similar to 'finally' in java
     *  &lt;/after&gt;
     * &lt;/safe:safely&gt;
     * </pre></code>
     * @param context
     * @param extensionElement
     */
    public void run(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, ParserConfigurationException, MalformedURLException, FileNotFoundException, IOException{
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder handler = new DOMBuilder(doc, frag);
        context.getTransformer().executeChildTemplates(extensionElement, handler);
        XSLTErrorListener listener = (XSLTErrorListener)context.getTransformer().getErrorListener();
        if(listener.getAllErrors().size() == 0){
            context.outputToResultTree(context.getStylesheet(), frag);
        }
    }

    /**
     * Specifies the transformation that should be executed if an error occuring during transformation of the <code>run</code>
     * section.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;safe:safely&gt;
     *  &lt:safe:run&gt;
     *      ...do something...
     *  &lt;/safe:run&gt;
     *  &lt;safe:onError&gt;
     *      ...handle error...
     *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveErrors/&gt;&lt;/xsl:variable&gt;
     *      &lt;log:error select="$myError"/&gt;
     *  &lt;/safe:onError&gt;
     *  &lt;safe:after&gt;
     *      ...similar to 'finally' in java
     *  &lt;/after&gt;
     * &lt;/safe:safely&gt;
     * </pre></code>
     * @param context
     * @param extensionElement
     */
    public void onError(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        try{
            XSLTErrorListener errorListener = (XSLTErrorListener)context.getTransformer().getErrorListener();
            if(errorListener.getAllErrors().size() > 0) {
                context.getTransformer().executeChildTemplates(extensionElement, true);
            }
        }catch(ClassCastException e){
            logError(this.getClass(), extensionElement, "Error listener is not an SXE error listener", context);
        }
    }

    /**
     * Similar tot he java <code>finally</code> statement, this defines a transformation that will be performed
     * regardless of whether the transformation ran successfully, or had an error.
      * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;safe:safely&gt;
     *  &lt:safe:run&gt;
     *      ...do something...
     *  &lt;/safe:run&gt;
     *  &lt;safe:onError&gt;
     *      ...handle error...
     *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveErrors/&gt;&lt;/xsl:variable&gt;
     *      &lt;log:error select="$myError"/&gt;
     *  &lt;/safe:onError&gt;
     *  &lt;safe:after&gt;
     *      ...similar to 'finally' in java
     *  &lt;/after&gt;
     * &lt;/safe:safely&gt;
     * </pre></code>
     * @param context
     * @param extensionElement
     */
    public void after(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        context.getTransformer().executeChildTemplates(extensionElement, true);
    }

    /**
     * Retrieves the error(s) that were thrown from the <code>run</code> section.  This only works
     * from within the <code>onError</code> tag.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;safe:safely&gt;
     *  &lt:safe:run&gt;
     *      ...do something...
     *  &lt;/safe:run&gt;
     *  &lt;safe:onError&gt;
     *      ...handle error...
     *      &lt;xsl:variable name="myerror"&gt;&lt;safe:retrieveErrors/&gt;&lt;/xsl:variable&gt;
     *      &lt;log:error select="$myError"/&gt;
     *  &lt;/safe:onError&gt;
     *  &lt;safe:after&gt;
     *      ...similar to 'finally' in java
     *  &lt;/after&gt;
     * &lt;/safe:safely&gt;
     * </pre></code>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void retrieveErrors(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        try{
            XSLTErrorListener errorListener = (XSLTErrorListener)context.getTransformer().getErrorListener();
            setVariableIfPossible(context.getTransformer(), errorListener.getAllErrors(), extensionElement);
        }catch(ClassCastException e){
            logError(this.getClass(), extensionElement, "Error listener is not an SXE error listener", context);
        }
    }

}
