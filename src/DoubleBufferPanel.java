
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class DoubleBufferPanel extends JPanel {
	
	// double buffer
	Image screen;
	Graphics graphics;
	
	public DoubleBufferPanel() {
		super(false);
	}
	
	public DoubleBufferPanel(boolean b) {
		super(b);
	}
	
	public void initDoubleBuffer(int width, int height) {
		screen = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		graphics = screen.getGraphics();
	}
}
