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
package net.adamjenkins.sxe.concurrency;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

import net.adamjenkins.sxe.XSLTBasedTest;
import net.adamjenkins.sxe.util.XSLTErrorListener;

/**
 * Test for parallel execution.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
@Ignore
public class ParallelTest extends XSLTBasedTest {

    ThreadHolder holder = new ThreadHolder();
    
    @Override
    public Map<String, Object> getParameters() {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("threadholder", holder);
        return params;
    }



    @Override
    protected void processResults(String output, XSLTErrorListener listener){
        System.out.println(output);
        assertEquals("Errors occured running test.", 0, listener.getAllErrors().size());
        assertEquals("Incorrect number of threads.", 22, holder.getThreads().size());
    }
}
