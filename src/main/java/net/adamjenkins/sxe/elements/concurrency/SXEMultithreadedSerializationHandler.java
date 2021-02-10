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
package net.adamjenkins.sxe.elements.concurrency;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;

import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A serialization handler that can handle multithreaded input.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class SXEMultithreadedSerializationHandler implements SerializationHandler {
	
	private SerializationHandler handler;
	
	public SXEMultithreadedSerializationHandler(SerializationHandler hander) {
		this.handler = handler;
	}

	public void addAttribute(String uri, String localName, String rawName, String type, String value,
			boolean XSLAttribute) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void addAttributes(Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void addAttribute(String qName, String value) {
		// TODO Auto-generated method stub
		
	}

	public void characters(String chars) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void characters(Node node) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endElement(String elemName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startElement(String qName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void namespaceAfterStartElement(String uri, String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush) throws SAXException {
		// TODO Auto-generated method stub
		return false;
	}

	public void entityReference(String entityName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public NamespaceMappings getNamespaceMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrefix(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamespaceURI(String name, boolean isElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamespaceURIFromPrefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSourceLocator(SourceLocator locator) {
		// TODO Auto-generated method stub
		
	}

	public void addUniqueAttribute(String qName, String value, int flags) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void addXSLAttribute(String qName, String value, String uri) {
		// TODO Auto-generated method stub
		
	}

	public void addAttribute(String uri, String localName, String rawName, String type, String value)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		
	}

	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void processingInstruction(String target, String data) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void comment(String comment) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endDTD() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void startCDATA() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void endCDATA() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void comment(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public String getDoctypePublic() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDoctypeSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getIndent() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getIndentAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMediaType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getOmitXMLDeclaration() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getStandalone() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCdataSectionElements(Vector URI_and_localNames) {
		// TODO Auto-generated method stub
		
	}

	public void setDoctype(String system, String pub) {
		// TODO Auto-generated method stub
		
	}

	public void setDoctypePublic(String doctype) {
		// TODO Auto-generated method stub
		
	}

	public void setDoctypeSystem(String doctype) {
		// TODO Auto-generated method stub
		
	}

	public void setEncoding(String encoding) {
		// TODO Auto-generated method stub
		
	}

	public void setIndent(boolean indent) {
		// TODO Auto-generated method stub
		
	}

	public void setMediaType(String mediatype) {
		// TODO Auto-generated method stub
		
	}

	public void setOmitXMLDeclaration(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setStandalone(String standalone) {
		// TODO Auto-generated method stub
		
	}

	public void setVersion(String version) {
		// TODO Auto-generated method stub
		
	}

	public String getOutputProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputPropertyDefault(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOutputProperty(String name, String val) {
		// TODO Auto-generated method stub
		
	}

	public void setOutputPropertyDefault(String name, String val) {
		// TODO Auto-generated method stub
		
	}

	public void elementDecl(String name, String model) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void internalEntityDecl(String name, String value) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void error(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void setOutputStream(OutputStream output) {
		// TODO Auto-generated method stub
		
	}

	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setWriter(Writer writer) {
		// TODO Auto-generated method stub
		
	}

	public Writer getWriter() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOutputFormat(Properties format) {
		// TODO Auto-generated method stub
		
	}

	public Properties getOutputFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentHandler asContentHandler() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public DOMSerializer asDOMSerializer() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean reset() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object asDOM3Serializer() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setContentHandler(ContentHandler ch) {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void serialize(Node node) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public boolean setEscaping(boolean escape) throws SAXException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setIndentAmount(int spaces) {
		// TODO Auto-generated method stub
		
	}

	public void setTransformer(Transformer transformer) {
		// TODO Auto-generated method stub
		
	}

	public Transformer getTransformer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNamespaceMappings(NamespaceMappings mappings) {
		// TODO Auto-generated method stub
		
	}

	public void flushPending() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void setDTDEntityExpansion(boolean expand) {
		// TODO Auto-generated method stub
		
	}

}
