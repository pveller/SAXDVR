package org.playground.saxdvr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class TestClipXMLMapping {
	
	private Parser<Clip> clipHolder;
	private Serializer<Clip> serializer;

	private final String srcXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<clip>" +
					"<category>Blah!</category>" +
					"<date>12/24/2012</date>" +
					"<title>My Title</title>" +
					"</clip>";

	@Before
	public void setUpParser() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();
		
		final Clip subject = new Clip();
		clipHolder = new Parser<Clip>(subject, parser.getXMLReader());
		serializer = new Serializer<Clip>();

		final InputSource src = new InputSource(new StringReader(srcXml));

		clipHolder.parse(src);
	}

	@Test
	public void testCanCreateClipFromXML() throws Exception {

		final Clip clip = clipHolder.getSubject();
		assertNotNull("Clip object should not be null", clip);

		assertEquals(clip.getTitle(), "My Title");
		assertEquals(clip.getDate(), "12/24/2012");
		assertEquals(clip.getCategory(), "Blah!");
	}

	@Test
	public void testCanSerializeClipToXML() throws Exception {
		final Clip clip = clipHolder.getSubject();
		serializer.setSubject(clip);

		final TransformerFactory xsltFactory = TransformerFactory.newInstance();
		final Transformer t = xsltFactory.newTransformer();

		final StringWriter outXmlBuffer = new StringWriter();

		t.transform(new SAXSource(serializer, new InputSource()), new StreamResult(outXmlBuffer));
		
		assertEquals(srcXml, outXmlBuffer.getBuffer().toString());
	}
}
