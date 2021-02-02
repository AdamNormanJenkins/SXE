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
package net.adamjenkins.sxe.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * XSLT Functions specific to conversions.
 * 
 * You can make these functions available to your XSLT sheet by adding the following line 
 * to the root element:
 * 
 * xmlns:convert="xalan://net.adamjenkins.sxe.functions.ConversionFunctions"
 * extension-element-prefixes="convert ..."
 * 
 * where ... above means any other extension elements in use
 * 
 * You can then access a function on any XSLT element using select="convert:functionName(param1, param2)"
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class ConversionFunctions {

    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000");

    /**
     * Tests if some text is a valid integer number (use full for finding numbered links in multipage job results).
     *
     * Usage Example:
     *
     * &lt;xsl:template match="a[convert:validInteger(text())]"/&gt;
     *
     * @param base  The string to check
     * @return  true if the supplied string is a valid integer, false if it's not, or it's null.
     */
    public static boolean validInteger(String base){
        if(base == null) return false;
        try{
            Integer.parseInt(base.trim());
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    /**
     * Converts a date in a given format to a XSD DATETIME variable format.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="posted" select="convert:convertToDate($job/submitted, 'yy/MM/dd')"/&gt;
     *
     * @param value         The date string.
     * @param pattern       The format the date string is in.
     * @return  The date in XML Schema DATETIME variable format.
     * @throws java.text.ParseException
     */
    public static String convertToDate(String value, String pattern) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date = format.parse(value);
        return DATE_TIME.format(date);
    }

}
