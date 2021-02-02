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
 * Represents the configuration for a series.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class SeriesConfiguration extends DataConfiguration{

    public enum DataType {INTEGER, LONG, DECIMAL}

    private DataType dataType = DataType.INTEGER;
    
    public SeriesConfiguration(Charting.Type type, XSLProcessorContext context, ElemExtensionCall extensionElement, ErrorListener listener) throws ChartDataConfigurationException{
        super(type, context, extensionElement, listener);
        if(XSLTUtil.hasAttribute(extensionElement, "dataType")){
            dataType = DataType.valueOf(XSLTUtil.getAttribute("dataType", context, extensionElement));
        }
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Number parseValue(String value){
        switch(dataType){
            case DECIMAL:
                return Double.parseDouble(value);
            case LONG:
                return Long.parseLong(value);
            case INTEGER:
            default:
                return Integer.parseInt(value);
        }
    }

}
