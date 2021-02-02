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
package net.adamjenkins.sxe.util;

import javax.xml.transform.SourceLocator;
import org.apache.xalan.templates.ElemExtensionCall;

/**
 * Locates where an extension element is in the document.  Used mainly for errors (gets the line and column number).
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class ExtensionElementSourceLocator implements SourceLocator{

    private ElemExtensionCall element;

    public ExtensionElementSourceLocator(ElemExtensionCall element){
        this.element = element;
    }

    public String getPublicId() {
        return element.getPublicId();
    }

    public String getSystemId() {
        return element.getSystemId();
    }

    public int getLineNumber() {
        return element.getLineNumber();
    }

    public int getColumnNumber() {
        return element.getColumnNumber();
    }    

}
