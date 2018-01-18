package led.discovery.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Term {
	private String lemma;
	private String pos;
	private int hashCode;

	private Term(String lemma, String pos) {
		this.lemma = lemma;
		this.pos = pos;
		HashCodeBuilder hb = new HashCodeBuilder(1, 4999);
		this.hashCode = hb.append(lemma).append(pos).append(Term.class).build();
	}

	public String getLemma() {
		return lemma;
	}

	public String getPOS() {
		return pos;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Term) {
			return lemma.equals(((Term) other).getLemma()) && pos.equals(((Term) other).getPOS());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		return lemma + "[" + pos + "]";
	}

	public final static Term build(String lemma, String pos) {
		return new Term(lemma, pos);
	}

	public final static List<Term> buildList(String[] str, String separator) {
		List<Term> ts = new ArrayList<Term>();
		for (String s : str) {
			String[] ss = s.split("|");
			String lemma = ss[0];
			String pos;
			if(ss.length == 1) {
				pos = "";
			}else {
				pos = ss[1];
			}
			ts.add(Term.build(lemma, pos));
		}
		return ts;
	}

	/**
	 * Separator is pipe: |
	 * 
	 * @param string
	 * @return
	 */
	public final static List<Term> buildList(String... str ) {
		return buildList(str, "|");
	}
}
