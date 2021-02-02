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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.adamjenkins.sxe.util.EnumerationIterator;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 * Elements for working with a JMS compliant messaging server.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Java Messaging Service Framework</h3>
 *
 * <br/><br/>
 * To register the JMS elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:jms="xalan://net.adamjenkins.sxe.elements.JMS" extension-element-prefixes="jms" ... &gt;
 * </code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class JMS extends AbstractExtensionElement{

    private static final String[] reservedAttributes = {
        "connectionFactory", "queue", "timeout", "topic",  "select", "selector"
    };

    private Map<Thread,Message> messagesUnderConstruction = new HashMap<Thread,Message>();

    /**
     * Reads a message from a queue.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     *  &lt;xsl:variable name="invoice"&gt;
     *      &lt;jms:messageValue connectionFactory="jms/queueConnectionFactory"
     *                           queue="jms/myQueue"
     *                           selector="JMSPriority > 6 AND type = 'invoice' AND company = '{customer/organisation/@name}'"
     *                           timeout="60000"
     *                           context="$myContext"/&gt;
     *  &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>connectionFactory</td><td>Template</td><td>The JNDI reference of the connection factory.</td><td>Yes</td></tr>
     *  <tr><td>queue</td><td>Template</td><td>The JNDI reference of the queue.</td><td>Yes</td></tr>
     *  <tr><td>selector</td><td>Template</td><td>An optional message selector to use.</td><td>No</td></tr>
     *  <tr><td>timeout</td><td>XPath</td><td>Timeout (in milliseconds)</td><td>No (defaults to 30000)</td></tr>
     *  <tr><td>context</td><td>XPath</td><td>The context to use</td><td>No (if not specified, will use the default context)</td></tr>
     * </table>
     * <b>Note:</b> If this is not inside a <code>&lt;xsl:variable&gt;</code> then the object will be output directly to the result
     * document.
     * 
     * @param context
     * @param extensionElement
     */
    public void messageValue(XSLProcessorContext context, ElemExtensionCall extensionElement) throws NamingException, JMSException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "connectionFactory", "queue")) return;
        Context ctx;
        long timeout = 30000;
        String selector = null;
        if(hasAttribute(extensionElement, "timeout")){
            timeout = (long)getXObject("timeout", context, extensionElement).num();
        }
        if(hasAttribute(extensionElement, "selector")){
            selector = getAttribute("selector", context, extensionElement);
        }
        if(hasAttribute(extensionElement, "context")){
            ctx = (Context)getXObject("context", context, extensionElement).object();
        }else{
            ctx = new InitialContext();
        }
        ConnectionFactory factory = (ConnectionFactory)ctx.lookup(getAttribute("connectionFactory", context, extensionElement));
        Connection conn = factory.createConnection();
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = (Queue)ctx.lookup(getAttribute("queue", context, extensionElement));
        MessageConsumer consumer;
        if(selector != null) consumer = sess.createConsumer(queue,selector);
        else consumer = sess.createConsumer(queue);
        conn.start();
        Message msg = consumer.receive(timeout);
        Object value = null;
        if(msg instanceof TextMessage){
            value = ((TextMessage)msg).getText();
        }else if(msg instanceof MapMessage){
            HashMap<String,Object> map = new HashMap<String,Object>();
            MapMessage mapMessage = (MapMessage)msg;
            for(String name : new EnumerationIterator<String>(mapMessage.getMapNames())){
                map.put(name, mapMessage.getObject(name));
            }
            value = map;
        }else if(msg instanceof ObjectMessage){
            value = ((ObjectMessage)msg).getObject();
        }
        setVariableOrWriteToOutput(value, context, extensionElement);
    }

    /**
     * Publishes a message to a jms queue.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;jms:publish connectionFactory="jms/queueConnectionFactory"
     *                      queue="jms/myQueue"
     *                      select="$myObject"/&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     * &lt;jms:publish connectionFactory="jms/queueConnectionFactory"
     *                      queue="jms/myQueue"&gt;
     *  &lt;property name="customerId" value="@id"/&gt;
     *  &lt;property name="customerName" value="./name"/&gt;
     * &lt;/jms:publish&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     * &lt;jms:publish connectionFactory="jms/queueConnectionFactory"
     *                      queue="jms/myQueue"&gt;
     *  Some text message
     * &lt;/jms:publish&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     * &lt;jms:publish connectionFactory="jms/queueConnectionFactory"
     *                      queue="jms/myQueue"
     *                      JMSPriority="5"
     *                      company="@organisation-name"&gt;
     *  &lt;customer&gt;Adam Norman Jenkins&lt;/customer&gt;
     * &lt;/jms:publish&gt;
     * </pre></code>
     * <br/><br/>
     * <b>Note:</b> The framework will figure out what kind of message you want to send from either the contents of the element, or
     * the result value of the select attribute.
     * <b>Note:</b> You can specify as many attributes outside of the ones listed below that you want, they'll be added to the message
     * as header values that can be used in the selector string of message consumer.
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>connectionFactory</td><td>Template</td><td>The JNDI reference of the connection factory.</td><td>Yes</td></tr>
     *  <tr><td>queue</td><td>Template</td><td>The JNDI reference of the queue.</td><td>Either a queue or a topic must be specified</td></tr>
     *  <tr><td>topic</td><td>Template</td><td>The JNDI reference of the topic.</td><td>Either a queue or a topic must be specified</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void publish(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, NamingException, JMSException, ParserConfigurationException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "connectionFactory")) return;
        if(hasAttribute(extensionElement, "queue") || hasAttribute(extensionElement, "topic")){
            Context ctx;
            if(hasAttribute(extensionElement, "context")){
                ctx = (Context)getXObject("context", context, extensionElement).object();
            }else{
                ctx = new InitialContext();
            }
            ConnectionFactory factory = (ConnectionFactory)ctx.lookup(getAttribute("connectionFactory", context, extensionElement));
            Connection conn = factory.createConnection();
            Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Message msg;
            if(hasAttribute(extensionElement, "select")){
                Object val = getXObject("select", context, extensionElement).object();
                if(val instanceof Map){
                    msg = sess.createMapMessage();
                    Iterator iter = ((Map)val).entrySet().iterator();
                    while(iter.hasNext()){
                        Map.Entry entry = (Map.Entry)iter.next();
                        ((MapMessage)msg).setObject(entry.getKey().toString(), entry.getValue());
                    }
                }else if(val instanceof String){
                    msg = sess.createTextMessage((String)val);
                }else{
                    msg = sess.createObjectMessage((Serializable)val);
                }
            }else{
                if(elementContainsProperties(extensionElement)){
                    msg = sess.createMapMessage();
                    messagesUnderConstruction.put(Thread.currentThread(), msg);
                    context.getTransformer().executeChildTemplates(extensionElement, true);
                    messagesUnderConstruction.remove(Thread.currentThread());
                }else{
                    msg = sess.createTextMessage(processBody(context, extensionElement));
                }
            }
            configureHeaderAttributes(context, extensionElement, msg);
            //send the message here
            Destination dest;
            if(hasAttribute(extensionElement, "queue")){
                dest = (Destination)getXObject("queue", context, extensionElement).object();
            }else{
                dest = (Destination)getXObject("topic", context, extensionElement).object();
            }
            MessageProducer producer = sess.createProducer(dest);
            producer.send(msg);
            sess.close();
            conn.close();
        }else{
            logError(this.getClass(),
                    extensionElement,
                    "You must configure either a queue or topic to publish a message to",
                    context);
        }
        //if it has a value reference, then load the object, if it's not a map, then it's an object message
        //if it is a map, then it's a map message....otherwise
        //check the children...
        //...if there's a 'property' tag, then it's a map message
        //otherwise it's a text message.
    }

    /**
     * Adds a property to a message that is being published.  If you publish tag contains properties, a MapMessage will
     * be sent.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;jms:publish connectionFactory="jms/queueConnectionFactory"
     *                      queue="jms/myQueue"
     *                      timeout="60000"
     *                      JMSPriority="5"
     *                      company="@organisation-name"&gt;
     *  &lt;property name="somevalue" select="/someelement/@someattribute"&gt;
     * &lt;/jms:publish&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>The property name.</td><td>Yes</td></tr>
     *  <tr><td>select</td><td>XPath</td><td>The property value.</td><td>Yes</td></tr>
     * </table>
     * <br/><br/>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void property(XSLProcessorContext context, ElemExtensionCall extensionElement) throws JMSException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "name", "select")) return;
        try{
            MapMessage m = (MapMessage)messagesUnderConstruction.get(Thread.currentThread());
            m.setObject(getAttribute("name", context, extensionElement), getXObject("select", context, extensionElement).object());
        }catch(ClassCastException e){
            logError(this.getClass(), 
                    extensionElement,
                    "You can not add property and use the value attribute when sending messages.",
                    context);
        }
    }

    private boolean elementContainsProperties(ElemExtensionCall extensionElement){
        return countChildElementsOfType(extensionElement, "xalan://net.adamjenkins.sxe.elements.JMS", "property") > 0;
    }

    public void configureHeaderAttributes(XSLProcessorContext context, ElemExtensionCall extensionElement, Message msg) throws JMSException, TransformerException{
        NamedNodeMap map = extensionElement.getAttributes();
        for(int i = 0; i < map.getLength(); i++){
            Attr attribute = (Attr)map.item(i);
            if(Arrays.binarySearch(reservedAttributes, attribute.getName()) < 0){
                msg.setObjectProperty(attribute.getName(), getXObject(attribute.getName(), context, extensionElement).object());
            }
        }
    }

}
