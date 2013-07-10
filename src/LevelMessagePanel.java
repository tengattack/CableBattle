
import javax.swing.*;

import util.StringGraphics;

import java.awt.*;

import util.*;

public class LevelMessagePanel extends DescriptionPanel {

	static final int WIDTH = MainWindow.WIDTH;
	static final int HEIGHT = MainWindow.HEIGHT;
	
	static final int LEVEL_FONT_SIZE = 20;
	static final int NAME_FONT_SIZE = 42;
	static final int TIPS_FONT_SIZE = 30;

	static final int TEXT_X = 100;
	static final int TEXT_Y = (int)(HEIGHT * (1.0f - 0.618f));
	
	Font levelFont;
	int level = 0;
	
	public LevelMessagePanel() {
		super(WIDTH, HEIGHT);
	}

	public void drawText(Graphics g) {
		//maybe level0 exist. :)
		
		int textY = TEXT_Y;
		
		g.setFont(levelFont);
		textY += g.getFontMetrics().getHeight();
		if (level >= 0) {
			g.drawString(String.format("level %d", level), TEXT_X, textY);
		}

		g.setFont(nameFont);
		textY += g.getFontMetrics().getHeight();
		g.drawString(name, TEXT_X, textY);
		
		if (description != null) {
			/*g.setFont(infoFont);
			textY += g.getFontMetrics().getHeight();
			g.drawString(description, TEXT_X, textY);*/

			textY += 5;
			Rectangle rc = new Rectangle(TEXT_X, textY, 
					WIDTH - 2 * TEXT_X, HEIGHT - textY);
			StringGraphics.drawString((Graphics2D)g, rc, description, Color.BLACK, infoFont,
					StringGraphicsAlignment.Left, StringGraphicsAlignment.Top, true);
		}
	}
	
	public void initFont() {
		levelFont = CableBattle.cb.resource.baseFont.deriveFont(Font.PLAIN, LEVEL_FONT_SIZE);
		nameFont = CableBattle.cb.resource.baseFont.deriveFont(Font.BOLD, NAME_FONT_SIZE);
		infoFont = CableBattle.cb.resource.baseFont.deriveFont(Font.PLAIN, TIPS_FONT_SIZE);
	}
	
	public void setMessage(int level, String name, String description) {
		this.level = level;
		this.name = name;
		this.description = description;
		
		if (this.isVisible()) {
			repaint();
		}
	}
	
	public void setMessage(String name, String description) {
		setMessage(-1, name, description);
	}
}
