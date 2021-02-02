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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;

/**
 * Elements for calling EJB 3.0 session beans (for entity beans, use the {@link net.adamjenkins.sxe.elements.JPA} framework).
 *
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE EJB Framework</h3>
 * <br/><br/>
 * This framework allows you to lookup session beans (remote or local, stateless or stateful) and assign the result to a XSL variable for use
 * with the {@link net.adamjenkins.sxe.elements.JavaBean} framework.  It also caches the interfaces to performance optimization.
 * <br/><br/>
 * To register the EJB elements with Xalan, add the following to your stylesheet declaration:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:ejb="xalan://net.adamjenkins.sxe.elements.EJB" extension-element-prefixes="ejb" ... &gt;
 * </code>
 * <br/><br/>
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class EJB extends AbstractExtensionElement{

    private HashMap<ContextKey, Object> cache = new HashMap<ContextKey,Object>();

    /**
     * Looks up an ejb and assigns it to a variable.  This element also provides a cache of ejb references.
     * <br/><br/>
     * Usage example:
     * <code><pre>
     * &lt;xsl:variable name="myejb"&gt;
     *  &lt;ejb:lookup context="$mycontext" ref="ejb/MyBeanRemote"/&gt;
     * &lt;/xsl:variable&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>context</td><td>XPath</td><td>The JNDI context to use.</td><td>No (if none supplied, the default context will be used)</td></tr>
     *  <tr><td>ref</td><td>XPath</td><td>The JNDI lookup string for this EJB.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException
     * @throws TransformerException
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NamingException
     */
    public void lookup(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, MalformedURLException, FileNotFoundException, IOException, NamingException{
        if(!passesAttributeValidation(extensionElement, context, "ref")) return;
        String s = getAttribute("ref", context, extensionElement);
        InitialContext resolvedContext = getInitialContext(context, extensionElement);
        if(resolvedContext == null) return;
        ContextKey newKey = new ContextKey(resolvedContext, s);
        if(!cache.containsKey(newKey)) {
            Object o = resolvedContext.lookup(s);
            cache.put(newKey, o);
        }
        if(!setVariableIfPossible(cache.get(newKey), extensionElement)) context.outputToResultTree(context.getStylesheet(), cache.get(newKey));
    }

    private class ContextKey{
        private Context ctx;
        private String ref;
        ContextKey(Context ctx, String ref){
            this.ctx = ctx;
            this.ref = ref;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ContextKey other = (ContextKey) obj;
            if (this.ctx != other.ctx && (this.ctx == null || !this.ctx.equals(other.ctx))) {
                return false;
            }
            if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.ctx != null ? this.ctx.hashCode() : 0);
            hash = 29 * hash + (this.ref != null ? this.ref.hashCode() : 0);
            return hash;
        }
        
    }
}
