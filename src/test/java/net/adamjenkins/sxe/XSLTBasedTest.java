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
package net.adamjenkins.sxe;


import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.xalan.transformer.TransformerImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.adamjenkins.sxe.util.XSLTErrorListener;


/**
 * Unit test framework for running XSLT based unit tests.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public abstract class XSLTBasedTest {

    private static final Logger log = LoggerFactory.getLogger(XSLTBasedTest.class);

    private String getXMLFileReference(){
        return "/net/adamjenkins/sxe/TestFile.xml";
    }

    protected Map<String,Object> getParameters(){
        return new HashMap<String,Object>();
    }

    private String getXSLTFileReference(){
        String name = getClass().getName().replace(".", "/");
        return "/" + name + ".xsl";
    }
    
    protected void setUp() {
    	//nothing here
    }

    @Test
    public void testComponent() throws TransformerConfigurationException, TransformerException, IOException{
    	setUp();
        XSLTErrorListener listener = new XSLTErrorListener();
        StreamSource xmlSource = new StreamSource(this.getClass().getResourceAsStream(getXMLFileReference()));
        StreamSource xsltSource = new StreamSource(this.getClass().getResourceAsStream(getXSLTFileReference()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(out);
        TransformerFactory transFact = TransformerFactory.newInstance(
                "org.apache.xalan.processor.TransformerFactoryImpl",
                getClass().getClassLoader()
        );
        Transformer trans = (TransformerImpl)transFact.newTransformer(xsltSource);
        for(Map.Entry<String,Object> param : getParameters().entrySet()){
            trans.setParameter(param.getKey(), param.getValue());
        }
        trans.setErrorListener(listener);
        trans.transform(xmlSource, result);
        if(log.isDebugEnabled()){
        	//reopen the sources
            xmlSource = new StreamSource(this.getClass().getResourceAsStream(getXMLFileReference()));
            xsltSource = new StreamSource(this.getClass().getResourceAsStream(getXSLTFileReference()));
        	log.debug("::Test Source XSLT::");
        	log.debug(IOUtils.toString(xsltSource.getInputStream(), "UTF-8"));
        	log.debug("::Test Source XML::");
        	log.debug(IOUtils.toString(xmlSource.getInputStream(), "UTF-8"));
            log.debug("::Test OUTPUT::");
            log.debug(new String(out.toByteArray()));
        }
        processResults(new String(out.toByteArray()), listener);
    }

    protected void processResults(String output, XSLTErrorListener listener){
        if(listener.getAllErrors().size() > 0){
            fail(listener.getAllErrors().get(0).getMessage());
        }
    }

    protected void assertSingleError(String errorMessage, XSLTErrorListener listener){
        if(listener.getAllErrors().size() < 1){
            fail("No errors detected, was supposed to be: " + errorMessage);
        }else if(listener.getAllErrors().size() > 1){
            StringBuilder builder = new StringBuilder();
            for(TransformerException e : listener.getAllErrors()){
                builder.append(e.toString());
                builder.append("\r\n");
            }
            fail("Too many errors: \r\n" + builder.toString());
        }else{
            assertEquals("Incorrect error", errorMessage, listener.getAllErrors().get(0).getMessage());
        }
    }
}
