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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xpath.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XSLT Functions for various string related operations.
 *
 * You can make these functions available to your XSLT sheet by adding the following line
 * to the root element:
 *
 * xmlns:sxestr="xalan://new.java.dev.sxe.functions.StringFunctions"
 * extension-element-prefixes="sxestr ..."
 *
 * where ... above means any other extension elements in use
 *
 * You can then access a function on any XSLT element using select="sxestr:functionName(param1, param2)"
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class StringFunctions {

    /**
     * Similar to XSLT substring-after, except the parameter is a regular expression.
     *
     * Usage Example:
     *
     * &lt;xsl:value-of name="url" select="sxestr:substringAfterMatch('some text', '\s')"/&gt;
     *
     * @param   base        The string to substring
     * @param   regexp      The regexp.
     * @return  The new string.
     */
    public static String substringAfterMatch(String base, String regexp){
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(base);
        if(m.find()){
            return base.substring(m.end()+1, base.length());
        }else{
            return base;
        }
    }

    /**
     * Similar to XSLT substring-before, except the parameter is a regular expression.
     *
     * Usage Example:
     *
     * &lt;xsl:value-of name="url" select="sxestr:substringBeforeMatch('some text', '\s')"/&gt;
     *
     * @param   base        The string to substring
     * @param   regexp      The regexp.
     * @return  The new string.
     */
    public static String substringBeforeMatch(String base, String regexp){
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(base);
        if(m.find()){
            return base.substring(0, m.start());
        }else{
            return base;
        }
    }

    /**
     * Scans a string of name value pairs (e.g x=y,z=a,b=c) for a particular value (e.g. 'a').
     *
     * Usage Example:
     *
     * say we have &lt;a onclick="myform.somevalue=x;myform.othervalue='y'"&gt; we can access with
     *
     * &lt;xsl:variable name="somevar" select="sxestr:scanNameValueString(a/@onclick, '=', ';', 'myform.somevalue', true)"/&gt;
     *
     * @param   base                The string to search
     * @param   nameValueDelimiter  The name/value delimiter (e.g. '=' in the above example)
     * @param   pairDelimiter       The delimiter for each name value pair (e.g. ';' in the above example).
     * @param   search              The name of the value.
     * @param   stripQuotes         To strip the quotes from the value if it is surrounded by quotes.
     * @return
     */
    public static String scanNameValueString(String base, String nameValueDelimiter, String pairDelimiter, String search, boolean stripQuotes){
        StringTokenizer tok = new StringTokenizer(base, pairDelimiter);
        while(tok.hasMoreTokens()){
            String s = tok.nextToken();
            if(s.startsWith(search)){
                String returnValue = s.substring(s.lastIndexOf(nameValueDelimiter) + 1, s.length());
                if(((returnValue.startsWith("'") && returnValue.endsWith("'"))||
                        (returnValue.startsWith("\"") && returnValue.endsWith("\""))) &&
                        stripQuotes){
                    returnValue = returnValue.substring(1, returnValue.length() -1);
                }
                return returnValue;
            }
        }
        return null;
    }

    /**
     * Gets the file extension from a string.
     *
     * Example Usage:
     *
     * &lt;xsl:variable name="moreinfo" select="sxestr:proprietaryDocument($http-client, $url, sxestr:documentExtension($url))"/&gt;
     *
     * @param base  The base.
     * @return  The document extension, or null if the extension could not be determined.
     */
    public static String documentExtension(String base){
        if(base == null) return null;
        if(!base.contains(".")) return null;
        base = base.trim();
        return base.substring(base.lastIndexOf(".")+1, base.length());
    }

    /**
     * A nice way to do a string replace (calls through to String.replace(...)
     *
     * Usage Example
     *
     * &lt;xsl-value-of select="sxestr:replace($someurl, '.', ' ')"/&gt;
     *
     * @param base  The string to perform the replace on.
     * @param search    The characters to replace
     * @param replaceWith   The replacement string.
     * @return  The result.
     */
    public static String replace(String base, String search, String replaceWith){
        return base.replace(search, replaceWith);
    }

    /**
     * Finds the first node whose value matches a regular expression.
     *
     * Usage Example:
     *
     * &lt;xsl:value-of select="sxestr:findFirstRegexpValueMatch(./table/tr/td/a, '\s\d\d\s'/&gt;
     *
     * @param set           The node set to search.
     * @param regexp        The regular expression to use.
     * @return  The matching node value text or null if no match was found.
     */
    public static String findFirstRegexpValueMatch(NodeList list, String regexp){
        if(list == null || regexp == null) return null;
        Pattern pattern = Pattern.compile(regexp);
        for(int i = 0; i < list.getLength(); i++){
            Node n = list.item(i);
            Matcher matcher = pattern.matcher(n.getNodeValue());
            if(matcher.find()){
                return matcher.group();
            }
        }
        return null;
    }

    /**
     * Checks to see if some text matches a regular expression.
     *
     * Usage Example:
     *
     * &lt;xsl:template match="a[sxestr:match(text(), $myregexp)]"/&gt;
     *
     * @param text      The text to check.
     * @param regexp    The regular expression to use.
     * @return  true if a match if found, false if not, or if any of the inputs are null.
     */
    public static boolean matches(String text, String regexp){
        if(text == null || regexp == null) return false;
        Pattern pattern = Pattern.compile(regexp);
        Matcher m = pattern.matcher(text);
        if(m.find()){
            return true;
        }
        return false;
    }

    /**
     * Finds the regular expression match that is in the position specified (counting begins at 1, just like the rest of xslt).
     *
     * Usage Example:
     *
     * &lt;xsl:value-of select="sxestr:findRegexpMatch($somevalue, '\s\d\d\s', 2)"/&gt;
     *
     * @param base      The base to search.
     * @param regexp    The regular expression to use.
     * @param match     The position
     * @return  The match, or null if no match is found.
     */
    public static String findRegexpMatch(String base, String regexp, int position){
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(base);
        int count = 1;
        while(matcher.find()){
            if(position == count){
                return matcher.group();
            }else{
                ++count;
            }
        }
        return null;
    }

    /**
     * Strips a string from another string.
     *
     * Example Usage:
     *
     * &lt;xsl:value-of select="sxestr:strip($someamount, ',')"/&gt;
     *
     * @param base          The string to strip from.
     * @param toStrip       The string to strip.
     *
     * @return  The stripped string.
     */
    public static String strip(String base, String toStrip){
        return base.replace(toStrip, "");
    }

    /**
     * Checks to see if a nodes value contains a string, ignoring the case of both.
     *
     * Example Usage:
     * &lt;xsl:apply-templates select="/somenode[sxestr:containsIgnoreCase(text, 'somevalue')]/someothernode"/&gt;
     *
     * @param n         The node.
     * @param contains  The string.
     * @return  true if the nodes value contains the string provided or false if not, or if any of the input are null or the node value is null.
     */
    public static boolean containsIgnoreCase(Node n, String contains){
        if(n == null) return false;
        if(contains == null) return false;
        String value = n.getNodeValue();
        if(value == null) return false;
        value = value.toLowerCase();
        contains = contains.toLowerCase();
        return value.contains(contains);
    }

    /**
     * Limits a string to a particular length.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="limitedText" select="sxestr:limitTo($mydoc/somelongtext, 50)"/&gt;
     *
     * @param value         The string to limit.
     * @param sizeLimit     The limit to apply.
     * @return  The string if its length is less than the supplied limit, otherwise will return the substring up to the limit.
     */
    public static String limitTo(String value, int sizeLimit){
        if(value == null) return "";
        if(value.length() > sizeLimit){
            value = value.substring(0, sizeLimit);
        }
        return value;
    }

    /**
     * Normalizes a string to ensure there are not weird characters that XML doesn't support.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="normalized" select="sxestr:normalizeForXML($mytext)"/&gt;
     *
     * @param value The string to normalize
     * @return  The normalized string.
     */
    public static String normalizeForXML(String value){
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (value == null || ("".equals(value))) return ""; // vacancy test.
        for (int i = 0; i < value.length(); i++) {
            current = value.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    /**
     * Uppercases a value.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="workType" select="sxestr:uppercase($worktype)"/&gt;
     *
     *
     * @param value The value.
     * @return  It's upper case counterpart.
     */
    public static String uppercase(String value){
        return value.toUpperCase();
    }

    /**
     * Applies a regular expression to a string, and returns a node set of all parts of the string
     * that match the regular expression.
     *
     * Usage Example:
     *
     * if variable titleString = 'Showing jobs 1 to 20 of 530 jobs'
     *
     * &lt;xsl:variable name="allJobs" select="sxestr:regexp('\d+', $titleString)[3]"/&gt;
     *
     * will return '530'
     *
     * @param regexp    The regular expression.
     * @param base      The string to act upon.
     * @return  A list of text nodes, one for each match.
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static NodeSet regexp(String regexp, String base) throws ParserConfigurationException{
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        NodeSet set = new NodeSet();
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(base);
        while(matcher.find()){
                Node elem = doc.createTextNode(matcher.group());
                set.addNode(elem);
        }
        return set;
    }

    /**
     * Tests if two strings are equal, ignoring their case.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="isAustralia" select="sxestr:equalsIgnoreCase($mytext, 'australia')"/&gt;
     *
     *
     * @param s1    String 1.
     * @param s2    String 2.
     * @return  true if the two string are equal (ignoring case), false otherwise
     */
    public static boolean equalsIgnoreCase(String s1, String s2){
        return s1.equalsIgnoreCase(s2);
    }

    /**
     * Tests if the base string contains the segment string, ignoring the case.
     *
     * Usage Example:
     *
     * &lt;xsl:variable name="isMoney" select="sxestr:containsIgnoreCase($mytext, 'AUD$')"/&gt;
     *
     * @param base      The base string.
     * @param segment   The segment to test for.
     * @return  true if the base contains the segment (ignoring case), false otherwise.
     */
    public static boolean containsIgnoreCase(String base, String segment){
        return base.toUpperCase().contains(segment.toUpperCase());
    }

}
