package org.playground.saxdvr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class TestCanHandlePartialUpdate {

	private final Logger logger = LoggerFactory.getLogger(TestCanHandlePartialUpdate.class);

	private final String srcXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<clip>" +
					"<title>My Title</title>" +
					"<somethingnew>let's see</somethingnew>" +
					"<date>12/24/2012</date>" +
					"<category>Blah!</category>" +
					"<somethingelsenew>let's see</somethingelsenew>" +
					"</clip>";

	private final String targetXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<clip>" +
					"<title>My Title Updated</title>" +
					"<somethingnew>let's see</somethingnew>" +
					"<date>12/24/2012</date>" +
					"<category>Something else</category>" +
					"<somethingelsenew>let's see</somethingelsenew>" +
					"</clip>";

	@Test
	public void testPartialUpdateScenario() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		RecorderProxy recorder = new RecorderProxy(parser.getXMLReader());
		final Clip subject = new Clip();
		final Parser<Clip> clipHolder = new Parser<Clip>(subject, recorder);
		final InputSource src = new InputSource(new StringReader(srcXml));

		clipHolder.parse(src);

		assertTrue(recorder.hasRecordingToReplay());

		final Clip clip = clipHolder.getSubject();
		assertNotNull(clip);
		assertEquals(clip.getTitle(), "My Title");
		assertEquals(clip.getCategory(), "Blah!");
		assertEquals(clip.getDate(), "12/24/2012");

		clip.setTitle("My Title Updated");
		clip.setCategory("Something else");

		final Serializer<Clip> serializer = new Serializer<Clip>(recorder);
		serializer.setSubject(clip);

		final TransformerFactory xsltFactory = TransformerFactory.newInstance();
		final Transformer t = xsltFactory.newTransformer();
		final StringWriter outXmlBuffer = new StringWriter();

		t.transform(new SAXSource(serializer, new InputSource()), new StreamResult(outXmlBuffer));

		final String resultXml = outXmlBuffer.getBuffer().toString();

		logger.debug(resultXml);

		assertEquals(targetXml, resultXml);
	}

}
