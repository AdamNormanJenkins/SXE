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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.DOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * Element for sending emails from within XSLT.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Mail Framework</h3>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:mail="xalan://net.adamjenkins.sxe.elements.Mail" extension-element-prefixes="mail" ... &gt;
 * </code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Mail extends AbstractExtensionElement{

    private Session globalSession;

    private Properties mailProperties;

    private Map<Thread, MimeMessage> emailsUnderCreation = new HashMap<Thread, MimeMessage>();

    /**
     * Creates a new mail session and optionally attaches it to a variable.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="session"&gt;
     *  &lt;mail:session ref="mail/session" context="$context"/&gt;
     * &lt;/xsl:varaiable&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * &lt;xsl:variable name="session"&gt;
     *  &lt;mail:session username="bob" password="blah"&gt;
     *     &lt;mail:property name="mail.smtp.host" value="mymailserver.mycompany.com"/&gt;
     *  &lt;/mail:session&gt;
     * &lt;/xsl:varaiable&gt;
     * </pre></code>
     * <br/><br/>
     * <b>Note:</b> If the session is not placed within a <code>xsl:variable</code> tag, then the default session will be set.
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>ref</td><td>Template</td><td>The jndi reference if we're looking the mail session up from JNDI.</td><td>Only if using JNDI</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The context to lookup.</td><td>No</td></tr>
     *  <tr><td>username</td><td>Template</td><td>The username.</td><td>No</td></tr>
     *  <tr><td>password</td><td>Template</td><td>The password.</td><td>No</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void session(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, TransformerException{
        Session session;
        mailProperties = new Properties();
        DefaultPasswordAuthentication authenticator = null;
        if(hasAttribute(extensionElement, "username") && hasAttribute(extensionElement, "password")){
                authenticator = new DefaultPasswordAuthentication(
                    getAttribute("username", context, extensionElement),
                    getAttribute("password", context, extensionElement)
                );
        }
        if(hasAttribute(extensionElement, "ref")){
            Context ctx;
            if(hasAttribute(extensionElement, "context")){
                ctx = (Context)getXObject("context", context, extensionElement).object();
            }else{
                ctx = new InitialContext();
            }
            session = (Session)ctx.lookup(getAttribute("ref", context, extensionElement));
        }else{
            context.getTransformer().executeChildTemplates(extensionElement, true);
            if(authenticator != null) session = Session.getInstance(mailProperties, authenticator);
            else session = Session.getInstance(mailProperties);
        }
        if(!setVariableIfPossible(context.getTransformer(), session, extensionElement)){
            globalSession = session;
        }
    }

    /**
     * Adds a property to a mail session.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * &lt;xsl:variable name="session"&gt;
     *  &lt;mail:session username="bob" password="blah"&gt;
     *     &lt;mail:property name="mail.host" value="mymailserver.mycompany.com"/&gt;
     *  &lt;/mail:session&gt;
     * &lt;/xsl:varaiable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>The property name.</td><td>Yes</td></tr>
     *  <tr><td>value</td><td>Template</td><td>The property value.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void property(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(!passesAttributeValidation(extensionElement, context, "name", "value")) return;
        mailProperties.setProperty(
                getAttribute("name", context, extensionElement),
                getAttribute("value", context, extensionElement)
        );
    }

    /**
     * Sends an email.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     *  &lt;mail:email session="$mailSession" from="mail@somecompany.com" subject="Some email"&gt;
     *      &lt;mail:to&gt;someoneelse@somecompany.com&lt;/mail:to&gt;
     *      &lt;mail:to&gt;someone@someothercompany.com&lt/mail:to&gt;
     *      &lt;mail:body type="text"&gt;
     *          This is an email
     *      &lt;/mail:body&gt;
     *  &lt;/mail:email&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>session</td><td>XPath</td><td>The mail session to use.</td><td>No (if not supplied, the default one will be used)</td></tr>
     *  <tr><td>from</td><td>Template</td><td>The 'from' field for the email.</td><td>Yes</td></tr>
     *  <tr><td>subject</td><td>Template</td><td>The 'subject' field for the email.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void email(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MessagingException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "from", "subject")) return;
        Session s;
        if(hasAttribute(extensionElement, "session")){
            s = (Session)getXObject("session", context, extensionElement).object();
        }else{
            s = globalSession;
        }
        MimeMessage message = new MimeMessage(s);
        message.setFrom(new InternetAddress(getAttribute("from", context, extensionElement)));
        message.setSubject(getAttribute("subjecct", context, extensionElement));
        emailsUnderCreation.put(Thread.currentThread(), message);
        context.getTransformer().executeChildTemplates(extensionElement, true);
        message = emailsUnderCreation.remove(Thread.currentThread());
        message.saveChanges(); 
        Transport transport = s.getTransport("smtp");
        transport.connect();
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    /**
     * Configures an email body.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     *  &lt;mail:email session="$mailSession" from="mail@somecompany.com" subject="Some email"&gt;
     *      &lt;mail:to&gt;someoneelse@somecompany.com&lt;/mail:to&gt;
     *      &lt;mail:to&gt;someone@someothercompany.com&lt/mail:to&gt;
     *      &lt;mail:body type="text/plain"&gt;
     *          This is an email
     *      &lt;/mail:body&gt;
     *  &lt;/mail:email&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="lefYest">Mandatory?</th></tr>
     *  <tr><td>type</td><td>Template</td><td>The email type (either TEXT or HTML - case sensitive).</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void body(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MessagingException, TransformerException, ParserConfigurationException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "type")) return;
        MimeMessage msg = emailsUnderCreation.get(Thread.currentThread());
        msg.setContent(processBody(context, extensionElement), getAttribute("type", context, extensionElement));
    }

    /**
     * Configures who to send the email to.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     *  &lt;mail:email session="$mailSession" from="mail@somecompany.com"&gt;
     *      &lt;mail:to&gt;someoneelse@somecompany.com&lt;/mail:to&gt;
     *      &lt;mail:to&gt;someone@someothercompany.com&lt/mail:to&gt;
     *      &lt;mail:body type="text"&gt;
     *          This is an email
     *      &lt;/mail:body&gt;
     *  &lt;/mail:email&gt;
     * </pre></code>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void to(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MessagingException, TransformerException, ParserConfigurationException, IOException{
        MimeMessage msg = emailsUnderCreation.get(Thread.currentThread());
        msg.addRecipient(RecipientType.TO, new InternetAddress(processBody(context, extensionElement)));
    }

    /**
     * Adds a 'cc' to the email.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     *  &lt;mail:email session="$mailSession" from="mail@somecompany.com"&gt;
     *      &lt;mail:to&gt;someoneelse@somecompany.com&lt;/mail:to&gt;
     *      &lt;mail:to&gt;someone@someothercompany.com&lt/mail:to&gt;
     *      &lt;mail:cc&gt;someoneelse@someothercompany.com&lt/mail:cc&gt;
     *      &lt;mail:body type="text"&gt;
     *          This is an email
     *      &lt;/mail:body&gt;
     *  &lt;/mail:email&gt;
     * </pre></code>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void cc(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MessagingException, TransformerException, ParserConfigurationException, IOException{
        MimeMessage msg = emailsUnderCreation.get(Thread.currentThread());
        msg.addRecipient(RecipientType.CC, new InternetAddress(processBody(context, extensionElement)));
    }

    /**
     * Adds a 'bcc' to the email.
     * <br/><br/>
     * Usage Examples:
     * <br/><br/>
     * <code><pre>
     *  &lt;mail:email session="$mailSession" from="mail@somecompany.com"&gt;
     *      &lt;mail:to&gt;someoneelse@somecompany.com&lt;/mail:to&gt;
     *      &lt;mail:to&gt;someone@someothercompany.com&lt/mail:to&gt;
     *      &lt;mail:cc&gt;someoneelse@someothercompany.com&lt/mail:cc&gt;
     *      &lt;mail:body type="text"&gt;
     *          This is an email
     *      &lt;/mail:body&gt;
     *  &lt;/mail:email&gt;
     * </pre></code>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void bcc(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MessagingException, TransformerException, ParserConfigurationException, IOException{
        MimeMessage msg = emailsUnderCreation.get(Thread.currentThread());
        msg.addRecipient(RecipientType.BCC, new InternetAddress(processBody(context, extensionElement)));
    }

    private static class DefaultPasswordAuthentication extends Authenticator{
        private String username;
        private String password;
        DefaultPasswordAuthentication(String username, String password){
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }


    }

}
