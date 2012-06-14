package org.playground.saxdvr;

import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class Parser<T> extends XMLFilterImpl {

	private final T subject;

	private String context;

	private final Logger logger = LoggerFactory.getLogger(Parser.class);

	public Parser(T subject, XMLReader parent) {
		super(parent);

		this.subject = subject;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		logger.debug("reading start element {}:{}:{}", new Object[] { uri, localName, qName });

		context = PropertyUtils.isWriteable(subject, qName) ? qName : null;

		if (context == null) {
			logger.debug("No field with the name [{}] found in Clip", new Object[] { qName });
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (context != null) {
			// let's assume the values will come as a whole in one characters()
			final String newValue = new String(Arrays.copyOfRange(ch, start, start + length));

			logger.debug("parsing out value {} for {}", new Object[] { newValue, context });

			try {
				BeanUtils.setProperty(subject, context, newValue);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		context = null;
	}

	public T getSubject() {
		return subject;
	}
}
