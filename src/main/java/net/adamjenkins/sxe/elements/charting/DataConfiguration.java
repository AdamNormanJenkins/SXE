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
package net.adamjenkins.sxe.elements.charting;

import javax.xml.transform.ErrorListener;
import net.adamjenkins.sxe.elements.Charting;
import net.adamjenkins.sxe.util.XSLTUtil;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;

/**
 * Shared configuration information for series and categories.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class DataConfiguration{

    private String name;

    public DataConfiguration(Charting.Type graphType,
                             XSLProcessorContext context,
                             ElemExtensionCall extensionElement,
                             ErrorListener listener) throws ChartDataConfigurationException{
        if(!XSLTUtil.passesAttributeValidation(this.getClass(), extensionElement, listener, "name"))
            throw new ChartDataConfigurationException();
        this.name = XSLTUtil.getAttribute("name", context, extensionElement);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
}
