package led.discovery.annotator.window;

public class FixedWindow extends MovingWindow {
	public FixedWindow() {
		super(-1,-1,-1);
	}
	
	public void produce() {
		flush();
	}
}
