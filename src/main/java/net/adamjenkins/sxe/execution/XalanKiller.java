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
package net.adamjenkins.sxe.execution;

import javax.xml.transform.TransformerException;
import org.apache.xalan.trace.GenerateEvent;
import org.apache.xalan.trace.SelectionEvent;
import org.apache.xalan.trace.TraceListener;
import org.apache.xalan.trace.TracerEvent;

/**
 * A trace listener for xalan that throws a runtime exception on request so that no matter
 * what xalan is doing, it will exit.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class XalanKiller implements TraceListener{

    private boolean ok = true;
    private String killedBy;
    private String reason;

    public void kill(String killedBy, String reason){
        ok = false;
        this.killedBy = killedBy;
        this.reason = reason;
    }

    public void trace(TracerEvent ev) {
        if(!ok){
            throw new XalanKilledException(killedBy, reason);
        }
    }

    public void selected(SelectionEvent ev) throws TransformerException {        
    }

    public void generated(GenerateEvent ev) {        
    }
}
