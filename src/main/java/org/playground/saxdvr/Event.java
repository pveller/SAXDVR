package org.playground.saxdvr;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Event {

	public enum Type {
		START_DOCUMENT,
		END_DOCUMENT,
		START_ELEMENT,
		END_ELEMENT,
		CHARACTERS
	}

	private final Type type;
	private final Object[] params;

	public Event(final Type type, Object[] params) {
		this.type = type;
		this.params = params;
	}

	public void fire(final ContentHandler handle) throws SAXException {
		switch (type) {
			case START_DOCUMENT:
				handle.startDocument();
				break;
			case END_DOCUMENT:
				handle.endDocument();
				break;
			case START_ELEMENT:
				handle.startElement((String) params[0], (String) params[1], (String) params[2],
						(Attributes) params[3]);
				break;
			case END_ELEMENT:
				handle.endElement((String) params[0], (String) params[1], (String) params[2]);
				break;
			case CHARACTERS:
				handle.characters((char[]) params[0], (Integer) params[1], (Integer) params[2]);
				break;
		}
	}
}
