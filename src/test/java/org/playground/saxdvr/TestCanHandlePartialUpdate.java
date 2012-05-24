package org.playground.saxdvr;

import static org.junit.Assert.*;

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
import org.playground.saxdvr.clip.Clip;
import org.playground.saxdvr.clip.ClipHolder;
import org.playground.saxdvr.clip.ClipSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class TestCanHandlePartialUpdate {

	private final Logger logger = LoggerFactory.getLogger(ClipSerializer.class);

	private final String srcXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<data>" +
					"<title>My Title</title>" +
					"<somethingnew>let's see</somethingnew>" +
					"<date>12/24/2012</date>" +
					"<category>Blah!</category>" +
					"<somethingelsenew>let's see</somethingelsenew>" +
					"</data>";

	private final String targetXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<data>" +
					"<title>My Title Updated</title>" +
					"<somethingnew>let's see</somethingnew>" +
					"<date>12/24/2012</date>" +
					"<category>Something else</category>" +
					"<somethingelsenew>let's see</somethingelsenew>" +
					"</data>";
	
	@Test
	public void testPartialUpdateScenario() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		RecorderProxy recorder = new RecorderProxy(parser.getXMLReader());
		final ClipHolder clipHolder = new ClipHolder(recorder);
		final InputSource src = new InputSource(new StringReader(srcXml));

		clipHolder.parse(src);
		
		assertTrue(recorder.hasRecordingToReplay());
		
		final Clip clip = clipHolder.getClip();
		assertNotNull(clip);
		assertEquals(clip.title, "My Title");
		assertEquals(clip.category, "Blah!");
		assertEquals(clip.date, Clip.DATE_FORMAT.parse("12/24/2012"));
		
		clip.title = "My Title Updated";
		clip.category = "Something else";
		
		final ClipSerializer serializer = new ClipSerializer(recorder);
		serializer.setClip(clip);
		
		final TransformerFactory xsltFactory = TransformerFactory.newInstance();
		final Transformer t = xsltFactory.newTransformer();
		final StringWriter outXmlBuffer = new StringWriter();

		t.transform(new SAXSource(serializer, new InputSource()), new StreamResult(outXmlBuffer));
		
		final String resultXml = outXmlBuffer.getBuffer().toString(); 
		
		logger.debug(resultXml);
		
		assertEquals(targetXml, resultXml);
	}

}
