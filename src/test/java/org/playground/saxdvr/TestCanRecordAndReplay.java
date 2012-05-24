package org.playground.saxdvr;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.playground.saxdvr.RecorderProxy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestCanRecordAndReplay {

	@Test
	public void testParseReplay() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		final RecorderProxy proxy = new RecorderProxy(parser.getXMLReader());

		final String srcXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<data>" +
				"<title>My Title</title>" +
				"<date>12/24/2012</date>" +
				"<category>Blah!</category>" +
				"</data>";
		final InputSource src = new InputSource(new StringReader(srcXml));

		proxy.parse(src);
		
		assertTrue(proxy.hasRecordingToReplay());

		final TransformerFactory xsltFactory = TransformerFactory.newInstance();
		final Transformer t = xsltFactory.newTransformer();

		final StringWriter outXmlBuffer = new StringWriter();

		t.transform(new SAXSource(proxy, new InputSource()), new StreamResult(outXmlBuffer));

		assertEquals(srcXml, outXmlBuffer.getBuffer().toString());

	}

}