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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 * Elements for interacting with hibernate (Note: this is directly with hibernate, for new JPA access to hibernate, see the {@link net.adamjenkins.sxe.elements.JPA} framework).
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Hibernate Framework</h3>
 * <br/><br/>
 * Elements for loading a hibernate session factory and interacting with it.
 * <br/><br/>
 * To register the hibernate elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:hibernate="xalan://net.adamjenkins.sxe.elements.Hibernate" extension-element-prefixes="hibernate" ... &gt;
 * </code>
 * <br/><br/>
 * Example Usage:
 * <code><pre>
 * 
 * &lt;hibernate:sessionFactory&gt;
 *  &lt;hibernate:property name="connection.datasource" value="my/first/datasource"/&gt;
 *  &lt;hibernate:property name="hibernate.dialect" value="org.hibernate.dialect.MySQLInnoDBDialect"/&gt;
 * &lt;/hibernate:sessionFactory&gt;
 *
 * &lt;xsl:variable name="myentity"&gt;
 *  &lt;hibernate:load class="com.test.Customer" id="customer/@id"/&gt;
 * &lt;/xsl:variable&gt;
 *
 * &lt;name&gt;&lt;bean:getProperty target="$myentity" property="firstName"/&gt;&lt;/name&gt;
 * </pre></code>
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Hibernate extends AbstractExtensionElement {

    private SessionFactory globalSessionFactory;

    private Configuration currentConfiguration;

    private Session globalSession;

    private Transaction globalTransaction;

    /**
     * Configures a hibernate session factory and optionally sets it to a variable if wrapped in an
     * xsl:variable element.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="sf"&gt;
     *  &lt;hibernate:sessionFactory configurationFile="myconfig/hibernate.cfg.xml"&gt;
     *      &lt;property name="hibernate.dialect" value="org.hibernate.dialect.MySQLInnoDBDialect"/&gt;
     *      &lt;mapping file="c:/entities.hbm.xml"/&gt;
     *      &lt;mapping resource="com/test/entities.hbm.xml"/&gt;
     *      &lt;mapping dir="c:/hibernatefiles"/&gt;
     *  &lt;/hibernate:sessionFactory&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>configurationFile</td><td>Template</td><td>The location of the hibernate.cfg.xml file.</td><td>No (defaults to the resource hibernate.cfg.xml in the root of the classpath)</td></tr>
     *  <tr><td>configurationUrl</td><td>Template</td><td>The URL of the hibernate.cfg.xml file.</td><td>No (defaults to the resource hibernate.cfg.xml in the root of the classpath)</td></tr>
     *  <tr><td>configurationResource</td><td>Template</td><td>The classpath location of the hibernate.cfg.xml file.</td><td>No (defaults to the resource hibernate.cfg.xml in the root of the classpath)</td></tr>
     * </table>
     * <b>Note:</b> You can only use one of the above configuration attributes, if you have more than one the others will be ignored.
     * 
     * @param context
     * @param extensionElement
     */
    public void sessionFactory(XSLProcessorContext context, ElemExtensionCall extensionElement) throws MalformedURLException, TransformerException{
        Configuration config = new Configuration();
        if(hasAttribute(extensionElement, "configurationFile")){
            config.configure(new File(getAttribute("configurationFile", context, extensionElement)));
        }else if(hasAttribute(extensionElement, "configurationUrl")){
            config.configure(new URL(getAttribute("configurationUrl", context, extensionElement)));
        }else if(hasAttribute(extensionElement, "configurationResource")){
            config.configure(getAttribute("configurationResource", context, extensionElement));
        }else{
            config.configure();
        }
        currentConfiguration = config;
        context.getTransformer().executeChildTemplates(extensionElement, true);
        if(!setVariableIfPossible(config.buildSessionFactory(), extensionElement)){
            globalSessionFactory = config.buildSessionFactory();
        }
        currentConfiguration = null;
    }

    /**
     * Adds a mapping resource to a configuration (must be inside a &lt;hibernate:sessionFactory&gt; element).
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="sf"&gt;
     *  &lt;hibernate:sessionFactory configurationFile="myconfig/hibernate.cfg.xml"&gt;
     *      &lt;property name="hibernate.dialect" value="org.hibernate.dialect.MySQLInnoDBDialect"/&gt;
     *      &lt;mapping file="c:/entities.hbm.xml"/&gt;
     *      &lt;mapping resource="com/test/entities.hbm.xml"/&gt;
     *      &lt;mapping dir="c:/hibernatefiles"/&gt;
     *  &lt;/hibernate:sessionFactory&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>file</td><td>Template</td><td>The location of a hibernate mapping file.</td><td>One of the 3 attributes must be present.</td></tr>
     *  <tr><td>resource</td><td>Template</td><td>The classpath location of a hibernate mapping file.</td><td>One of the 3 attributes must be present.</td></tr>
     *  <tr><td>directory</td><td>Template</td><td>The directory of a range of hibernate mapping file.</td><td>One of the 3 attributes must be present.</td></tr>
     * </table>
     * <b>Note:</b> You can only use one of the above configuration attributes, if you have more than one the others will be ignored.  You must have at least one.
     * @param context
     * @param extensionElement
     */
    public void mapping(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(currentConfiguration == null){
            logError(this.getClass(),
                    extensionElement,
                    "Mapping element must be inside a <hibernate:sessionFactory> element.",
                    context);
            return;
        }
        if(hasAttribute(extensionElement, "file")){
            currentConfiguration.addFile(getAttribute("file", context, extensionElement));
        }else if(hasAttribute(extensionElement, "resource")){
            currentConfiguration.addResource(getAttribute("resource", context, extensionElement));
        }else if(hasAttribute(extensionElement, "directory")){
            currentConfiguration.addDirectory(new File(getAttribute("directory", context, extensionElement)));
        }else{
            logError(this.getClass(),
                    extensionElement,
                    "Mapping element must have at least one attribute (file, resource or directory",
                    context);
        }
    }

    /**
     * Adds a property to a configuration (must be inside a &lt;hibernate:sessionFactory&gt; element).
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="sf"&gt;
     *  &lt;hibernate:sessionFactory configurationFile="myconfig/hibernate.cfg.xml"&gt;
     *      &lt;property name="hibernate.dialect" value="org.hibernate.dialect.MySQLInnoDBDialect"/&gt;
     *  &lt;/hibernate:sessionFactory&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>The property name.</td><td>Yes</td></tr>
     *  <tr><td>value</td><td>Template</td><td>The property value.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void property(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(currentConfiguration == null){
            logError(this.getClass(),
                    extensionElement,
                    "Hibernate properties element must be inside a <hibernate:sessionFactory> element.",
                    context);
            return;
        }
        if(!passesAttributeValidation(extensionElement, context, "name", "value")) return;
        currentConfiguration.setProperty(getAttribute("name", context, extensionElement), getAttribute("value", context, extensionElement));
    }

    /**
     * Opens a hibernate session for use.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="session"&gt;
     *  &lt;hibernate:session/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="session"&gt;
     *  &lt;hibernate:openSession sessionFactory="$hibFactory"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>sessionFactory</td><td>XPath</td><td>The session factory to use.</td><td>No</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     * @throws HibernateException 
     */
    public void openSession(XSLProcessorContext context, ElemExtensionCall extensionElement) throws HibernateException, TransformerException{
        //setVariableIfPossible(getSession(context, extensionElement, true), extensionElement);
        if(!setVariableIfPossible(getSessionFactory(context, extensionElement).openSession(), extensionElement)){
            //setting a global session
            if(globalSession != null){
                logError(this.getClass(),
                        extensionElement,
                        "Global session already exists",
                        context);
            }else{
                globalSession = getSessionFactory(context, extensionElement).openSession();
            }
        }
    }

    /**
     * Closes a hibernate session.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:closeSession/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>session</td><td>XPath</td><td>The session to close.</td><td>No (defaults to the global session if one exists)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void closeSession(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        Session s = getSession(context, extensionElement, false);
        if(s == null){
            logError(this.getClass(),
                    extensionElement,
                    "No session available to be closed",
                    context);
        }else{
            s.close();
            if(s == globalSession){
                globalSession = null;
            }
        }
    }

    /**
     * Begins a hibernate transaction.  Can either be set into an xsl:variable as below, or, if not used inside an xsl:variable element, will
     * set up a global transaction.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="transaction"&gt;
     *  &lt;hibernate:beginTransaction/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>session</td><td>XPath</td><td>The session to start a transaction on.</td><td>No (defaults to the global session if one exists)</td></tr>
     * </table>
     * <b>Note:</b> If this element is not placed within a xsl:variable element, then the a default transaction will be created.
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void beginTransaction(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        Session session = getSession(context, extensionElement, false);
        if(session == null) {
            logError(this.getClass(),
                    extensionElement,
                    "No open session to start transaction on",
                    context);
        }else{
            Transaction t = session.beginTransaction();
            if(!setVariableIfPossible(t, extensionElement)){
                globalTransaction = t;
            }
        }
    }

    /**
     * Commits a hibernate transaction.  If no transaction is specified, it will commit the global transaction.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:commit transaction="$mytrans"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>transaction</td><td>XPath</td><td>The transaction to commit.</td><td>No (defaults to the global transaction if one exists)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void commit(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        Transaction t = getTransaction(context, extensionElement);
        if(t != null){
            t.commit();
            if(t == globalTransaction) globalTransaction = null;
        }else{
            logError(this.getClass(),
                    extensionElement,
                    "No transaction specified to commit and no global transaction open.",
                    context);
        }
    }

    /**
     * Rolls back a hibernate transaction.  If no transaction is specified, it will rollback the global transaction.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:rollback transaction="$mytrans"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>transaction</td><td>XPath</td><td>The transaction to rollback.</td><td>No (defaults to the global transaction if one exists)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void rollback(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        Transaction t = getTransaction(context, extensionElement);
        if(t != null){
            t.rollback();
            if(t == globalTransaction) globalTransaction = null;
        }else{
            logError(this.getClass(),
                    extensionElement,
                    "No transaction specified to rollback and no global transaction open.",
                    context);
        }
    }

    /**
     * Executes a hibernate ql query.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="customers"&gt;
     *  &lt;hibernate:query name="customer/name" location="customer/address/city" type="customer/@type"&gt;
     *      from Customer c where c.name = :name and c.city = :location and c.customerType = :type
     *  &lt;/hibernate:query&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>session</td><td>XPath</td><td>The session to use.</td><td>No</td></tr>
     * </table>
     * <b>Note:</b> All other attributes will be translated to hibernate query parameters, for example, if you have
     * a parameter location="@location", the query element will look for a named parameter called :location in your hibernate
     * query string.
     * @param context
     * @param extensionElement
     */
    public void query(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, IOException{
        Session s = getSession(context, extensionElement, true);
        String query = extensionElement.getFirstChild().getNodeValue();
        Query q = s.createQuery(query);
        NamedNodeMap attributes = extensionElement.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++){
            Attr attr = (Attr)attributes.item(i);
            if(attr.getName().equals("session")) continue;
            q.setParameter(
                    attr.getName(),
                    getXObject(attr.getName(), context, extensionElement).object()
            );
        }
        List results = q.list();
        Object value = null;
        if(results.size() == 1){
            value = results.get(0);
        }else if(results.size() > 1){
            value = results;
        }
        if(value != null) setVariableOrWriteToOutput(value, context, extensionElement);
    }

    /**
     * Deletes a hibernate entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:delete entity="$myentity"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to delete.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void delete(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        Session session = getSession(context, extensionElement, true);
        session.delete(
                getXObject("entity", context, extensionElement).object()
                );
        if(closeSessionOnExit(context, extensionElement)){
            session.close();
        }
    }

    /**
     * Flushes a hibernate session.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:flush/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>session</td><td>XPath</td><td>The session to close.</td><td>No (defaults to the global session if one exists)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void flush(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "session")) return;
        getSession(context, extensionElement, true).flush();
    }

    /**
     * Loads a hibernate entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="myentity"&gt;
     *  &lt;hibernate:load className="com.test.MyEntity" id="@id"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>entityName</td><td>Template</td><td>The name of the entity type to load.</td><td>Either an entityName or className must be specified.</td></tr>
     *  <tr><td>className</td><td>Template</td><td>The name of the entity class to load.</td><td>Either an entityName or className must be specified.</td></tr>
     *  <tr><td>id</td><td>XPath</td><td>The id of the entity to load.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void load(XSLProcessorContext context, ElemExtensionCall extensionElement) throws ClassNotFoundException, TransformerException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "id")) return;
        Session session = getSession(context, extensionElement, true);
        if(hasAttribute(extensionElement, "entityName")){
            setVariableOrWriteToOutput(session.load(
                    getXPath("entityName", context, extensionElement),
                    getXPath("id", context, extensionElement)),
                    context,
                    extensionElement);
        }else if(hasAttribute(extensionElement, "className")){
            setVariableOrWriteToOutput(session.load(
                    Class.forName(getXPath("className", context, extensionElement)),
                    getXPath("id", context, extensionElement)),
                    context,
                    extensionElement);
        }else{
            logError(this.getClass(),
                    extensionElement,
                    "Hibernate 'load' element must have either an 'entityName' attribute or a 'class' attribute.",
                    context);
        }
        if(closeSessionOnExit(context, extensionElement)){
            session.close();
        }
    }

    /**
     * Saves a hibernate entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="entityId"&gt;
     *  &lt;hibernate:save entity="$myentity"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:save entity="$myentity"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to save.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void save(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        Session session = getSession(context, extensionElement, true);
        session.save(
                getXObject("entity", context, extensionElement).object()
                );
        if(closeSessionOnExit(context, extensionElement)){
            session.close();
        }
    }

    /**
     * Updates a hibernate entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:update entity="$myentity"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to update.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void update(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        Session session = getSession(context, extensionElement, true);
        session.update(
                getXObject("entity", context, extensionElement).object()
                );
        if(closeSessionOnExit(context, extensionElement)){
            session.close();
        }
    }

    /**
     * Saves a hibernate entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="entityId"&gt;
     *  &lt;hibernate:saveOrUpdate entity="$myentity"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code><pre>
     *  &lt;hibernate:saveOrUpdate entity="$myentity"/&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to save or update.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void saveOrUpdate(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        Session session = getSession(context, extensionElement, true);
        session.saveOrUpdate(
                getXObject("entity", context, extensionElement).object()
                );
        if(closeSessionOnExit(context, extensionElement)){
            session.close();
        }
    }

    private boolean closeSessionOnExit(XSLProcessorContext context, ElemExtensionCall extensionElement){
        return hasAttribute(extensionElement, "session");
    }

    private Transaction getTransaction(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(hasAttribute(extensionElement, "transaction")){
            Object obj = getXObject("transaction", context, extensionElement);
            if(obj instanceof Transaction){
                return (Transaction)obj;
            }else throw new RuntimeException("'transaction' attribute is not a valid hibernate transaction!");
        }else{
            if(globalTransaction != null) return globalTransaction;
            else return null;
        }
    }

    private Session getSession(XSLProcessorContext context, ElemExtensionCall extensionElement, boolean createNew) throws TransformerException{
        if(hasAttribute(extensionElement, "session")){
            Object obj = getXObject("session", context, extensionElement);
            if(obj instanceof Session){
                return (Session)obj;
            }else throw new RuntimeException("'session' attribute is not a valid hibernate session!");
        }else{
            if(globalSession != null) return globalSession;
            if(createNew) return getSessionFactory(context, extensionElement).openSession();
            else return null;
        }
    }

    private SessionFactory getSessionFactory(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(hasAttribute(extensionElement, "sessionFactory")){
            Object obj = getXObject("sessionFactory", context, extensionElement);
            if(obj instanceof SessionFactory){
                return (SessionFactory)obj;
            }else{
                throw new RuntimeException("'sessionFactory' attribute is not a valid hibernate session factory");
            }
        }
        if(globalSessionFactory == null){
            globalSessionFactory = new Configuration().configure().buildSessionFactory();
        }
        return globalSessionFactory;
    }

}
