/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.xalan.templates;


/**
 *
 * A helper to inject variles into a stylesheet.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class VarBridge {

    public static void addVariable(ElemVariable var, Stylesheet sheet){
        StylesheetRoot sroot = (StylesheetRoot)sheet;
        sroot.recomposeVariables(var);
    }

}
