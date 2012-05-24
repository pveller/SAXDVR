package org.playground.saxdvr.clip;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class ClipSerializer extends XMLFilterImpl {

	private Clip clip;
	private Field context;

	private final Logger logger = LoggerFactory.getLogger(ClipSerializer.class);

	public ClipSerializer() {
		super();
	}

	public ClipSerializer(XMLReader parent) {
		super(parent);
	}
	
	public void setClip(final Clip clip) {
		this.clip = clip;
	}

	@Override
	public void parse(InputSource input) throws SAXException, IOException {
		if (clip == null || getParent() != null) {
			super.parse(input);
		} else {
			serializeClip();
		}
	}

	private void serializeClip() throws SAXException {
		try {
			super.startDocument();
			super.startElement("", "", "data", new AttributesImpl());
			super.startElement("", "", "title", new AttributesImpl());
			serializeField(Clip.class.getField("title"));
			super.endElement("", "", "title");

			super.startElement("", "", "date", new AttributesImpl());
			serializeField(Clip.class.getField("date"));
			super.endElement("", "", "date");

			super.startElement("", "", "category", new AttributesImpl());
			serializeField(Clip.class.getField("category"));
			super.endElement("", "", "category");

			super.endElement("", "", "data");
			super.endDocument();
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		logger.debug("serializing start element {}:{}:{}", new Object[] { uri, localName, qName });
		try {
			context = Clip.class.getField(qName);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			logger.debug("No field with the name [{}] found in Clip", new Object[] { qName });
		}

		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (context == null) {
			super.characters(ch, start, length);
		} else {
			serializeField(context);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		context = null;
		
		super.endElement(uri, localName, qName);
	}

	private void serializeField(final Field field) throws SAXException {
		String value = "";
		try {
			if (field.get(clip) == null) {
				;
			} else if (field.getType() == String.class) {
				value = (String) field.get(clip);

			} else if (field.getType() == Date.class) {
				value = Clip.DATE_FORMAT.format((Date) field.get(clip));

			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		logger.debug("New string value {} for {}", new Object[] { value, field.getName() });
		super.characters(value.toCharArray(), 0, value.length());
	}

}
