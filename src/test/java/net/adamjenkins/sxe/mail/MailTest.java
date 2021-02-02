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
package net.adamjenkins.sxe.mail;

import static org.junit.Assert.*;

import java.util.List;
import javax.mail.Message;
import net.adamjenkins.sxe.XSLTBasedTest;
import net.adamjenkins.sxe.util.XSLTErrorListener;
import org.jvnet.mock_javamail.Mailbox;

/**
 * Tests sending emails.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class MailTest extends XSLTBasedTest{

    private String result = "<test>This is an email</test>";
    
    @Override
    protected void processResults(String output, XSLTErrorListener listener) {
        super.processResults(output, listener);
        try{
            List<Message> inbox = Mailbox.get("mail@test.com");
            assertEquals("Incorrect number of emails in box.", inbox.size(),1);
            assertEquals("Results were incorrect", result, inbox.get(0).getContent());
            assertTrue("Incorrect message content type", inbox.get(0).getContentType().startsWith("text/plain"));
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

}
