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

import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * An error listener.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class XSLTErrorListener implements ErrorListener{
    private List<TransformerException> warnings = new ArrayList<TransformerException>();
    private List<TransformerException> errors = new ArrayList<TransformerException>();
    private List<TransformerException> fatals = new ArrayList<TransformerException>();

    public List<TransformerException> getAllErrors(){
        ArrayList<TransformerException> returnVal = new ArrayList<TransformerException>(warnings);
        returnVal.addAll(errors);
        returnVal.addAll(fatals);
        return returnVal;
    }

    public void warning(TransformerException exception) throws TransformerException {
        warnings.add(exception);
    }

    public void error(TransformerException exception) throws TransformerException {
        errors.add(exception);
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        fatals.add(exception);
    }

    public List<TransformerException> getErrors() {
        return errors;
    }

    public List<TransformerException> getFatals() {
        return fatals;
    }

    public List<TransformerException> getWarnings() {
        return warnings;
    }

    public void transferTo(ErrorListener listener) throws TransformerException{
        for(TransformerException e : getWarnings()){
            listener.warning(e);
        }
        for(TransformerException e : getErrors()){
            listener.error(e);
        }
        for(TransformerException e : getFatals()){
            listener.fatalError(e);
        }
    }

}    
