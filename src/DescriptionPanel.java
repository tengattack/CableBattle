
import javax.swing.*;
import javax.swing.border.Border;

import util.StringGraphics;
import util.StringGraphicsAlignment;

import java.awt.*;
import java.awt.event.*;

public class DescriptionPanel extends JPanel {

	static final int NAME_X = 3;
	static final int NAME_Y = 2;
	static final int NAME_FONT_SIZE = 18;
	
	static final int TIPS_X = NAME_X;
	static final int TIPS_Y = NAME_Y + NAME_FONT_SIZE + 5;
	static final int TIPS_FONT_SIZE = 14;
	
	Font nameFont, infoFont;
	
	String name = new String();
	String description = new String();
	
	int width, height;

	public DescriptionPanel(int width, int height) {
		super(false);

		this.width = width;
		this.height = height;
		
		setPreferredSize(new Dimension(width, height));
        setLayout(new FlowLayout());
		
		initFont();
	}

	public void initFont() {
		nameFont = CableBattle.cb.resource.baseFont.deriveFont(Font.BOLD, NAME_FONT_SIZE);
		infoFont = CableBattle.cb.resource.baseFont.deriveFont(Font.PLAIN, TIPS_FONT_SIZE);
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	
		drawText(g);
	}
	
	public void drawText(Graphics g) {
		g.setFont(nameFont);
		g.drawString(name, NAME_X, NAME_Y + nameFont.getSize());
		
		/*g.setFont(infoFont);
		g.drawString(description, TIPS_X, TIPS_Y + infoFont.getSize());*/
		Rectangle rc = new Rectangle(TIPS_X, TIPS_Y, 
				this.width - TIPS_X * 2, this.height - TIPS_Y);
		StringGraphics.drawString((Graphics2D)g, rc, description, Color.BLACK, infoFont,
				StringGraphicsAlignment.Left, StringGraphicsAlignment.Top, true);
	}
}
