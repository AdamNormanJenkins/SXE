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

import java.awt.Color;
import java.awt.Stroke;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.transform.ErrorListener;
import net.adamjenkins.sxe.elements.Charting;
import net.adamjenkins.sxe.util.XSLTUtil;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.xml.ParserUtil;

/**
 * Configuration for a charting category.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class CategoryConfiguration extends DataConfiguration {

    private enum TimePeriod {YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILLISECOND;
        Class getTimePeriodClass(){
            switch(this){
                case YEAR:
                    return org.jfree.data.time.Year.class;
                case MONTH:
                    return org.jfree.data.time.Month.class;
                case WEEK:
                    return org.jfree.data.time.Week.class;
                case DAY:
                    return org.jfree.data.time.Day.class;
                case HOUR:
                    return org.jfree.data.time.Hour.class;
                case MINUTE:
                    return org.jfree.data.time.Minute.class;
                case SECOND:
                    return org.jfree.data.time.Second.class;
                case MILLISECOND:
                    return org.jfree.data.time.Millisecond.class;
            }
            throw new AssertionError("Unknown time period " + this);
        }
    }

    Class timePeriodClass;
    private SimpleDateFormat formatter;
    private Color color;
    private Stroke stroke;
    
    public CategoryConfiguration(Charting.Type chartType,
                                 XSLProcessorContext context,
                                 ElemExtensionCall extensionElement,
                                 ErrorListener listener) throws ChartDataConfigurationException{
        super(chartType, context, extensionElement, listener);
        if(chartType == Charting.Type.TIMESERIES){
            if(!XSLTUtil.passesAttributeValidation(this.getClass(), extensionElement, listener, "timePeriod", "dateTimeFormat"))
                throw new ChartDataConfigurationException();
            timePeriodClass = TimePeriod.valueOf(XSLTUtil.getAttribute("timePeriod", context, extensionElement)).getTimePeriodClass();
            formatter = new SimpleDateFormat(XSLTUtil.getAttribute("dateTimeFormat", context, extensionElement));
        }
        if(XSLTUtil.hasAttribute(extensionElement, "color")){
            color = ParserUtil.parseColor(XSLTUtil.getAttribute("color", context, extensionElement));
        }
        if(XSLTUtil.hasAttribute(extensionElement, "weight")){
            stroke = ParserUtil.parseStroke(XSLTUtil.getXPath("weight", context, extensionElement));
        }
    }

    public void setTimeValueFormat(String timeValueFormat) {
        this.formatter = new SimpleDateFormat(timeValueFormat);
    }

    public Class getTimePeriodClass() {
        return timePeriodClass;
    }

    public void setTimePeriodClass(Class timePeriodClass) {
        this.timePeriodClass = timePeriodClass;
    }
    
    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public RegularTimePeriod parseTimeValue(String source) throws ParseException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        if(formatter == null) throw new RuntimeException("Could not parse time value " + source + " for chart.  No time format was configured for this category");
        Date d = formatter.parse(source);
        Constructor c = timePeriodClass.getConstructor(Date.class);
        return (RegularTimePeriod)c.newInstance(d);
    }

}
