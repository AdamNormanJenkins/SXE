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
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.objects.XObject;

import net.adamjenkins.sxe.elements.concurrency.EmbeddedStylesheetDefinition;

/**
 * Elements for safely performing concurrent operations.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Concurrency Framework</h3>
 * These are a range of elements for creating high performance, concurrently executing XSLT files.  It allows you
 * to run parts of your XSLT file on multiple threads.  <b>WARNING:</b> This should be considered an advanced
 * user library, you can really mess things up if you're not careful with this one!
 * <br/><br/>
 * Concurrent XSLT operations use read write locks to perform XSLT processing concurrently, and updating of the
 * output safely.  Please note, the concurrency elements only protect against concurrent updates of the XSLT output,
 * if you have resources that are being accessed concurrently that need to be protected, you can either create an use
 * a read/write lock or you can synchronize the block of code being executed.
 * <br/><br/>
 * <b>Note:</b> While concurrent operations will give you a faster processing time, they cannot specify the order
 * that the concurrent elements will be added to the final document, and they do increase the memory footprint for your
 * transformation.
 * <br/><br/>
 * To register the concurrency elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:concurrent="xalan://net.adamjenkins.sxe.elements.Concurrency" extension-element-prefixes="concurrent" ... &gt;
 * </code>
 * <h3>DO NOT USE THIS ELEMENT -- UNDER CONSTRUCTION</h3>
 * <br/><br/>
 * Example Usage:
 * <br/><br/>
 * <code>
 * <pre>
 * &lt;template match="/"&gt;
 *  &lt;div id="allcontacts-unordered"&gt;
 *      &lt;xsl:apply-templates select="contacts/*"/&gt;
 *      &lt;concurrent:waitForThreads/&gt;
 *  &lt;/div&gt;
 * &lt;/template&gt;
 *
 * &lt;template match="contact"&gt;
 *      &lt;concurrent:parallel&gt;
 *          &lt;div&gt;
 *              &lt;xsl:value-of select="@name"/&gt;
 *          &lt;/div&gt;
 *      &lt;/concurrent:parallel&gt;
 * &lt;/template&gt;
 * </pre>
 * </code>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Concurrency extends AbstractExtensionElement{

    private final HashSet<Thread> runningThreads = new HashSet<Thread>();

    private final HashMap<ElemExtensionCall,EmbeddedStylesheetDefinition> stylesheetCache = new HashMap<ElemExtensionCall, EmbeddedStylesheetDefinition>();

    private final ReentrantLock synchronizationLock = new ReentrantLock();

    /**
     * Allows part of the XSLT document processing that is being performed on another thread to be done
     * in a thread safe manner.  It is similar to the java <i>synchronized</i> keyword, but allows for reentrant
     * behaviour.  While reentrant synchronization is supported, it should not be considered best practice.
     * <br/><br/>
     * Usage Example
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;template match="contact"&gt;
     *      &lt;concurrent:parallel&gt;
     *          &lt;div&gt;
     *              &lt;xsl:value-of select="@name"/&gt;
     *              &lt;concurrent:synchronize&gt;
     *                  &lt;bean:invoke target="$phoneBookEJB" method="lookupPhoneNumberByName"&gt;
     *                      &lt;bean:parameter select="@name"/&gt;
     *                  &lt;/bean:invoke&gt;
     *              &lt;/concurrent:synchronize&gt;
     *          &lt;/div&gt;
     *      &lt;/concurrent:parallel&gt;
     * &lt;/template&gt;
     * </pre>
     * </code>
     *
     * @param context
     * @param extensionElement
     * @throws TransformerException
     */
    public void synchronize(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        synchronizationLock.lock();
        try{
            context.getTransformer().executeChildTemplates(extensionElement, true);
        }finally{
            synchronizationLock.unlock();
        }
    }

    /**
     * Waits for all currently executing threads to finish.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;concurrent:waitForThreads/&gt;</code>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public synchronized void waitForThreads(XSLProcessorContext context, ElemExtensionCall extensionElement){
        while(runningThreads.size() > 0){
            try{
                Thread.sleep(10l);
            }catch(InterruptedException ignore){}
        }
    }

    /**
     * Waits for a predetermined length of time (in milliseconds).  Most useful in testing, not many real world
     * applications.  Also, note that the millseconds time is the minimum amount of time the current thread will
     * sleep for..the actual amount of time is controlled by the JVM (standard java threading 101 stuff).
     * <br/><br/>
     * Usage example:
     * <br/><br/>
     * <code>&lt;concurrent:wait milliseconds="10000"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>milliseconds</td><td>XPath</td><td>The amount of time, in milliseconds, to wait for.</td><td>Yes</td></tr>
     * </table>
     * <br/><br/>
     * @param context
     * @param extensionElement
     */
    public void wait(XSLProcessorContext context, ElemExtensionCall extensionElement){
        try{
            long time = Long.parseLong(getXPath("milliseconds", context, extensionElement));
            Thread.sleep(time);
        }catch(InterruptedException e){
            logError(this.getClass(), extensionElement, e.getMessage(), context);
        }
    }

    /**
     * Starts a parallel thread that processes the child elements and writes to the output document when completed.  Use
     * this at your own peril...it is a very advanced XSLT element and you can really screw your output documents up if you're
     * not managing your parallel threads effectively.
     * <br/><br/>
     * <b>Note:</b> When the first parallel element runs, a brand new XSLT stylesheet is created in memory from both the contents of
     * the <code>&lt:concurrent:parallel/&gt; element and any templates that are referenced by it (recursively) which is a somewhat expensive
     * operation.  So you need to make up your own mind whether you will get performance gains from parallelling your processing...it's really a
     * very use case dependant gain.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>
     * <pre>
     * &lt;template match="contact"&gt;
     *      &lt;concurrent:parallel&gt;
     *          &lt;div&gt;
     *              &lt;xsl:value-of select="@name"/&gt;
     *              &lt;text&gt;:&lt;/text&gt;
     *              &lt;xsl:value-of select="@phone-number"/&gt;
     *          &lt;/div&gt;
     *      &lt;/concurrent:parallel&gt;
     * &lt;/template&gt;
     * </pre>
     * </code>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>pool</td><td>XPath</td><td>A previously created thread pool to use.</td><td>No</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public synchronized void parallel(XSLProcessorContext context, ElemExtensionCall extensionElement) throws ParserConfigurationException {
        //TODO: finish this extension BIG JOB!
        /*EmbeddedStylesheetDefinition styleSheet;
        SerializationHandler handler = context.getTransformer().getSerializationHandler();
        if(!(handler instanceof SXEMultithreadedSerializationHandler)){
            //we're still using an old handler here, better do something with it
            handler = new SXEMutlithreadedSerializationHander(handler);
            context.getTransformer().setSerializationHandler(handler);
        }
        //we have to swap out the serialization handler here
        //TODO: change this to use read/write lock to speed things up
        synchronized(stylesheetCache){
            if(!stylesheetCache.containsKey(extensionElement)){
                stylesheetCache.put(extensionElement, XSLTUtil.createNewStylesheetFromChildren(context, extensionElement));
            }
            styleSheet = stylesheetCache.get(extensionElement);
        }
        ThreadedXalanProcessor processor = new ThreadedXalanProcessor(
                context,
                extensionElement,
                runningThreads,
                styleSheet,
                ((SXEMutlithreadedSerializationHander)handler.spawnResults()));
        if(hasAttribute(extensionElement, "pool")){
            try{
                ThreadPoolExecutor executor = (ThreadPoolExecutor)getXObject("pool", context, extensionElement).object();
                executor.execute(processor);
            }catch(Exception e){
                throw new RuntimeException("Could not get reference to thread pool");
            }
        }else{
            processor.start();
        }*/
    }

    /**
     * Creates a thread pool for use with parallel processing.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code><pre>
     * &lt;xsl:variable name="pool"&gt;
     *  &lt;concurrent:threadPool minSize="1" maxSize="10"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>minSize</td><td>XPath</td><td>The minimum available threads to create.</td><td>No (defaults to 1)</td></tr>
     *  <tr><td>maxize</td><td>XPath</td><td>The maximum available threads to create.</td><td>No (defaults to 5)</td></tr>
     *  <tr><td>timeout</td><td>XPath</td><td>The timeout for idle threads (in seconds).</td><td>No (defaults to 60)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void threadPool(XSLProcessorContext context, ElemExtensionCall extensionElement){
        int min = getIntegerXPath("minSize", context, extensionElement, 1);
        int max = getIntegerXPath("maxSize", context, extensionElement, 5);
        int timeout = getIntegerXPath("timeout", context, extensionElement, 60);
        setVariableIfPossible(new ThreadPoolExecutor(min, max,timeout, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>()),
                                extensionElement);

    }

    /**
     * Objtains a reentrant read/write lock for use somewhere else in your document.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>
     * <pre>
     *  &lt;xsl:variable name="mylock"&gt;
     *      &lt;concurrent:readWriteLock/&gt;
     *  &lt;/xsl:variable&gt;
     * </pre>
     * </code>
     * @param context
     * @param extensionElement
     * @throws TransformerException
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void readWriteLock(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, MalformedURLException, FileNotFoundException, IOException{
        setVariableIfPossible(new ReentrantReadWriteLock(), extensionElement);
    }

    /**
     * Locks a previously created lock for reading.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;concurrent:lockForReading lock="$myLock"/&gt;</code>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void lockForReading(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{ 
        ReentrantReadWriteLock lock = getLock(context, extensionElement);
        if(lock != null) lock.readLock().lock();
    }

    /**
     * Locks a previously created lock for writing.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;concurrent:lockForWriting lock="$myLock"/&gt;</code>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void lockForWriting(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        ReentrantReadWriteLock lock = getLock(context, extensionElement);
        if(lock != null) lock.writeLock().lock();
    }

    /**
     * Locks a previously created unlock for reading.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;concurrent:unLockForReading lock="$myLock"/&gt;</code>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void unLockForReading(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        ReentrantReadWriteLock lock = getLock(context, extensionElement);
        if(lock != null) lock.readLock().unlock();
    }

    /**
     * Locks a previously created lock for reading.
     * <br/><br/>
     * Usage Example:
     * <br/><br/>
     * <code>&lt;concurrent:unLockForReading lock="$myLock"/&gt;</code>
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void unLockForWriting(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        ReentrantReadWriteLock lock = getLock(context, extensionElement);
        if(lock != null) lock.writeLock().unlock();
    }

    private ReentrantReadWriteLock getLock(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        if(!passesAttributeValidation(extensionElement, context, "lock")) return null;
        XObject obj = getXObject("lock", context, extensionElement);
        if(obj.object() instanceof ReentrantReadWriteLock){
            return (ReentrantReadWriteLock)obj.object();
        }else{
            logError(this.getClass(), extensionElement, "Attribute 'lock' must reference a previously created <concurrent:readWriteLock/>", context);
            return null;
        }
    }

}
