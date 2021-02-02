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
package net.adamjenkins.sxe.elements.scratchpad;

import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.VarBridge;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XString;
import org.w3c.dom.NodeList;

/**
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Scratchpad {

    private long id;

    public Scratchpad(){
        id = System.currentTimeMillis();
    }

    public static void setVar(XSLProcessorContext context, ElemExtensionCall extensionElement){
        ElemVariable var = new ElemVariable();
        var.setName(new QName("test"));
        var.setSelect(new XPath(new XString("blah")));
        VarBridge.addVariable(var, context.getStylesheet());
    }

    public static void outerStatic(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        System.out.println("OUTER STATIC " + System.currentTimeMillis());
    }

    public void outer(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException {
        System.out.println("OUTER " + id);
        NodeList list = extensionElement.getChildNodes();
        for(int i = 0; i < list.getLength(); i++){
            System.out.println(list.item(i).getNodeName());
        }
        context.getTransformer().executeChildTemplates(extensionElement, true);
    }

    public void inner(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException {
        System.out.println("INNER " + id);
        context.getTransformer().executeChildTemplates(extensionElement, true);
    }

}
