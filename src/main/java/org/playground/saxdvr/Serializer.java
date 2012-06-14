package org.playground.saxdvr;

import java.beans.PropertyDescriptor;
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

public class Serializer<T> extends XMLFilterImpl {

	private T subject;
	private String context;

	private final Logger logger = LoggerFactory.getLogger(Serializer.class);

	public Serializer() {
		super();
	}

	public Serializer(XMLReader parent) {
		super(parent);
	}

	public void setSubject(final T subject) {
		this.subject = subject;
	}

	@Override
	public void parse(InputSource input) throws SAXException, IOException {
		if (subject == null || getParent() != null) {
			super.parse(input);
		} else {
			serializeSubject();
		}
	}

	private void serializeSubject() throws SAXException {
		final String objectName = subject.getClass().getSimpleName().toLowerCase();
		final PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(subject);

		super.startDocument();
		super.startElement("", "", objectName, new AttributesImpl());
		for (PropertyDescriptor property : properties) {
			if ("class".equals(property.getName()) && (Class.class == property.getPropertyType())) {
				continue;
			}

			final String propertyName = property.getName();

			super.startElement("", "", propertyName, new AttributesImpl());
			serializeField(propertyName);
			super.endElement("", "", propertyName);

		}
		super.endElement("", "", objectName);
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		logger.debug("serializing start element {}:{}:{}", new Object[] { uri, localName, qName });

		context = PropertyUtils.isWriteable(subject, qName) ? qName : null;

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
			value = BeanUtils.getProperty(subject, field);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.debug("New string value {} for {}", new Object[] { value, field });

		super.characters(value.toCharArray(), 0, value.length());
	}

}
