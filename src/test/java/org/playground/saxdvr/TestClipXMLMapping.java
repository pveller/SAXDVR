package org.playground.saxdvr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.playground.saxdvr.clip.Clip;
import org.playground.saxdvr.clip.ClipHolder;
import org.playground.saxdvr.clip.ClipSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestClipXMLMapping {

	private ClipHolder clipHolder;
	private ClipSerializer serializer;

	private final String srcXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<data>" +
					"<title>My Title</title>" +
					"<date>12/24/2012</date>" +
					"<category>Blah!</category>" +
					"</data>";

	@Before
	public void setUpParser() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		clipHolder = new ClipHolder(parser.getXMLReader());
		serializer = new ClipSerializer();

		final InputSource src = new InputSource(new StringReader(srcXml));

		clipHolder.parse(src);
	}

	@Test
	public void testCanCreateClipFromXML() throws Exception {

		final Clip clip = clipHolder.getClip();
		assertNotNull("Clip object should not be null", clip);

		assertEquals(clip.title, "My Title");
		assertEquals(clip.date, Clip.DATE_FORMAT.parse("12/24/2012"));
		assertEquals(clip.category, "Blah!");
	}

	@Test
	public void testCanSerializeClipToXML() throws Exception {
		final Clip clip = clipHolder.getClip();
		serializer.setClip(clip);

		final TransformerFactory xsltFactory = TransformerFactory.newInstance();
		final Transformer t = xsltFactory.newTransformer();

		final StringWriter outXmlBuffer = new StringWriter();

		t.transform(new SAXSource(serializer, new InputSource()), new StreamResult(outXmlBuffer));

		assertEquals(srcXml, outXmlBuffer.getBuffer().toString());

	}
}
