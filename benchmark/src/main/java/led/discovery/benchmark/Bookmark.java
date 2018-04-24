package led.discovery.benchmark;

public class Bookmark {

	private int from;
	private int to;
	private int score;

	public Bookmark(int from, int to, int score) {
		this.from = from;
		this.to = to;
		this.score = score;
	}

	public String toString() {
		return new StringBuilder().append(from).append(':').append(to).toString();
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int getScore() {
		return score;
	}

	static final Bookmark empty = new Bookmark(-1, -1, -1);

	public double getNormalisedScore() {
		return getNormalisedScore(getScore(), getTo() - getFrom());
	}

	public static final double getNormalisedScore(int score, int length) {
		return ((double) score) / ((double) length);
	}
}