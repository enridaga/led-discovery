package led.discovery.analysis.entities.spot;

import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SpotlightAnnotation {

	private String uri;
	private int support;
	private ArrayList<String> types;
	private String surfaceForm;
	private int offset;
	private double similarity;
	private double secondRankPct;
	private double confidence;

	public SpotlightAnnotation(String uri, int support, ArrayList<String> types, String sF, int offset, double sim, double sRP, double c) {
		this.uri = uri;
		this.support = support;
		this.types = types;
		this.surfaceForm = sF;
		this.offset = offset;
		this.similarity = sim;
		this.secondRankPct = sRP;
		this.confidence = c;
	}

	public SpotlightAnnotation(Node xml, double confidence) {
		this.confidence = confidence;
		parseDomNode(xml);
	}

	private void parseDomNode(Node xml) {
		NamedNodeMap nm = xml.getAttributes();
		uri = (nm.getNamedItem("URI") != null) ? nm.getNamedItem("URI").getTextContent() : null;
		surfaceForm = (nm.getNamedItem("surfaceForm") != null) ? nm.getNamedItem("surfaceForm").getTextContent() : null;
		similarity = (nm.getNamedItem("similarityScore") != null) ? Double.parseDouble(nm.getNamedItem("similarityScore").getTextContent()) : null;
		support = (nm.getNamedItem("support") != null) ? Integer.parseInt(nm.getNamedItem("support").getTextContent()) : null;
		offset = (nm.getNamedItem("offset") != null) ? Integer.parseInt(nm.getNamedItem("offset").getTextContent()) : null;
		secondRankPct = (nm.getNamedItem("percentageOfSecondRank") != null) ? Double.parseDouble(nm.getNamedItem("percentageOfSecondRank").getTextContent()) : null;
		types = (nm.getNamedItem("types") != null) ? new ArrayList<String>(Arrays.asList(nm.getNamedItem("types").getTextContent().split(","))) : null;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getSupport() {
		return support;
	}

	public void setSupport(int support) {
		this.support = support;
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public double getSecondRankPct() {
		return secondRankPct;
	}

	public void setSecondRankPct(double secondRankPct) {
		this.secondRankPct = secondRankPct;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public String toString() {
		return new StringBuilder().append(uri).append("[").append(offset).append("]").toString();
	}
}
