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

import static net.adamjenkins.sxe.util.XSLTUtil.getXObject;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import net.adamjenkins.sxe.elements.charting.CategoryConfiguration;
import net.adamjenkins.sxe.elements.charting.ChartDataConfigurationException;
import net.adamjenkins.sxe.elements.charting.SeriesConfiguration;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang3.StringUtils;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.objects.XObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.util.TableOrder;
import org.jfree.xml.ParserUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Element for creating simple charts/graphs.
 * <br/><br/>
 * <b>Read This First: </b><a href="package-summary.html#package_description">Introduction to SXE Elements</a>
 * <br/><br/>
 *
 * <h3>SXE Charting Framework</h3>
 * These are some elements for creating simpel graphs/charts in XSLT.  At current, this only supports 4 types of graphs
 * (Pie, Bar, Line and TimeSeries - and 3d version of the first 3).  It does not support all the
 * different configuration that is available through JFreeCharts, but is expected to
 * become more feature rich over time.
 * <br/><br/>
 * To register it with the xslt processor, add it to the root element thus:
 * <br/><br/>
 * <code>
 * &lt;xsl:stylesheet ... xmlns:chart="xalan://net.adamjenkins.sxe.elements.Charting" extension-element-prefixes="chart" ... &gt;
 * </code>
 * <br/><br/>
 * Then you need to create a chart using combinations of the image, chart,
 * series, category and addValue tags.  At this stage, only one chart is supported per XSLT, however
 * we're working on making this more feature rich.
 * <br/><br/>
 * Example Usage:
 * <br/><br/>
 * <code>
 * <pre>
 * &lt;template match="/"&gt;
 *  &lt;chart:image width="300" height="$chartHeight"&gt;
 *      &lt;chart:chart type="BAR" title="My Bar Chart" is3D="true" orientation="VERTICAL"&gt;
 *          &lt;chart:category name="Australian Division" color="#123456"/&gt;
 *          &lt;chart:category name="United States Division" color="blue"/&gt;
 *          &lt;chart:series name="First Quater Profits" dataType="DECIMAL"/&gt;
 *          &lt;chart:series name="Second Quater Profits" dataType="DECIMAL"/&gt;
 *          &lt;chart:series name="Third Quater Profits" dataType="DECIMAL"/&gt;
 *          &lt;xsl:apply-templates select="first-q/profits"/&gt;
 *      &lt;/chart:chart&gt;
 *  &lt;/chart:image&gt;
 * &lt;/template&gt;
 * 
 * &lt;template match="profits"&gt;
 *      &lt;chart:addValue category="{./@division}" series="First Quater Profits" value="@amount"/&gt;
 * &lt;/template&gt;
 * </pre>
 * </code>
 * <br/><br/>
 * For more information on the available configuration, see the individual method declarations below.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class Charting extends AbstractExtensionElement{

    public enum OutputType {SVG, JPEG, PNG}

    public enum Type {PIE, BAR, LINE, TIMESERIES}

    private JFreeChart chart;
    private OutputType format;
    private Type type;
    private OutputStream out;
    private int width;
    private int height;
    private Map<String, SeriesConfiguration> seriesConfig;
    private Map<String, CategoryConfiguration> categoryConfig;
    private Dataset dataSet;

    /**
     * Supplies the image configuration for a chart.  This is the root element for the charting elements.
     * <br/><br/>
     * Examle Usage:
     * <br/><br/>
     * <code>&lt;chart:image width="300" height="$chartHeight"&gt</code>
     * <br/><br/>
     * <code>&lt;/chart:image&gt;</code>
     * <br/><br/>
     * 
     * NOTE:  The output attribute exists to accomodate binary formats like JPEG and PNG.  Because
     * XSLT cannot (and should not) handle binary formats, if you wish to output your graph to a
     * file as a JPEG or PNG, you must supply a java.io.OuputStream for the charting components
     * to write to.  This can be done by passing the output into your xalan transformer at creation
     * time as a parameter and then declaring it as a parameter in your XSLT, e.g.:
     * <br/><br/>
     * Java Code:
     * <code><pre>
     *   TransformerFactory transFact = TransformerFactory.newInstance(
     *           "org.apache.xalan.processor.TransformerFactoryImpl",
     *           this.getClass().getClassLoader()
     *   );
     *
     *   trans = (TransformerImpl)transFact.newTransformer(xsltSource);
     *   trans.setParameter("myoutputstream", new FileOutputStream("c:/images/chart.png"));
     * </pre></code>
     * <br/><br/>
     * XSLT Code:
     * <br/><br/>
     * <code><pre>
     *  &lt;xsl:stylesheet......&gt;
     *      &lt;xsl:param name="myoutputstream"/&gt;
     *
     *      &lt;xsl:template match="/"&gt;
     *          &lt;chart:image width="400" height="400" format="PNG" output="$myoutputstream"&gt;
     *              ...
     *          &lt;/chart:image&gt;
     *      &lt;/xsl:template&gt;
     *  &lt;/xsl:stylesheet&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>height</td><td>XPath</td><td>The height of the image in pixels.</td><td>No (defaults to 300)</td></tr>
     *  <tr><td>width</td><td>XPath</td><td>The width of the image in pixels.</td><td>No (default to 300)</td></tr>
     *  <tr><td>format</td><td>Template</td><td>The format that the image will be saved in, either SVG, PNG or JPEG (case sensitive)</td><td>No (defaults to SVG)</td></tr>
     *  <tr><td>output</td><td>XPath</td><td>A java.io.OutputStream, passed into the XSLT engine as an external parameter, that should be used to saved binary image formats.</td><td>Only if using a binary format like PNG or JPEG</td></tr>
     * </table>
     * 
     * @param context
     * @param extensionElement
     * @throws TransformerException
     * @throws IOException
     */
    public void image(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException, IOException {
        configureFromAttributes(context, extensionElement);
        if((format == OutputType.JPEG || format == OutputType.PNG) && (out == null)){
            logError(this.getClass(), extensionElement, "You must specify a java.io.OutputStream for binary formats", context);
            throw new RuntimeException("No output stream specified for binary chart format");
        }
        if(validateConfiguration(context, extensionElement)){
            context.getTransformer().executeChildTemplates(extensionElement, true);
            if(chart != null){
                if(format == OutputType.PNG){
                    ChartUtilities.writeChartAsPNG(out, chart, width, height);
                }else if(format == OutputType.JPEG){
                    ChartUtilities.writeChartAsJPEG(out, chart, width, height);
                }else{
                    context.outputToResultTree(context.getStylesheet(),writeChartToSVG(chart, width, height));
                }
            }
        }
    }

    /**
     * Defines a chart and the associated attributes.
     * <br/><br/>
     * e.g.
     * <br/><br/>
     * <code><pre>
     *      &lt;chart:chart type="BAR" title="My Bar Chart" is3D="true" orientation="VERTICAL"&gt;
     *          &lt;chart:category name="Australian Division" color="#123456"/&gt;
     *          &lt;chart:category name="United States Division" color="blue"/&gt;
     *          &lt;chart:series name="First Quater Profits" dataType="DECIMAL"/&gt;
     *          &lt;chart:series name="Second Quater Profits" dataType="DECIMAL"/&gt;
     *          &lt;chart:series name="Third Quater Profits" dataType="DECIMAL"/&gt;
     *          &lt;xsl:apply-templates select="first-q/profits"/&gt;
     *      &lt;/chart:chart&gt;
     * </pre></code>
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>type</td><td>XPath</td><td>The type of chart.  Valid types are BAR, LINE, PIE or TIMESERIES (case dependant).  Time
     *                                      series charts are line charts that are date related (where data is per day/month/year etc).</td><td>No (defaults to BAR)</td></tr>
     *  <tr><td>title</td><td>Template</td><td>The title of the chart.</td><td>No</td></tr>
     *  <tr><td>titleFont</td><td>Template</td><td>The font to use for the title of the chart.</td><td>No.</td></tr>
     *  <tr><td>titleFontStyle</td><td>Template</td><td>The title font style (PLAIN, BOLD, ITALIC or BOLD ITALIC).</td><td>No</td></tr>
     *  <tr><td>titleFontSize</td><td>XPath</td><td>The pt size of the title font.</td><td>No</td></tr>
     *  <tr><td>orientation</td><td>Template</td><td>The orientation of the chart (HORIZONTAL or VERTICAL).  This is ignored for pie charts.</td><td>No (default to VERTICAL)</td></tr>
     *  <tr><td>subtitle</td><td>Template</td><td>A subtitle for the chart.</td><td>No</td></tr>
     *  <tr><td>subtitleFont</td><td>Template</td><td>The font to use for the sub title of the chart.</td><td>No.</td></tr>
     *  <tr><td>subtitleFontStyle</td><td>Template</td><td>The sub title font style (PLAIN, BOLD, ITALIC or BOLD ITALIC)</td><td>No</td></tr>
     *  <tr><td>subtitleFontSize</td><td>XPath</td><td>The pt size of the sub title font</td><td>No</td></tr>
     *  <tr><td>borderColor</td><td>Template</td><td>The color of the border (can be either rgb hex or an action color - e.g. #123456 or red).</td><td>No (default to black)</td></tr>
     *  <tr><td>borderWeight</td><td>XPath</td><td>The width of the chart border.</td><td>No</td></tr>
     *  <tr><td>backgroundColor</td><td>Template</td><td>The color of the background (can be either rgb hex or an action color - e.g  #123456 or red)</td><td>No (default to grey)</td></tr>
     *  <tr><td>antialias</td><td>Template</td><td>Use antialiasing?</td><td>No (default to false)</td></tr>
     *  <tr><td>showLegend</td><td>Template</td><td>Show the legend?</td><td>No (default to true)</td></tr>
     *  <tr><td>is3D</td><td>Template</td><td>Make the chart 3 dimensional?</td><td>No (default to false)</td></tr>
     *  <tr><td>categoryAxisTitle</td><td>Template</td><td>The title for the category axis.</td><td>No</td></tr>
     *  <tr><td>categoryAxisFont</td><td>Template</td><td>The font for the category axis title.</td><td>No</td></tr>
     *  <tr><td>categoryAxisFontStyle</td><td>Template</td><td>The category axis title font style (PLAIN, BOLD, ITALIC or BOLD ITALIC).</td><td>No</td></tr>
     *  <tr><td>categoryAxisfontSize</td><td>XPath</td><td>The pt size of the category axis title font.</td><td>No</td></tr>
     *  <tr><td>valueAxisTitle</td><td>Template</td><td>The title for the series value axis.</td><td>No</td></tr>
     *  <tr><td>valueAxisFont</td><td>Template</td><td>The font for the series value axis title.</td><td>No</td></tr>
     *  <tr><td>valueAxisFontStyle</td><td>Template</td><td>The series value axis title font style (PLAIN, BOLD, ITALIC or BOLD ITALIC)</td><td>No</td></tr>
     *  <tr><td>valueAxisFontSize</td><td>XPath</td><td>The pt size of the series value axis title font.</td><td>No</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     * @throws TransformerException
     */
    public void chart(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        /*
         * Chart attributes:
         * type, title, titleFont, titleFontStyle, titleFontSize, orientation
         * subtitle, subtitleFont, subtitleFontStyle, subtitleFontSize,
         * borderColor, borderWeight, backgroundColor, antialias, showLegend,
         * is3D, categoryAxisTitle, categoryAxisFont, categoryAxisFontStyle,
         * categoryAxisFontSize, valueAxisTitle, valueAxisFont, valueAxisFontStyle,
         * valueAxisFontSize
         */
        if(hasAttribute(extensionElement, "type")){
            String chartType = getAttribute("type", context, extensionElement);
            if(StringUtils.isNotEmpty(chartType)){
                try{
                    type = Type.valueOf(chartType);
                }catch(Throwable t){
                    logError(this.getClass(), extensionElement, "Unknown chart type [" + chartType + "]", context);
                }
            }
        }
        switch(type){
            case TIMESERIES:
                dataSet = new TimeSeriesCollection();
                break;
            case BAR:
            case LINE:
            case PIE:
            default:
                dataSet = new DefaultCategoryDataset();
                break;
        }
        seriesConfig = new HashMap<String, SeriesConfiguration>();
        categoryConfig = new HashMap<String, CategoryConfiguration>();
        context.getTransformer().executeChildTemplates(extensionElement, true);
        String chartTitle = "XSLT Generated Chart";
        String categoryAxisTitle = "";
        String valueAxisTitle = "";
        boolean is3D = false;
        boolean showLegend = true;
        PlotOrientation orientation = PlotOrientation.HORIZONTAL;
        if(hasAttribute(extensionElement, "title")){
            chartTitle = getAttribute("title", context, extensionElement);
        }
        if(hasAttribute(extensionElement, "categoryAxisTitle")){
            categoryAxisTitle = getAttribute("categoryAxisTitle", context, extensionElement);
        }
        if(hasAttribute(extensionElement, "valueAxisTitle")){
            valueAxisTitle = getAttribute("valueAxisTitle", context, extensionElement);
        }
        if(hasAttribute(extensionElement, "showLegend")){
            showLegend = Boolean.parseBoolean(getAttribute("showLegend", context, extensionElement));
        }
        if(hasAttribute(extensionElement, "is3D")){
            is3D = Boolean.parseBoolean(getAttribute("is3D", context, extensionElement));
        }
        if(hasAttribute(extensionElement, "orientation")){
            String orientationString = getAttribute("orientation", context, extensionElement);
            if("horizontal".equalsIgnoreCase(orientationString)){
                orientation = PlotOrientation.HORIZONTAL;
            }else if("vertical".equalsIgnoreCase(orientationString)){
                orientation = PlotOrientation.VERTICAL;
            }else{
                logError(this.getClass(), extensionElement, "Invalid graph orientation: " + orientationString, context);
            }
        }
        switch(type){
            case LINE:
                chart = createLineChart(chartTitle, categoryAxisTitle, valueAxisTitle, (CategoryDataset)dataSet, is3D, showLegend, orientation);
                break;
            case PIE:
                chart = createPieChart(chartTitle, (CategoryDataset)dataSet, is3D, showLegend);
                break;
            case TIMESERIES:
                chart = createTimeSeriesChart(chartTitle, categoryAxisTitle, valueAxisTitle, (TimeSeriesCollection)dataSet, is3D, showLegend);
                break;
            case BAR:
            default:
                chart = createBarChart(chartTitle, categoryAxisTitle, valueAxisTitle, (CategoryDataset)dataSet, is3D, showLegend, orientation);
                break;
        }
        configureTitle(chart, context, extensionElement);
        configureSubTitle(chart, context, extensionElement);
        configureBorder(chart, context, extensionElement);
        configureBackground(chart, context, extensionElement);
        configureMisc(chart, context, extensionElement);
        configureCategoryAxis(chart, context, extensionElement);
        configureValueAxis(chart, context, extensionElement);
        configureCategories(chart, context, extensionElement);
    }

    /**
     * Defines a series of values that will be used within the chart.  All series and categories must be declared before they can
     * be used by the <code>addValue</code> element.  This element must be declared within a <code>chart</code> element.
     * <br/><br/>
     * Example:
     * <br/><br/>
     * &lt;chart:series name="First Quater Profits" dataType="DECIMAL"/&gt;
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>The name of the series</td><td>Yes</td></tr>
     *  <tr><td>dataType</td><td>Template</td><td>The data type of the series (INTEGER, LONG or DECIMAL - case dependant).</td><td>No (defaults to INTEGER)</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void series(XSLProcessorContext context, ElemExtensionCall extensionElement) {
        /*
         * name, color?, weight?, dataType?
         */
        if(!hasAttribute(extensionElement, "name")) throw new RuntimeException("Series declarations must supply a name");
        SeriesConfiguration c;
        try {
            c = new SeriesConfiguration(type, context, extensionElement, context.getTransformer().getErrorListener());
        } catch (ChartDataConfigurationException ex) {
            throw new RuntimeException("Invalid series configuration");
        }
        seriesConfig.put(c.getName(), c);
    }

    /**
     * Defines a category that will be used within the chart.  All series and categories must be declared before they can
     * be used by the <code>addValue</code> element.  This element must be declared within a <code>chart</code> element.
     * <br/><br/>
     * Example:
     * <br/><br/>
     * &lt;chart:category name="United States Division" color="blue"/&gt;
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>name</td><td>Template</td><td>The name of the series</td><td>Yes</td></tr>
     *  <tr><td>color</td><td>Template</td><td>The color of the element on the chart.</td><td>No</td></tr>
     *  <tr><td>weight</td><td>XPath</td><td>The line weight of the series (only relevant for LINE or TIMESERIES graphs).</td><td>No</td></tr>
     *  <tr><td>timePeriod</td><td>Template</td><td>The time period (for TIMESERIES graphs only).  Either YEAR, MONTH, WEEK, DAY, HOUR, 
     *                                              MINUTE, SECOND or MILLISECOND (case dependant)</td><td>Mandatory for TIMESERIES graphs.</td></tr>
     *  <tr><td>dateTimeFormat</td><td>Template</td><td>The format (same pattern as java SimpleDateFormat) that will be used to decode the values into
     *                                                  dates.</td><td>Mandatory for TIMESERIES graphs.</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void category(XSLProcessorContext context, ElemExtensionCall extensionElement){
         /*
         * name, color?, weight?, timeperiod?, dateformat?
         */
        if(!hasAttribute(extensionElement, "name")) throw new RuntimeException("Category declarations must supply a name");
        CategoryConfiguration c;
        try {
            c = new CategoryConfiguration(type, context, extensionElement, context.getTransformer().getErrorListener());
        } catch (ChartDataConfigurationException ex) {
            throw new RuntimeException("Invalid category configuration");
        }
        categoryConfig.put(c.getName(), c);
        if(type == Type.TIMESERIES){
            ((TimeSeriesCollection)dataSet).addSeries(new TimeSeries(c.getName()));
        }
    }

    /**
     * Adds a value to the chart.
     * <br/><br/>
     * Example:
     * <br/><br/>
     * &lt;chart:addValue category="{./@division}" series="First Quater Profits" value="@amount"/&gt;
     * <br/><br/>
     * <table border="1">
     *  <tr><th align="left">Attribute Name</th><th align="left">Type</th><th align="left">Description</th><th align="left">Mandatory?</th></tr>
     *  <tr><td>category</td><td>Template</td><td>The name of the category to associate this value with</td><td>Yes</td></tr>
     *  <tr><td>series</td><td>Template</td><td>The name of the series to associate this value with.</td><td>Yes</td></tr>
     *  <tr><td>value</td><td>XPath</td><td>The value to add to the graph.</td><td>Yes</td></tr>
     * </table>
     * @param context
     * @param extensionElement
     */
    public void addValue(XSLProcessorContext context, ElemExtensionCall extensionElement){
        /*
         * series, category, value, type
         */
        if(!passesAttributeValidation(extensionElement, context, "value", "series", "category")) return;
        String series = getAttribute("series", context, extensionElement);
        String category = getAttribute("category", context, extensionElement);        
        String value = getXPath("value", context, extensionElement);
        SeriesConfiguration sc = seriesConfig.get(series);
        CategoryConfiguration cc = categoryConfig.get(category);
        if(sc == null && type != Type.TIMESERIES){
            throw new RuntimeException("No series configuration available for series " + series);
        }
        if(cc == null){
            throw new RuntimeException("No category configuration available for category " + category);
        }
        switch(type){
            case TIMESERIES:
                TimeSeriesCollection collection = (TimeSeriesCollection)dataSet;
                TimeSeries ts = collection.getSeries(cc.getName());
                try{
                    ts.add(cc.parseTimeValue(series), sc.parseValue(value));
                }catch(Exception e){
                    logError(this.getClass(), extensionElement, "Could not parse value: " + e.getMessage(), context);
                }
                break;
            case LINE:
            case PIE:
            case BAR:
            default:
                DefaultCategoryDataset set = (DefaultCategoryDataset)dataSet;
                set.addValue(sc.parseValue(value),category,series);
                break;
        }
    }

    private Node writeChartToSVG(JFreeChart chart, int width, int height){
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height), null);
        return svgGenerator.getRoot();
    }

    private void configureTitle(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //titleFont, titleFontStyle, titleFontSize
        Font defaultFont = chart.getTitle().getFont();
        Font f = calcFont(
                    "titleFont",
                    "titleFontStyle",
                    "titleFontSize",
                    defaultFont.getFontName(),
                    defaultFont.getStyle(),
                    defaultFont.getSize(),
                    context,
                    extensionElement
                );
        chart.getTitle().setFont(f);
    }

    private void configureSubTitle(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //subtitle, subtitleFont, subtitleFontStyle, subtitleFontSize
        if(hasAttribute(extensionElement, "subtitle")){
            String subtitle = getAttribute("subtitle", context, extensionElement);
            Font f = calcFont("subtitleFont",
                                "subtitleFontStyle",
                                "subtitleFontSize",
                                chart.getTitle().getFont().getFontName(),
                                chart.getTitle().getFont().getSize() > 6 ? chart.getTitle().getFont().getSize() - 4 : 6,
                                chart.getTitle().getFont().getStyle(),
                                context,
                                extensionElement);
            TextTitle title = new TextTitle(subtitle, f);
            chart.addSubtitle(title);
        }
    }

    private void configureBorder(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //borderColor, borderWeight
        if(hasAttribute(extensionElement, "borderColor")){
            chart.setBorderPaint(ParserUtil.parseColor(getAttribute("borderColor", context, extensionElement)));
        }
        if(hasAttribute(extensionElement, "borderWeight")){
            chart.setBorderStroke(ParserUtil.parseStroke(getXPath("borderWeight", context, extensionElement)));
        }
    }

    private void configureBackground(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //backgroundColor
        if(hasAttribute(extensionElement, "backgroundColor")){
            chart.setBackgroundPaint(ParserUtil.parseColor(getAttribute("backgroundColor", context, extensionElement)));
        }
    }

    private void configureMisc(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException {
        //antialias
        if(hasAttribute(extensionElement, "antialias")){
            chart.setAntiAlias(Boolean.parseBoolean(getAttribute("antialias", context, extensionElement)));
        }
    }

    private void configureCategoryAxis(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //categoryAxisFont, categoryAxisFontStyle, categoryAxisFontSize
        Font f = calcFont("categoryAxisFont",
                          "categoryAxisFontStyle",
                          "categoryAxisFontSize",
                          chart.getTitle().getFont().getFontName(),
                          Font.PLAIN,
                          6,
                          context,
                          extensionElement);
        switch(type){
            case PIE:
                //not necessary for pie charts
                break;
            case TIMESERIES:
                //XYPlot
                {
                    XYPlot plot = (XYPlot)chart.getPlot();
                    plot.getDomainAxis().setLabelFont(f);
                }
            case BAR:
            case LINE:
            default:
                //CategoryPlot and Axis
                {
                    CategoryPlot plot = (CategoryPlot)chart.getPlot();
                    plot.getDomainAxis().setLabelFont(f);
                }
        }
        
    }

    private void configureValueAxis(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement) {
        //valueAxisFont, valueAxisFontStyle, valueAxisFontSize
        Font f = calcFont("valueAxisFont",
                          "valueAxisFontStyle",
                          "valueAxisFontSize",
                          chart.getTitle().getFont().getFontName(),
                          Font.PLAIN,
                          6,
                          context,
                          extensionElement);
        switch(type){
            case PIE:
                //for pie charts, this information is configured for the individual series and categories
                break;
            case TIMESERIES:
                //XYPlot
                {
                    XYPlot plot = (XYPlot)chart.getPlot();
                    plot.getRangeAxis().setLabelFont(f);
                }
            case BAR:
            case LINE:
            default:
                //CategoryPlot and Axis
                {
                    CategoryPlot plot = (CategoryPlot)chart.getPlot();
                    plot.getRangeAxis().setLabelFont(f);
                }
        }
    }

    private int calcFontStyle(String fontStyleString){
        if(fontStyleString.toUpperCase().contains("PLAIN")){
            return Font.PLAIN;
        }else if(fontStyleString.toUpperCase().contains("BOLD") && fontStyleString.toUpperCase().contains("ITALIC")){
            return Font.BOLD | Font.ITALIC;
        }else if(fontStyleString.toUpperCase().contains("BOLD")){
            return Font.BOLD;
        }else if(fontStyleString.toUpperCase().contains("ITALIC")){
             return Font.ITALIC;
        }
        return Font.PLAIN;
    }

    private Font calcFont(String fontNameKey,
                            String fontStyleKey,
                            String fontSizeKey,
                            String defaultFontName,
                            int defaultFontStyle,
                            int defaultFontSize,
                            XSLProcessorContext context,
                            ElemExtensionCall extensionElement){
        String fontName = defaultFontName;
        int fontStyle = defaultFontStyle;
        int fontSize = defaultFontSize;
        if(hasAttribute(extensionElement, fontNameKey)){
            fontName = getAttribute(fontNameKey, context, extensionElement);
        }
        if(hasAttribute(extensionElement, fontStyleKey)){
            fontStyle = calcFontStyle(getAttribute(fontStyleKey, context, extensionElement));
        }
        if(hasAttribute(extensionElement, fontSizeKey)){
            try{
                fontSize = Integer.parseInt(getXPath(fontSizeKey, context, extensionElement));
            }catch(NumberFormatException e){
                logError(this.getClass(),
                        extensionElement,
                        "Incorrect format for " + fontSizeKey + " (" + extensionElement.getAttribute(fontSizeKey) + " does not evaluate to a valid number)",
                        context);
            }
        }
        return new Font(fontName, fontStyle, fontSize);
    }

    private void configureCategories(JFreeChart chart, XSLProcessorContext context, ElemExtensionCall extensionElement){
        switch(type){
            case PIE:
                {
                    final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
                    final JFreeChart subchart = plot.getPieChart();
                    final PiePlot p = (PiePlot) subchart.getPlot();
                    for(CategoryConfiguration config : categoryConfig.values()){
                        if(config.getColor() != null) p.setSectionPaint(config.getName(), config.getColor());
                        if(config.getStroke() != null) p.setSectionOutlineStroke(config.getName(), config.getStroke());
                    }
                }
                break;
            case TIMESERIES:
                {
                    XYPlot plot = chart.getXYPlot();
                    XYItemRenderer renderer = plot.getRenderer();
                    TimeSeriesCollection tsc = (TimeSeriesCollection)dataSet;
                    for(int i = 0; i < tsc.getSeriesCount(); i++){
                        TimeSeries s = tsc.getSeries(i);
                        CategoryConfiguration config = categoryConfig.get((String)s.getKey());
                        if(config.getColor() != null) renderer.setSeriesPaint(i, config.getColor());
                        if(config.getStroke() != null) renderer.setSeriesStroke(i, config.getStroke());
                    }
                }
                break;
            case LINE:
            case BAR:
            default:
                {
                    CategoryPlot plot = chart.getCategoryPlot();
                    AbstractRenderer renderer = (AbstractRenderer)plot.getRenderer();
                    DefaultCategoryDataset ds = (DefaultCategoryDataset)dataSet;
                    for(int i = 0; i < ds.getRowCount(); i++){
                        CategoryConfiguration config = categoryConfig.get((String)ds.getRowKey(i));
                        if(config.getColor() != null) renderer.setSeriesPaint(i, config.getColor());
                        if(config.getStroke() != null) renderer.setSeriesStroke(i, config.getStroke());
                    }
                }
                break;
        }
    }

    private boolean validateConfiguration(XSLProcessorContext context, ElemExtensionCall extensionElement){
        if(format == OutputType.JPEG || format == OutputType.PNG){
            if(out == null) {
                logError(this.getClass(),
                        extensionElement,
                        "Binary image formats like PNG and JPEG must specify an output stream",
                        context);
                return false;
            }
        }
        return true;
    }

    private void configureFromAttributes(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException{
        format = OutputType.SVG;
        type = Type.BAR;
        width = 300;
        height = 300;
        if(hasAttribute(extensionElement, "height")){
            try{
                height = Integer.parseInt(getXPath("height", context, extensionElement));
            }catch(Throwable t){
                logError(this.getClass(),
                        extensionElement,
                        "Incorrect height expression on image: " + getAttribute("height", context, extensionElement),
                        context);
            }
        }
        if(hasAttribute(extensionElement, "width")){
            try{
                width = Integer.parseInt(getXPath("width", context, extensionElement));
            }catch(Throwable t){
                logError(this.getClass(),
                        extensionElement,
                        "Incorrect weight expression on image: " + getAttribute("width", context, extensionElement),
                        context);
            }
        }
        if(hasAttribute(extensionElement, "format")){
            String fmt = getAttribute("format", context, extensionElement);
            if(StringUtils.isNotEmpty(fmt)){
                try{
                    format = OutputType.valueOf(fmt);
                }catch(Throwable t){
                    logError(this.getClass(),
                            extensionElement,
                            "Unknown format type [" + fmt + "]",
                            context);
                }
            }
        }
        if(hasAttribute(extensionElement, "output")){
            XObject stream = getXObject("output", context, extensionElement);
            if(stream != null && stream.object() != null){
                try{
                    out = (OutputStream)stream.object();
                }catch(ClassCastException e){
                    logError(this.getClass(),
                            extensionElement,
                            "Output object is an invalid type (" + stream.object().getClass() + " cannot be cast to an output stream)",
                            context);
                }
            }else{
                logError(this.getClass(),
                        extensionElement,
                        "No valid output specified",
                        context);
            }
        }
    }

    private JFreeChart createBarChart(String title,
                                      String categoryAxisTitle,
                                      String valueAxisTitle,
                                      CategoryDataset data,
                                      boolean is3D,
                                      boolean showLegend,
                                      PlotOrientation orientation){
        if(is3D){
            return ChartFactory.createBarChart3D(title, categoryAxisTitle, valueAxisTitle, data, orientation, showLegend, false, false);
        }else{
            return ChartFactory.createBarChart(title, categoryAxisTitle, valueAxisTitle, data, orientation, showLegend, false, false);
        }
    }

    private JFreeChart createLineChart(String title, String categoryAxisTitle, String valueAxisTitle, CategoryDataset data, boolean is3D, boolean showLegend, PlotOrientation orientation){
        if(is3D){
            return ChartFactory.createLineChart3D(title, categoryAxisTitle, valueAxisTitle, data, orientation, showLegend, false, false);
        }else{
            return ChartFactory.createLineChart(title, categoryAxisTitle, valueAxisTitle, data, orientation, showLegend, false, false);
        }
    }

    private JFreeChart createTimeSeriesChart(String title, String categoryAxisTitle, String valueAxisTitle, TimeSeriesCollection data, boolean is3D, boolean showLegend){
        return ChartFactory.createTimeSeriesChart(title, categoryAxisTitle, valueAxisTitle, data, showLegend, false, false);
    }

    private JFreeChart createPieChart(String title, CategoryDataset data, boolean is3D, boolean showLegend){
        if(is3D){
            return ChartFactory.createMultiplePieChart3D(title, data, TableOrder.BY_COLUMN, showLegend, false, false);
        }else{
            return ChartFactory.createMultiplePieChart(title, data, TableOrder.BY_COLUMN, showLegend, false, false);
        }
    }


}
