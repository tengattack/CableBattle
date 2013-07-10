
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;

public class LevelTitlePanel extends DescriptionPanel {

	static final int HEIGHT = 55;

	int level = 0;

	public LevelTitlePanel() {
		super(MainWindow.WIDTH, HEIGHT);
        
        //Border blackline = BorderFactory.createLineBorder(Color.BLACK);
		//setBorder(blackline);
	}
	
	public void setLevelInfo(int level, String name, String description) {
		this.level = level;
		
		this.name = String.format("Level %d", level);
		if (name != null && !name.isEmpty()) {
			this.name += " - " + name;
		}
		this.description = description;
		
		repaint();
	}
}
