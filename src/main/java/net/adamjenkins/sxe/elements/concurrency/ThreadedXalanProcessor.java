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
package net.adamjenkins.sxe.elements.concurrency;

import java.util.HashSet;
import net.adamjenkins.sxe.util.XSLTUtil;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A Xalan processor that runs on a seperate thread.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class ThreadedXalanProcessor extends Thread implements Runnable{

    private XSLProcessorContext ctx;
    private ElemExtensionCall extensionElement;
    private boolean completed = false;
    private HashSet<Thread> runningThreads;
    private EmbeddedStylesheetDefinition stylesheet;

    public ThreadedXalanProcessor(XSLProcessorContext ctx,
                                  ElemExtensionCall extensionElement,
                                  HashSet<Thread> runningThreads,
                                  EmbeddedStylesheetDefinition stylesheet){
        this.ctx = ctx;
        this.extensionElement = extensionElement;
        this.runningThreads = runningThreads;
        this.stylesheet = stylesheet;
        runningThreads.add(this);
    }

    @Override
    public void run(){
        //we need to change this so that it creates a stylesheet with all the trimmings, copies over all the variables
        //as parameters, and executes each of the new stylesheet as completely indepependant transformations
        //then incorporates the results
        try{
            Node contextNode = ctx.getContextNode();
            SAX2DOM dom = new SAX2DOM();
            ctx.getTransformer().executeChildTemplates(extensionElement, dom);
            synchronized(ctx.getStylesheet()){
                ctx.outputToResultTree(ctx.getStylesheet(), dom.getDOM());
            }
        }catch(Throwable t){
            XSLTUtil.logError(this.getClass(), extensionElement, "Error performing parallel XSLT processing: " + t.getMessage(), ctx.getTransformer().getErrorListener());
        }finally{
            completed = true;
            runningThreads.remove(this);
        }
    }

    public boolean isCompleted(){
        return completed;
    }


}
