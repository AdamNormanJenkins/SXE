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
package net.adamjenkins.sxe.soap;

import static org.junit.Assert.*;

import net.adamjenkins.sxe.XSLTBasedTest;
import net.adamjenkins.sxe.util.XSLTErrorListener;

/**
 * Tests loading a spring bean for use within an XSLT.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class SoapTest extends XSLTBasedTest{

    private String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    		+ "<AddResponse xmlns=\"http://tempuri.org/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
    		+ "<AddResult>2</AddResult>"
    		+ "</AddResponse>";


    @Override
    protected void processResults(String output, XSLTErrorListener listener) {
        super.processResults(output, listener);
        assertEquals("Results were incorrect", result, output);
    }

}
