/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.xalan.templates;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;

import net.adamjenkins.sxe.util.XSLTUtil;

/**
 *
 * A helper to inject variles into a stylesheet.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class VarBridge {

    public static void addVariable(XSLProcessorContext context, String variableName, Object var){
        XObject xObjVar = new XObject(var);
        QName qName = new QName(variableName);
        StylesheetRoot.ComposeState cstate = context.getStylesheet().getStylesheetRoot().getComposeState();
        int m_index = cstate.addVariableName(qName) - cstate.getGlobalsSize();
        context.getTransformer().getXPathContext().getVarStack().setLocalVariable(m_index, xObjVar);
    }
    
    public static void fixupVariables(XPath expression, Stylesheet sheet) {
    	StylesheetRoot sroot = sheet.getStylesheetRoot();
    	//cstate.pushStackMark();
        java.util.Vector vnames = sroot.getComposeState().getVariableNames();

        if (null != expression)
          expression.fixupVariables(
            vnames, sroot.getComposeState().getGlobalsSize());
    }
    


}
