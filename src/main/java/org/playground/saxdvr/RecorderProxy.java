package org.playground.saxdvr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class RecorderProxy extends XMLFilterImpl {

	private final List<Event> events = new ArrayList<Event>();

	public RecorderProxy(XMLReader parent) {
		super(parent);
	}

	public void replay(final ContentHandler handle) throws SAXException {
		for (Event e : events) {
			e.fire(handle);
		}
	}

	public boolean hasRecordingToReplay() {
		return events.size() > 0;
	}
	
	@Override
	public void parse(final InputSource input) throws SAXException, IOException {
		if (events.size() == 0) {
			super.parse(input);
		} else {
			replay(getContentHandler());
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		events.add(new Event(Event.Type.CHARACTERS, new Object[] {
				Arrays.copyOfRange(ch, start, start + length), 0, length }));

		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		events.add(new Event(Event.Type.END_DOCUMENT, new Object[] {}));

		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		events.add(new Event(Event.Type.END_ELEMENT, new Object[] { uri, localName, qName }));

		super.endElement(uri, localName, qName);
	}

	@Override
	public void startDocument() throws SAXException {
		events.add(new Event(Event.Type.START_DOCUMENT, new Object[] {}));

		super.startDocument();

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		events.add(new Event(Event.Type.START_ELEMENT, new Object[] { uri, localName, qName, atts }));

		super.startElement(uri, localName, qName, atts);
	}

}
