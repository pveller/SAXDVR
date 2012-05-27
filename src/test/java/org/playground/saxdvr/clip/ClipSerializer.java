package org.playground.saxdvr.clip;

import java.io.IOException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
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
	private String context;

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
		super.startDocument();
		super.startElement("", "", "data", new AttributesImpl());
		super.startElement("", "", "title", new AttributesImpl());
		serializeField("title");
		super.endElement("", "", "title");

		super.startElement("", "", "date", new AttributesImpl());
		serializeField("date");
		super.endElement("", "", "date");

		super.startElement("", "", "category", new AttributesImpl());
		serializeField("category");
		super.endElement("", "", "category");

		super.endElement("", "", "data");
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		logger.debug("serializing start element {}:{}:{}", new Object[] { uri, localName, qName });

		context = PropertyUtils.isWriteable(clip, qName) ? qName : null;

		if (context == null) {
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

	private void serializeField(final String field) throws SAXException {
		String value = "";

		try {
			value = BeanUtils.getProperty(clip, field);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.debug("New string value {} for {}", new Object[] { value, field });

		super.characters(value.toCharArray(), 0, value.length());
	}

}
