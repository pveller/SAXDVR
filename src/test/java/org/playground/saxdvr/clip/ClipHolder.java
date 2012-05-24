package org.playground.saxdvr.clip;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class ClipHolder extends XMLFilterImpl {

	private final Clip clip = new Clip();

	private Field context;

	private final Logger logger = LoggerFactory.getLogger(ClipHolder.class);

	public ClipHolder(XMLReader parent) {
		super(parent);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		logger.debug("reading start element {}:{}:{}", new Object[] { uri, localName, qName });
		try {
			context = Clip.class.getField(qName);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			logger.debug("No field with the name [{}] found in Clip", new Object[] { qName });
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (context == null) {
			return;
		}

		final String newValue = new String(Arrays.copyOfRange(ch, start, start + length));
		logger.debug("parsing out value {} for {}", new Object[] { newValue, context.getName()});
		try {
			// let's assume the values will come as a whole in one characters()
			if (String.class == context.getType()) {
				context.set(clip, newValue);
			} else if (Date.class == context.getType()) {
				try {
					context.set(clip, Clip.DATE_FORMAT.parse(newValue));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		context = null;
	}

	public Clip getClip() {
		return clip;
	}
}
