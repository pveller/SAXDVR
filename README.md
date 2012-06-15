SAXDVR
======

A simple implementation of XML Filter (org.xml.sax.XMLFilter) that **records all SAX events and can later replay them back**. You can use SAXDVR to do just that or **to overlay partial updates on top of the XML structure that you don't fully know but need to preserve**. [This question over at StackOverflow](http://stackoverflow.com/questions/10648651/how-to-preserve-xml-nodes-that-are-not-bound-to-an-object-when-using-sax-for-par) triggered the idea of the SAXDVR, specifically the usage of it for "partial updates". 

Limitations
-----------

The **RecorderProxy** supports basic XML document events: start/end document, start/end element, characters. Adding support for more SAX events (like processinng instruction or ignorable whitespace) should be very easy through extending *org.playground.saxdvr.Event* class.

The **Parser** and **Serializer** are very simple and map XML node names to the Java bean's properties using introspection without type conversion. No support for complex structures (property nesting), indexed properties, properties from attributes, etc. It's good enough to illustrate the concept but nearly not enough to use it as your advanced Java <-> XML mapper. 

Record and Replay
-----------------

Here's how you record and replay SAX events:

``` java
@Test
public void testParseReplay() throws Exception {
	final SAXParserFactory factory = SAXParserFactory.newInstance();
	final SAXParser parser = factory.newSAXParser();
	
	final RecorderProxy proxy = new RecorderProxy(parser.getXMLReader());
	
	final String srcXml =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<clip>" +
					"<title>My Title</title>" +
					"<date>12/24/2012</date>" +
					"<category>Blah!</category>" +
					"</clip>";
	final InputSource src = new InputSource(new StringReader(srcXml));
	
	proxy.parse(src);
	
	assertTrue(proxy.hasRecordingToReplay());
	
	final TransformerFactory xsltFactory = TransformerFactory.newInstance();
	final Transformer t = xsltFactory.newTransformer();
	
	final StringWriter outXmlBuffer = new StringWriter();
	
	t.transform(new SAXSource(proxy, new InputSource()),
			new StreamResult(outXmlBuffer));
	
	assertEquals(srcXml, outXmlBuffer.getBuffer().toString());
}
```

Overlay Partial Updates
-----------------------
	
You can also relay SAX events to another consumer (e.g. an XML-to-Object mapper) and then use the recorded stream to serialize it back with partial updates overlaid on top.  Please refer to the mentioned StackOverflow question for the detailed use case. Here's how you would do it with SAXDVR:

``` java
	final SAXParserFactory factory = SAXParserFactory.newInstance();
	final SAXParser parser = factory.newSAXParser();
	final InputSource src = new InputSource(new StringReader(srcXml));
	final Clip subject = new Clip();
	
	final RecorderProxy recorder = new RecorderProxy(parser.getXMLReader());
	// XML-to-Object mapper is wrapped over the recorder
	// The recorder will record and relay the events up the filter chain
	final Parser<Clip> clipHolder = new Parser<Clip>(subject, recorder);
	
	clipHolder.parse(src);
	
	final Clip clip = clipHolder.getSubject();
	clip.setTitle("My Title Updated");
	clip.setCategory("Something else");
	
	// Now we wrap Obejct-to-XML mapper over the recorder
	// the recorder will replay events and the mapper will overlay changes on top
	final Serializer<Clip> serializer = new Serializer<Clip>(recorder);
	serializer.setSubject(clip);
	
	final TransformerFactory xsltFactory = TransformerFactory.newInstance();
	final Transformer t = xsltFactory.newTransformer();
	final StringWriter outXmlBuffer = new StringWriter();
	
	t.transform(new SAXSource(serializer, new InputSource()), new StreamResult(outXmlBuffer));
	
	final String resultXml = outXmlBuffer.getBuffer().toString();
```		
		
You will get better idea by looking at the code and the unit tests. All the examples above are snippets from the tests. 

Good luck and please leave a comment if you find it useful.
