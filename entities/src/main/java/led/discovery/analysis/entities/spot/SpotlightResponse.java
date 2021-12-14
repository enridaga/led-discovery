package led.discovery.analysis.entities.spot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SpotlightResponse implements Serializable {
	private static final Logger L = LoggerFactory.getLogger(SpotlightResponse.class);
	private Map<String, String> headers;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpotlightResponse() {
	}

	public SpotlightResponse(String xml, long ms, double confidence, int support, Map<String, String> headers) {
		this.xml = xml;
		this.milliseconds = ms;
		this.confidence = confidence;
		this.headers = headers;
	}

	public String getXml() {
		return xml;
	}
	
	public Map<String,String> getResponseHeaders(){
		return Collections.unmodifiableMap(headers);
	}

	protected void setXml(String xml) {
		this.xml = xml;
	}

	public long getMilliseconds() {
		return milliseconds;
	}

	protected void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	private String xml;
	private long milliseconds;
	private double confidence;

	public List<SpotlightAnnotation> asList() {
		return asList(getXml(), confidence);
//		List<SpotlightAnnotation> annotations;
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		try {
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			org.w3c.dom.Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
//			NodeList nl = dom.getElementsByTagName("Resource");
//			annotations = new ArrayList<SpotlightAnnotation>();
//			for (int i = 0; i < nl.getLength(); ++i) {
//				Node n = nl.item(i);
//				SpotlightAnnotation annotation = new SpotlightAnnotation(n, this.confidence);
//				annotations.add(annotation);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return annotations;
	}

	public static List<SpotlightAnnotation> asList(String xml, double confidence) {
		List<SpotlightAnnotation> annotations;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Force XML 1.1 for lazy treatement of unsupported UTF-8 characters
//		System.out.println(xml);
		xml = xml.replaceFirst("<\\?xml.*\\?>","<?xml version=\"1.1\" encoding=\"UTF-8\"?>");

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			NodeList nl = dom.getElementsByTagName("Resource");
			annotations = new ArrayList<SpotlightAnnotation>();
			for (int i = 0; i < nl.getLength(); ++i) {
				Node n = nl.item(i);
				SpotlightAnnotation annotation = new SpotlightAnnotation(n, confidence);
				annotations.add(annotation);
			}
		} catch (Exception e) {
			L.error("Cannot parse XML", e);
			annotations = Collections.emptyList();
		}
		return annotations;
	}

}
