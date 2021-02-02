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

import net.adamjenkins.sxe.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.trace.TracerEvent;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNull;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Elements for performing assertions in XSLT<br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 * 
 * <h3>SXE Assertion Framework</h3>
 * These elements are used for placing assertions into an xslt document so we know if
 * something has gone wrong during processing.
 * <br/><br/>
 * Usage Example:
 * <br/><br/>
 * To use SXE xslt assertion framework, you must register it with the xslt processor,
 * initialize it, and finalize it when your finished and you want to apply your assertions.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:assert="xalan://net.adamjenkins.sxe.elements.Assertion" extension-element-prefixes="assert" ... &gt;
 * </code>
 * <br/><br/>
 * To initialize and finalize the framework, use the following calls:
 * <br/><br/>
 * To initialize:  <code>&lt;assert:init/&gt;</code><br/>
 * To finalize and apply the assertions: <code>&lt;assert:finalize/&gt;</code>
 * <br/><br/>
 * The assertion framework must always be initialize and finalized to work effectively!  It may optionally be initialized
 * with a timeout value in milliseconds, see the init() method javadoc for more information.
 * <br/><br/>
 * Assertions use the attribute 'targetId' to point to the id of an assertion token that appears somewhere in your document.
 * <br/><br/>
 * <code>
 * &lt;assert:token id="someid"/&gt;
 * </code>
 * <br/><br/>
 * Once your target element has an id, you can set up a range of assertions:
 * <br/>
 * <code>&lt;assert:applyLimit name="somekey" maxLimit="10"/&gt;</code> asserts that an element is not called more than the limit set (used as a timeout usually)<br/>
 * <code>&lt;assert:token id="someid"/&gt;</code> a token that is used as a target for all other assertions<br/>
 * <code>&lt;assert:called targetId="someid"/&gt;</code> asserts the target element is called once and once only<br/>
 * <code>&lt;assert:called targetId="someid" numberOfTimes="2"/&gt;</code> asserts the target element is called exactly twice<br/>
 * <code>&lt;assert:calledAtLeast targetId="someid" numberOfTimes="2"/&gt;</code> asserts the target element is called at least twice.<br/>
 * <code>&lt;assert:notCalled targetId="someid"/&gt;</code> asserts the target element is never called.<br/>
 * <code>&lt;assert:equal arg1="somexpath" arg2="somexpath"/&gt;</code> asserts that the two xpath locations or variables are equal<br/>
 * <code>&lt;assert:notEqual arg1="somexpath" arg2="somexpath"/&gt;</code> asserts that the two xpath locations or variables are not equal<br/>
 * <code>&lt;assert:isTrue test="some boolean expression"/&gt;</code> asserts the test<br/>
 * <code>&lt;assert:exists select="some xpath"/&gt;</code> asserts that something exists (is not null)<br/>
 * <br/>
 * Check the javadoc for the individual assertion methods (i.e. elements) for more usage examples.
 * 
 * 
 * @author <a href="mailto:mail.at.adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Assertion extends AbstractExtensionElement {

    private static final Logger log = LoggerFactory.getLogger(Assertion.class);
    //if we can't finish processing one website in 20 minutes, we've got problems
    private int maxProcessingTime = 1200000;
    private Map<String, Integer> assertCalledIds = new HashMap<String, Integer>();
    private Map<String, Integer> assertCalledAtLeastIds = new HashMap<String, Integer>();
    private Map<String, Integer> assertCalledTracker = new HashMap<String, Integer>();
    private List<String> assertNotCalledIds = new ArrayList<String>();
    private Map<String, Integer> limitTracker = new HashMap<String, Integer>();
    private long startTime;
    private boolean suspendTimeout = false;
    private boolean initialized = false;

    /**
     * This element initialized the assertion framework.  Optionally, a timeout can be specified.  Specify a timeout if
     * you wish to impose a processing limit (in milliseconds from initialization) on your XSLT.
     * <br/><br/>
     *
     * <code>&lt;assert:init timeout="600000"/&gt;</code> would initialize the assertion framework, and enforce that this XSLT did not
     * process for more than 10 minutes.
     * <br/><br/>
     * If, during processing of your document, you wish to suspend the timeout, you can use the element:
     * <br/><br/>
     * <code>&lt;assert:suspendTimeout/&gt;</code>
     * <br/><br/>
     * If you do not wish to impose a timeout value, simply initialize the assertion framework thus:
     * <br/><br/>
     * <code>&lt;assert:init/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>timeout</td><td>XPath</td><td>A timeout for the processing of the document.</td><td>No</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public synchronized void init(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        if (initialized) {
            return;
        }
        startTime = System.currentTimeMillis();
        log.debug("init");
        try {
            context.getTransformer().getTraceManager().addTraceListener(this);
            initialized = true;            
        } catch (Throwable t) {
            logError(this.getClass(), "Failed to initialize assertion framework: " + t.getMessage(), context);
        }
        String timeoutString = extensionElement.getAttribute("timeout");
        try{            
            if(StringUtils.isNotEmpty(timeoutString)) maxProcessingTime = Integer.parseInt(timeoutString);
            else suspendTimeout(context, extensionElement);
        }catch(NumberFormatException e){
            logError(this.getClass(), "Invalid timeout " + timeoutString + " timeout value must be a valid number - defaulting to " + maxProcessingTime, context);
        }
    }

    /**
     * Finalizes the assertions.  This method allows the assertion framework to clean up after itself and also to perform
     * checking on how many times each assertion token was called.
     * <br/><br/>
     * Usage:
     * <br/><br/>
     * <code>&lt;assert:finalize/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public synchronized void finalize(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        processAssertCalled(context);
        processAssertCalledAtLeast(context);
    }

    /**
     * If, during processing of a document, you need to make the decision that a timeout value is no longer necessary, you can
     * suspend the timeout using this element (provided the timeout value has not already been exceeded).
     * <br/><br/>
     * <code>&lt;assert:suspendTimeout/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void suspendTimeout(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        suspendTimeout = true;
    }

    /**
     * A lot of the more involved assertions (assertCalled, assertCalledAtLeast) use a targetId attribute which points to the unique id of
     * an assertion token.  Place these tokens throughout your document as points for the assertion framework to check the assertions you have configured.
     * <br/><br/>
     * You may configure the assertions at any point in your document, however they will only work with subsequent calls to the token they are watching.
     * <br/><br/>
     * Usage:
     * <br/><br/>
     * <code>&lt;assert:token id='mytoken'/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>id</td><td>Template</td><td>An id that will be used with other assertion elements to reference this point in the document.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void token(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "id")) return;
        String id = getAttribute("id", context, extensionElement);
        if (assertNotCalledIds.contains(id)) {
            logError(new TransformerException("Node '" + id + "' was called but was registered as 'assertNotCalled'"), context);
        }
        if (assertCalledAtLeastIds.keySet().contains(id) || assertCalledIds.keySet().contains(id)) {
            if (assertCalledTracker.keySet().contains(id)) {
                int count = assertCalledTracker.get(id);
                ++count;
                assertCalledTracker.put(id, count);
            } else {
                assertCalledTracker.put(id, 1);
            }
        }
    }

    /**
     * Applies a maximum limit on the number of time that THIS ELEMENT (or this element and any other applyLimit elements with the same name) can be processed.
     * <br/><br/>
     * Usage:
     * <br/><br/>
     * <code>&lt;assert:applyLimit name='notMoreThan10Times' maxLimit='10'/&gt;</code>
     * <br/><br/>
     * You can also use either an xpath, or a variable reference for the maxLimit attribute, e.g.
     * <br/><br/>
     * <code>&lt;assert:applyLimit name="notMoreThanXTimes' maxLimit='$somePredefinedLimit'/&gt;</code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code>&lt;assert:applyLimit name="notMoreThanXTimes' maxLimit='someElement/@someAttribute'/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>A name that is used to track calls (multiple apply limits can share the same name).</td><td>Yes</td></tr>
     *  <tr><td>maxLimit</td><td>XPath</td><td>A maximum number of times that the XSLT process can encounter either this tag, or other applyLimit tags with the same name.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void applyLimit(XSLProcessorContext context, final ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "name", "maxLimit")) return;
        String name = getAttribute("name", context, extensionElement);
        int maxLimit = Integer.parseInt(getXPath("maxLimit", context, extensionElement));
        Integer currentValue = limitTracker.get(name);
        if (currentValue == null) {
            currentValue = 0;
        }
        limitTracker.put(name, ++currentValue);
        if (currentValue >= maxLimit) {
            logError(new TransformerException("The limit for key '" + name + "' of " + maxLimit + " was exceeded."), context);
        }
    }

    /**
     * Causes the assertion framework to 'fail', that is, register an assertion failure.
     * <br/><br/>
     * <code>&lt;fail/&gt;</code>
     * <br/><br/>
     * or
     * <br/><br/>
     * <code>&lt;fail message="failed on element {/someelement/somechild} because it's incorrect structure"/&gt;</code>
     * <br/><br/>
     * note that you can put xpath declarations inside curly braces { } to have them evaluated and the result added to your
     * message.
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>message</td><td>Template</td><td>The message to put in the error caused by this fail tag.</td><td>No</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void fail(XSLProcessorContext context, final ElemExtensionCall extensionElement){
        checkTimeout();
        debug(extensionElement);
        String message = "An <assert:fail/> was encountered";
        if (hasAttribute(extensionElement, "message")) {
            message = getAttribute("message", context, extensionElement);
        }
        logError(this.getClass(), extensionElement, "Assertion failed: " + message, context);
    }

    /**
     * Asserts that the results of a given xpath are not empty or null.
     * <br/><br/>
     * <code>&lt;assert:exists select="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>select</td><td>XPath</td><td>A reference to the item that will be checked to see if it exists or not.</td><td>Yes</td></tr>
     * </table>
     *
     * @param context
     * @param extensionElement
     * @throws TransformerException 
     */
    public void exists(XSLProcessorContext context, final ElemExtensionCall extensionElement) throws TransformerException {
        checkTimeout();
        debug(extensionElement);
        if (!passesAttributeValidation(extensionElement, context, "select")) return;
        XObject obj = getXObject("select", context, extensionElement);
        if (isNull(obj)) {
            logError(this.getClass(), extensionElement, "Assertion failed (assertExists), result of expression " + extensionElement.getAttribute("select") + " is null", context);
        }
    }


    /**
     * Asserts that an assertion token with the given id is called a number of times (exactly).  If no numberOfTimes attribute
     * is specified, the number of times will default to 1.
     * <br/><br/>
     * <code>&lt;assert:called targetId="mytoken"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:called targetId="mytoken" numberOfTimes="3"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:called targetId="mytoken" numberOfTimes="$expectedNumberOfTimes"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:called targetId="mytoken" numberOfTimes="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * note, that all of the above assume there is a token somewhere in your xslt declared thus:
     * <br/><br/>
     * <code>&lt;assert:token id="mytoken"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>targetId</td><td>Template</td><td>The id of the assert:token that this assertion applies to.</td><td>Yes</td></tr>
     *  <tr><td>numberOfTimes</td><td>XPath</td><td>The number of times the target element must be processed by the XSLT engine.</td><td>No (defaults to 1)</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void called(XSLProcessorContext context, final ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "targetId")) return;
        String id = getAttribute("targetId", context, extensionElement);
        String numOfTimesString = getXPath("numberOfTimes", context, extensionElement);
        try {
            int times = StringUtils.isEmpty(numOfTimesString) ? 1 : Integer.parseInt(numOfTimesString);
            assertCalledIds.put(id, times);
        } catch (NumberFormatException e) {
            logError(this.getClass(),
                     extensionElement,
                     "Attribute 'numberOfTimes' must evaluate to a valid integer: " + e.getMessage(),
                     context);
        }
    }

    /**
     * Asserts that an assertion token with the given id is called at least a specified number of times.
     * You must specify a numberOfTimes (it's not optional).
     * <br/><br/>
     * <code>&lt;assert:calledAtLeast targetId="mytoken" numberOfTimes="3"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:calledAtLeast targetId="mytoken" numberOfTimes="$expectedNumberOfTimes"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:calledAtLeast targetId="mytoken" numberOfTimes="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * note, that all of the above assume there is a token somewhere in your xslt declared thus:
     * <br/><br/>
     * <code>&lt;assert:token id="mytoken"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>targetId</td><td>Template</td><td>The id of the assert:token that this assertion applies to.</td><td>Yes</td></tr>
     *  <tr><td>numberOfTimes</td><td>XPath</td><td>The minimum number of times the target element must be processed by the XSLT engine.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void calledAtLeast(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "targetId", "numberOfTimes")) return;
        try {
            int numberOfTimes = Integer.parseInt(getXPath("numberOfTimes", context, extensionElement));
            assertCalledAtLeastIds.put(extensionElement.getAttribute("targetId"), numberOfTimes);
        } catch (NumberFormatException e) {
        }
    }

    /**
     * Asserts that a token is not called.
     * <br/><br/>
     * <code>&lt;assert:notCalled targetId="mytoken"/&gt;</code>
     * <br/><br/>
     * note, that all of the above assume there is a token somewhere in your xslt declared thus:
     * <br/><br/>
     * <code>&lt;assert:token id="mytoken"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>targetId</td><td>Template</td><td>The id of the assert:token that this assertion applies to.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void notCalled(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "targetId")) return;
        assertNotCalledIds.add(getAttribute("targetId", context, extensionElement));
    }

    /**
     * Asserts that the results of two xpath evaluations are equal
     * <br/><br/>
     * <code>&lt;assert:equal arg1="$somevar" arg2="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:equal arg1="3" arg2="count(/someelement)"/&gt;</code>
     * <br/><br/>
     *
     * NOTE:  If you want to use a string value as one of the arguments, you have to put it into
     * single quotes as the arg1 and arg2 attributes expect xpath expressions, not literals.
     * <br/>
     * e.g.
     * <br/>
     * <code>&lt;assert:equal arg1="'bob'" arg2="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>arg1</td><td>XPath</td><td>An expression that evaluates to the first argument of the equal test.</td><td>Yes</td></tr>
     *  <tr><td>arg2</td><td>XPath</td><td>An expression that evaluates to the second argument of the equal test.</td><td>Yes</td></tr>     
     * </table>
     *
     * @param context
     * @param extensionElement
     */
    public void equal(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "arg1", "arg2")) return;
        String base = getXPath("arg1", context, extensionElement);
        String test = getXPath("arg2", context, extensionElement);
        if (!base.equals(test)) {
            logError(this.getClass(),
                     extensionElement,
                     "Assertion failed (assertEqual), arg1 and arg2 are not equal.",
                     context);
        }
    }

    /**
     * Asserts that the results of two xpath evaluations are not equal
     * <br/><br/>
     * <code>&lt;assert:notEqual arg1="$somevar" arg2="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * <code>&lt;assert:notEqual arg1="3" arg2="count(/someelement)"/&gt;</code>
     * <br/><br/>
     * NOTE:  If you want to use a string value as one of the arguments, you have to put it into
     * single quotes as the arg1 and arg2 attributes expect xpath expressions, not literals.
     * <br/>
     * e.g.
     * <br/>
     * <code>&lt;assert:notEqual arg1="'bob'" arg2="/someelement/@someattribute"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>arg1</td><td>XPath</td><td>An expression that evaluates to the first argument of the not equal test.</td><td>Yes</td></tr>
     *  <tr><td>arg2</td><td>XPath</td><td>An expression that evaluates to the second argument of the not equal test.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void notEqual(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "arg1", "arg2")) return;
        String base = getXPath("arg1", context, extensionElement);
        String test = getXPath("arg2", context, extensionElement);
        if (base.equals(test)) {
            logError(this.getClass(),
                    extensionElement,
                    "Assertion failed (assertNotEqual), arg1 and arg2 are equal.",
                    context);
        }
    }

    /**
     * Asserts that the string representation of the results of an xpath evaluation matches a given regular expression.
     * <br/><br/>
     * <code>&lt;assert:matches pattern="'\d{3}'" test="count(/someelement/somechild)"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>pattern</td><td>XPath</td><td>The regular expression (NOTE: if you're using a literal, it must be inside single quotes).</td><td>Yes</td></tr>
     *  <tr><td>test</td><td>XPath</td><td>An expression that evaluates to either true or false.</td><td>Yes</td></tr>
     * </table>
     *
     * @param context
     * @param extensionElement
     */
    public void matches(XSLProcessorContext context, ElemExtensionCall extensionElement){
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "test", "pattern")) return;
        String pattern = getXPath("pattern", context, extensionElement);
        String test = getXPath("test", context, extensionElement);
        if(!test.matches(pattern)){
            logError(this.getClass(),
                     extensionElement,
                     "Assertion failed (matches): '" + test + "' does not match the regular expression '" + pattern + "'",
                     context);
        }
    }

    /**
     * Asserts that an xpath evaluates to true.
     * <br/><br/>
     * <code>&lt;assert:isTrue test="count(/someelement/somechild) > 0"/&gt;</code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>test</td><td>XPath</td><td>An expression that evaluates to either true or false.</td><td>Yes</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     */
    public void isTrue(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException {
        checkTimeout();
        debug(extensionElement);
        if(!passesAttributeValidation(extensionElement, context, "test")) return;
        boolean test = getXObject("test", context, extensionElement).bool();
        if (!test) {
            logError(this.getClass(), 
                    extensionElement, "Assertion failed (isTrue: " + extensionElement.getAttribute("test") + ")",
                    context);
        }
    }

    @Override
    public void trace(TracerEvent event) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("trace");
                log.debug("Mode: " + event.m_mode);
                log.debug("Source Node: " + event.m_sourceNode);
                log.debug("StyleSheet Node: " + event.m_styleNode);
            }
            Node styleSheetNode = event.m_styleNode;
            if (styleSheetNode.getNodeType() == Node.ELEMENT_NODE && !(styleSheetNode instanceof ElemTemplateElement)) {
                Element element = (Element) styleSheetNode;
                String id = element.getAttribute("id");
                if (assertNotCalledIds.contains(id)) {
                    logError(new TransformerException("Node '" + id + "' was called but was registered as 'assertNotCalled'"), event.m_processor.getErrorListener());
                }
                if (assertCalledAtLeastIds.keySet().contains(id) || assertCalledIds.keySet().contains(id)) {
                    if (assertCalledTracker.keySet().contains(id)) {
                        int count = assertCalledTracker.get(id);
                        ++count;
                        assertCalledTracker.put(id, count);
                    } else {
                        assertCalledTracker.put(id, 1);
                    }
                }
            }
        } catch (Throwable t) {
            //we don't want to let any errors get out of the trace method
            //xalan can throw lots of different errors for different reasons
            //none of which are really relevant to things...we'll log them and move on
            log.warn("Error tracing assertion element.", t);
        }
    }

    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public void setMaxProcessingTime(int maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    private void checkTimeout() {
        long timeRunning = System.currentTimeMillis() - startTime;
        if (timeRunning > maxProcessingTime && !suspendTimeout) {
            throw new RuntimeException("Xalan Transformer Timeout!");
        }
    }

    private void processAssertCalled(XSLProcessorContext context) {
        for (String id : assertCalledIds.keySet()) {
            int numberOfTimes = assertCalledIds.get(id);
            if (assertCalledTracker.containsKey(id)) {
                int actual = assertCalledTracker.get(id);
                if (numberOfTimes != actual) {
                    logError(this.getClass(), 
                            "Element with id " + id + " was called " + actual + " times, but was supposed to be called " + numberOfTimes + " times.",
                            context);
                }
            } else {
                logError(this.getClass(), 
                        "Element with id " + id + " was not called, but was supposed to be called " + numberOfTimes + " times.",
                        context);
            }
        }
    }

    private void processAssertCalledAtLeast(XSLProcessorContext context) {
        for (String id : assertCalledAtLeastIds.keySet()) {
            int numberOfTimes = assertCalledAtLeastIds.get(id);
            if (assertCalledTracker.containsKey(id)) {
                int actual = assertCalledTracker.get(id);
                if (numberOfTimes > actual) {
                    logError(this.getClass(), 
                            "Element with id " + id + " was called " + actual + " times, but was supposed to be called at least " + numberOfTimes + " times.",
                            context);
                }
            } else {
                logError(this.getClass(), 
                        "Element with id " + id + " was not called, but was supposed to be called at least " + numberOfTimes + " times.",
                        context);
            }
        }
    }

}
