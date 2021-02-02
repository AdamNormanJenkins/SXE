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
import java.lang.Thread;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 * Elements for working with the Java Persistence API.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Java Persistence Framework</h3>
 * <br/><br/>
 * The elements below offer a range of functionality for working with a JPA provider.
 * <br/><br/>
 * To register the JPA elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:jpa="xalan://net.adamjenkins.sxe.elements.JPA" extension-element-prefixes="jpa" ... &gt;
 * </code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class JPA extends AbstractExtensionElement{

    private Map<Thread, Properties> emPropertyOverride = new HashMap<Thread, Properties>();

    private EntityManager globalEntityManager;

    private EntityTransaction globalTransaction;

     /**
      * Make sure you configure both the flush mode and the environment type (managed, unmanaged) and whether global or not
      * Creates a new entity manager and either sets it to the variable specified, or makes it global.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code><pre>
      * &lt;xsl:variable name="entityManager"&gt;
      *     &lt;ejb:entityManager persistenceUnit="myPU"/&gt;
      * &lt;/xsl:variable&gt;
      * </pre></code>
      * <br/><br/>
      * <code><pre>
      * &lt;xsl:variable name="entityManager"&gt;
      *     &lt;ejb:entityManager persistenceUnit="myPU"&gt;
      *         &lt;ejb:property name="flush" value="true"/&gt;
      *     &lt;/ejb:entityManager&gt;
      * &lt;/xsl:variable&gt;
      * </pre></code>
      * <br/><br/>
      * <b>Note:</b> If your entityManager element is surrounded by a <code>xsl:variable</code> element, it will be set to that variable, otherwise
      * the global entity manager will be set.
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>persistenceUnit</td><td>Template</td><td>The persistence unit to use.</td><td>Yes.</td></tr>
      * </table>
      * @param context
      * @param extensionElement
      */
    public void entityManager(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "persistenceUnit")) return;
        EntityManagerFactory factory = Persistence.createEntityManagerFactory(getAttribute("persistenceUnit", context, extensionElement));
        emPropertyOverride.put(Thread.currentThread(), new Properties());
        context.getTransformer().executeChildTemplates(extensionElement, true);
        Properties props = emPropertyOverride.remove(Thread.currentThread());
        EntityManager manager;
        if(props.size() > 0) manager = factory.createEntityManager(props);
        else manager = factory.createEntityManager();
        if(!setVariableIfPossible(manager, extensionElement)){
            if(globalEntityManager != null){
                logError(this.getClass(), extensionElement, "Global entity manager already exists", context);
                return;
            }
            globalEntityManager = manager;
        }
    }

     /**
      * A configuration property for an entity manager.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code>&lt;property name="flush" value="true"/&gt;</code>
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>name</td><td>Template</td><td>The name of the property.</td><td>Yes.</td></tr>
      *  <tr><td>value</td><td>Template</td><td>The value of the property.</td><td>Yes.</td></tr>
      * </table>
      * @param context
      * @param extensionElement
      */
    public void property(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(!passesAttributeValidation(extensionElement, context, "name", "value")) return;
        emPropertyOverride.get(Thread.currentThread()).setProperty(
              getAttribute("name", context, extensionElement),
              getAttribute("value", context, extensionElement)
        );
    }

     /**
      * Begins a JPA transaction.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code><pre>
      * &lt;xsl;variable name="transaction"&gt;
      *     &lt;ejb:beginTransaction entityManager="$manager"/&gt;
      * &lt;/xsl:variable&gt;
      * </pre></code>
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>manager</td><td>XPath</td><td>The entity manager.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
      * </table>
      * <b>Note:</b> If this element is not placed within a xsl:variable element, then the a default transaction will be created.
      * @param context
      * @param extensionElement
     * @throws TransformerException 
      */
    public void beginTransaction(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        EntityTransaction t = m.getTransaction();
        t.begin();
        if(!setVariableIfPossible(t, extensionElement)){
            globalTransaction = t;
        }
    }

    /**
     * Commits a JPA Transaction.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code>&lt;ejb:commit transaction="$transaction"/&gt;</code>
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>transaction</td><td>XPath</td><td>The transaction to commit.</td><td>No (if not specified, the default transaction will be used)</td></tr>
      * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void commit(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        EntityTransaction t = getEntityTransaction(context, extensionElement);
        if(t == null){
            logError(this.getClass(), extensionElement, "No transaction supplied or configured", context);
            return;
        }
        t.commit();
    }

    /**
     * Rollback a JPA Transaction.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code>&lt;ejb:rollback transaction="$transaction"/&gt;</code>
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>transaction</td><td>XPath</td><td>The transaction to rollback.</td><td>No (if not specified, the default transaction will be used)</td></tr>
      * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void rollback(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        EntityTransaction t = getEntityTransaction(context, extensionElement);
        if(t == null){
            logError(this.getClass(), extensionElement, "No transaction supplied or configured", context);
            return;
        }
        t.rollback();
    }

    /**
     * Flushes an entity manager.
      * <br/><br/>
      * Example Usage:
      * <br/><br/>
      * <code>&lt;ejb:flush entityManager="$manager"/&gt;</code>
      * <br/><br/>
      * <table border="1">
      *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
      *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
      * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void flush(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        m.flush();
    }

    /**
     * Gets a reference, possibly a lazy loading reference, to an entity.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="myEntity"&gt;
     *     &lt;ejb:reference class="com.test.MyEntity" id="10"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
     *  <tr><td>class</td><td>Template</td><td>The class to load.</td><td>Yes</td></tr>
     *  <tr><td>id</td><td>XPath</td><td>The primary key.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void reference(XSLProcessorContext context, ElemExtensionCall extensionElement) throws ClassNotFoundException, TransformerException, MalformedURLException, FileNotFoundException, IOException{
        if(!passesAttributeValidation(extensionElement, context, "class", "id")) return;
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        Object loaded = m.getReference(
                Class.forName(getAttribute("class", context, extensionElement)),
                getXObject("id", context, extensionElement).object()
        );
        if(!setVariableIfPossible(loaded, extensionElement)){
            context.outputToResultTree(context.getStylesheet(), loaded);
        }
    }

    /**
     * Loads an object.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="myEntity"&gt;
     *     &lt;ejb:find class="com.test.MyEntity" id="10"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
     *  <tr><td>class</td><td>Template</td><td>The class to load.</td><td>Yes</td></tr>
     *  <tr><td>id</td><td>XPath</td><td>The primary key.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void find(XSLProcessorContext context, ElemExtensionCall extensionElement) throws ClassNotFoundException, IOException, TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "class", "id")) return;
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        Object loaded = m.find(
                Class.forName(getAttribute("class", context, extensionElement)),
                getXObject("id", context, extensionElement).object()
        );
        if(!setVariableIfPossible(loaded, extensionElement)){
            context.outputToResultTree(context.getStylesheet(), loaded);
        }
    }

    /**
     * Persists an entity
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>&lt;ejb:persist entity="$myEntity"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to save.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void persist(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        m.persist(getXObject("entity", context, extensionElement).object());
    }

    /**
     * Refreshes an entity (reloads it's property data from the database).
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>&lt;ejb:refresh entity="$myEntity"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to refresh.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void refresh(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        m.refresh(getXObject("entity", context, extensionElement).object());
    }

    /**
     * Removes an entity (deletes it from the database).
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code>&lt;ejb:remote entity="$myEntity"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to flush.</td><td>No (if not specified, the default entity manager will be used)</td></tr>
     *  <tr><td>entity</td><td>XPath</td><td>The entity to remove.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void remove(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "entity")) return;
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        m.remove(getXObject("entity", context, extensionElement).object());
    }

    /**
     * Executes a JPA EJBQL query.
     * <br/><br/>
     * Example Usage:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="customers"&gt;
     *  &lt;ejb:query name="customer/name" location="customer/address/city" type="customer/@type"&gt;
     *      from Customer c where c.name = :name and c.city = :location and c.customerType = :type
     *  &lt;/ejb:query&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>manager</td><td>XPath</td><td>The entity manager to use.</td><td>No</td></tr>
     * </table>
     * <b>Note:</b> All other attributes will be translated to EJBQL query parameters, for example, if you have
     * a parameter location="@location", the query element will look for a named parameter called :location in your EJB
     * query string.
     * @param context
     * @param extensionElement
     */
    public void query(XSLProcessorContext context, ElemExtensionCall extensionElement) throws IOException, TransformerException{
        EntityManager m = getEntityManager(context, extensionElement);
        if(m == null){
            logError(this.getClass(), extensionElement, "No entity manager configured or supplied", context);
            return;
        }
        String query = extensionElement.getFirstChild().getNodeValue();
        Query q = m.createQuery(query);
        NamedNodeMap attributes = extensionElement.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++){
            Attr attr = (Attr)attributes.item(i);
            if(attr.getName().equals("manager")) continue;
            q.setParameter(
                    attr.getName(),
                    getXObject(attr.getName(), context, extensionElement).object()
            );
        }
        List results = q.getResultList();
        Object value = null;
        if(results.size() == 1){
            value = results.get(0);
        }else if(results.size() > 1){
            value = results;
        }
        if(value != null) setVariableOrWriteToOutput(value, context, extensionElement);
    }

    private EntityManager getEntityManager(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(hasAttribute(extensionElement, "manager")){
            return (EntityManager)getXObject("manager", context, extensionElement).object();
        }
        return globalEntityManager;
    }

    private EntityTransaction getEntityTransaction(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(hasAttribute(extensionElement, "transaction")){
            return (EntityTransaction)getXObject("transaction", context, extensionElement);
        }
        return globalTransaction;
    }

}
