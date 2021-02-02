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
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerException;
import net.adamjenkins.sxe.util.XSLTErrorListener;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.DOMBuilder;

/**
 * Element for calling SOAP web services from XSLT and incorporating the results into the processing tree.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE SOAP Framework</h3>
 * This framework provides the ability to send a SOAP message to a server, and incorporate the response into the context
 * of the document being processed.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:soap="xalan://net.adamjenkins.sxe.elements.SOAP" extension-element-prefixes="soap" ... &gt;
 * </code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class SOAP extends AbstractExtensionElement{

    private Map<Thread, SOAPEnvelope> envelopesUnderDevelopment = new HashMap<Thread, SOAPEnvelope>();

    /**
     * Invokes a SOAP service and incorporates the response into the XSLT context.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:template match="company"&gt;
     *    &lt;soap:invoke endPoint="http://somecompany.com/someservice"&gt;
     *      &lt;soap:header&gt;
     *          &lt;wsse:Security&gt;
     *              &lt;wsse:UsernameToken&gt;
     *                  &lt;wsse:Username&gt;&lt;xsl:value-of select="/contact/login/@user"/&gt;&lt;/wsse:Username&gt;
     *                  &lt;wsse:Password&gt;&lt;xsl:value-of select="/contact/login/@pass"/&gt;&lt;/wsse:Password&gt;
     *              &lt;/wsse:UsernameToken&gt;
     *          &lt;/wsse:Security&gt;
     *      &lt;/soap:header&gt;
     *      &lt;soap:body&gt;
     *          &lt;someSoapMessage/&gt;
     *      &lt;/soap:body&gt;
     *    &lt;/soap:invoke&gt;
     * &lt;/xsl:template&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>endPoint</td><td>Template</td><td>The soap endpoint.</td><td>Yes</td></tr>
     *  <tr><td>action</td><td>Template</td><td>The soap action.</td><td>No</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void invoke(XSLProcessorContext context, ElemExtensionCall extensionElement) throws SOAPException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "endPoint")) return;
        String destination = getAttribute("endPoint", context, extensionElement);
        swapErrorListener(context);
        SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = soapConnFactory.createConnection();
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        if(hasAttribute(extensionElement, "action")){
            MimeHeaders hd = message.getMimeHeaders();
            hd.addHeader("SOAPAction", getAttribute("action", context, extensionElement));
        }
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelopesUnderDevelopment.put(Thread.currentThread(), envelope);
        DOMBuilder handler = new DOMBuilder(envelope.getOwnerDocument(), envelope);
        context.getTransformer().executeChildTemplates(extensionElement, handler);
        message.saveChanges();
        envelopesUnderDevelopment.remove(Thread.currentThread());
        XSLTErrorListener listener = resetErrorListener(context);
        if(listener.getAllErrors().size() > 0){
            listener.transferTo(context.getTransformer().getErrorListener());
            logError(this.getClass(),
                     extensionElement,
                     "Could not invoke soap service, errors occured during message transformation",
                     listener);
            return;
        }
        SOAPMessage response = connection.call(message, destination);
        connection.close();
        if(response.getSOAPBody().hasFault()){
            SOAPFault fault = response.getSOAPBody().getFault();
            logError(this.getClass(),
                     extensionElement,
                     "Soap Fault: [" + fault.getFaultCode() + "] from [" + fault.getFaultActor() + "]: " + fault.getFaultString(),
                     context);
        }else{
            context.outputToResultTree(context.getStylesheet(), response.getSOAPBody().getFirstChild());
        }
    }

    /**
     * The soap header element (see the invoke method documentation for examples).
     * 
     * @param context
     * @param extensionElement
     */
    public void header(XSLProcessorContext context, ElemExtensionCall extensionElement) throws SOAPException, TransformerException{
        SOAPEnvelope envelope = envelopesUnderDevelopment.get(Thread.currentThread());
        SOAPHeader header = envelope.getHeader();
        DOMBuilder handler = new DOMBuilder(header.getOwnerDocument(), header);
        context.getTransformer().executeChildTemplates(extensionElement, handler);
    }

    /**
     * The soap body element (see the invoke method documentation for examples).
     * @param context
     * @param extensionElement
     */
    public void body(XSLProcessorContext context, ElemExtensionCall extensionElement) throws SOAPException, TransformerException{
        SOAPEnvelope envelope = envelopesUnderDevelopment.get(Thread.currentThread());
        SOAPBody body = envelope.getBody();
        DOMBuilder handler = new DOMBuilder(body.getOwnerDocument(), body);
        context.getTransformer().executeChildTemplates(extensionElement, handler);
    }

}
