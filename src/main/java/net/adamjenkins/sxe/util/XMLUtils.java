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

import net.adamjenkins.sxe.execution.*;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import net.adamjenkins.sxe.execution.cache.CacheingWebClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Some utilities for working with transformed xml.
 *
 * WARNING!!!  This class will be accessed from many different tiers.  Do not tie anything
 * in here to any EJB dependant methods or XQueries that use EJBs.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class XMLUtils {      
    
    private static final Logger log = LoggerFactory.getLogger(XMLUtils.class);
        
    
    /**
     * Pretty prints a dom object.
     * 
     * @param dom   The dom object.
     * @return  The pretty printed string.
     * @throws java.io.UnsupportedEncodingException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException 
     */
    public static final String prettyPrintDOM(Document dom) throws UnsupportedEncodingException, ClassNotFoundException, InstantiationException, InstantiationException, IllegalAccessException{
        //for some reason this stopped working on the upgrade to netbeans 6.5??
        //if(dom.getXmlVersion() == null) dom.setXmlVersion("1.0");
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DOMImplementationLS impl = 
            (DOMImplementationLS)registry.getDOMImplementation("LS");         
        LSSerializer serializer = impl.createLSSerializer();        
        DOMConfiguration config = serializer.getDomConfig();
        config.setParameter("format-pretty-print", true);           
        LSOutput lso = impl.createLSOutput();        
        lso.setByteStream(out);
        lso.setEncoding("UTF-8");        
        serializer.write(dom, lso);  
        return new String(out.toByteArray(), "UTF-8");
    }
    
    /**
     * Pretty prints a document the old fashioned (DOM2) way (because JTidy doesn't support DOM3).
     * 
     * @param dom   The DOM 2 level document
     * @return  The pretty printed string.
     * @throws java.io.IOException
     */
    public static final String prettyPrintDOMLevel2(Document dom) throws IOException{
        java.util.Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        Serializer ser = SerializerFactory.getSerializer(props);
        StringWriter writer = new StringWriter();
        ser.setWriter(writer);
        DOMSerializer dser = ser.asDOMSerializer();  
        dser.serialize(dom);        
        return writer.toString();
    }
    
    
    /**
     * Gets a html file as a DOM Document from the provided url, tidying any problems with the
     * document and stripping any &lt;script&gt; or &lt;noscript&gt; tags.
     * 
     * @param client    The http client.
     * @param url   The url.
     * @return  The well formatted html document.
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     */
    public static final Document getTidyXHTMLFromUrl(WebClient client, String url) throws IOException, TransformerException, UnsupportedEncodingException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedPageTypeException{
        Page result = client.getPage(url);
        try{
            HtmlPage page = (HtmlPage)result;
            if(log.isDebugEnabled()){
                log.debug("Document received from url " + url);
                log.debug(prettyPrintDOMLevel2(page));
            }
            return page;
        }catch(ClassCastException e){
            log.error("HTML Page at url " + url + " is not a validate HTML page.");
            throw new UnsupportedPageTypeException(url);
        }
    }
    
    /**
     * Gets a document using a http post.
     * 
     * @param client    The Http client to use.
     * @param url       The url.
     * @param input     Any form input for this post.
     * @return  The document.
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException 
     */
    public static final Document getTidyXHTMLFromFormPost(WebClient client, String url, Map<String,String> input) throws IOException, TransformerException{
        List<NameValuePair> nvps = paramsToPairs(input);
        return (Document)executePost(client, url, nvps);
    }
    
    /**
     * Gets a html file as a DOM Document from the provided url, tidying any problems with the
     * document and stripping any &lt;script&gt; or &lt;noscript&gt; tags.
     * 
     * @param client        The http client to use.
     * @param url           The url.
     * @param parameters    Any form input.
     * @return  The well formatted html document.
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     */
    public static final Document getTidyXHTMLFromFormGet(WebClient client, String url, Map<String,String> parameters) throws IOException, TransformerException{
        List<NameValuePair> queryPairs = paramsToPairs(parameters);
        return (Document)executeGet(client, url, queryPairs);
    }    
    
    /**
     * Gets a new http client with the standard configuration.
     * 
     * @return  The http client.
     */
    public static WebClient newHttpClient(){
        WebClient client = new WebClient(BrowserVersion.CHROME);
        configureClient(client);
        return client;
    }

    private static void configureClient(WebClient client){
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setRedirectEnabled(true);
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        //3 minute time out
        client.getOptions().setTimeout(180000);
    }

    /**
     * Gets a new cacheing http client with the standard configuration.
     *
     * @return  The new cacheing http client.
     */
    public static CacheingWebClient newCacheingHttpClient(){
        CacheingWebClient client = new CacheingWebClient(BrowserVersion.CHROME);
        configureClient(client);
        return client;
    }

    public static Page getPageUsingGet(WebClient client, String url, Map<String,String> parameters) throws MalformedURLException, IOException {
        List<NameValuePair> pairs = paramsToPairs(parameters);
        return executeGet(client, url, pairs);
    }

    public static Page getPageUsingPost(WebClient client, String url, Map<String,String> parameters) throws MalformedURLException,IOException {
        List<NameValuePair> pairs = paramsToPairs(parameters);
        return executePost(client, url, pairs);
    }
    
    
    private static List<NameValuePair> paramsToPairs(Map<String,String> parameters){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for(Map.Entry<String,String> param : parameters.entrySet()){
            pairs.add(new NameValuePair(param.getKey(), param.getValue() == null ? "" : param.getValue()));
        }
        return pairs;
    }
    
    private static Page executePost(WebClient client, String url, List<NameValuePair> input) throws MalformedURLException, IOException{
        WebRequest settings = new WebRequest(new URL(url), HttpMethod.POST);
        settings.setRequestParameters(input);
        Page page = client.getPage(settings);
        debugPage(page, url);   
        return page;        
    }
    
    private static Page executeGet(WebClient client, String url, List<NameValuePair> input) throws MalformedURLException, IOException{
        WebRequest settings = new WebRequest(new URL(url), HttpMethod.GET);
        settings.setRequestParameters(input);
        Page page = client.getPage(settings);       
        debugPage(page, url);
        return page;        
    }
    
    private static void debugPage(Page p, String url){
        if(log.isDebugEnabled()){
            log.debug("Document received from for get to " + url);
            try{
                log.debug(XMLUtils.prettyPrintDOMLevel2((Document)p));
            }catch(Throwable t){
                log.debug("Error writing documet", t);
                log.debug("Raw Document: " + p.getWebResponse().getContentAsString());
            }
        }        
    }
    
}
