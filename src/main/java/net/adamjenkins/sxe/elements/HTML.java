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

/**
 * The HTML elements are a group of elements for working with a HTML page returned from the doc:proprietaryDocument() function.  It
 * allows interaction with elements on the page such as filling out input and clicking links and buttons and interpretting the results or
 * including them into the generated document.
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE HTML Framework</h3>
 *
 * <br/><br/>
 * A range of utilities for interacting with HTML pages.
 *
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:html="xalan://net.adamjenkins.sxe.elements.HTML" extension-element-prefixes="http" ... &gt;
 * </code>
 * <br/><br/>
 *
 * <h3>DO NOT USE THIS ELEMENT -- UNDER CONSTRUCTION</h3>
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class HTML extends AbstractExtensionElement{

}
