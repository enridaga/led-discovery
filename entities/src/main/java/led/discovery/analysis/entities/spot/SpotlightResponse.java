package led.discovery.analysis.entities.spot;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SpotlightResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpotlightResponse() {
	}

	public SpotlightResponse(String xml, long ms, double confidence, int support) {
		this.xml = xml;
		this.milliseconds = ms;
		this.confidence = confidence;
	}

	public String getXml() {
		return xml;
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
			throw new RuntimeException(e);
		}
		return annotations;
	}

}
