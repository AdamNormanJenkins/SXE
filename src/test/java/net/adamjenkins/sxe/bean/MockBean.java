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
package net.adamjenkins.sxe.bean;

/**
 * A mock object for the java bean elements to work with.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class MockBean {

    private String text;
    
    private boolean bool;

    private double decimal;

    public MockBean(){}

    public MockBean(String text, boolean bool, double decimal) {
        this.text = text;
        this.bool = bool;
        this.decimal = decimal;
    }

    public String configure(String text, boolean bool, double decimal) {
        this.text = text;
        this.bool = bool;
        this.decimal = decimal;
        return "passed";
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public double getDecimal() {
        return decimal;
    }

    public void setDecimal(double decimal) {
        this.decimal = decimal;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString(){
        return text + "|" + bool + "|" + decimal;
    }

}
