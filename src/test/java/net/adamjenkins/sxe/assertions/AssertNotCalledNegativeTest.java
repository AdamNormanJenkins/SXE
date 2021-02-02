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
package net.adamjenkins.sxe.assertions;

import net.adamjenkins.sxe.XSLTBasedTest;
import net.adamjenkins.sxe.util.XSLTErrorListener;

/**
 * Tests the assert not called functionality.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class AssertNotCalledNegativeTest extends XSLTBasedTest{

    @Override
    protected void processResults(String output, XSLTErrorListener listener) {
        assertSingleError("Node 'token' was called but was registered as 'assertNotCalled'", listener);
    }

}
