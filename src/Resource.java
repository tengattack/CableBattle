
import java.awt.*;
import java.io.*;

import javax.imageio.ImageIO;

public class Resource {
	
	String path;
	
	Image blockImage[] = new Image[BlockType.COUNT];
	Image propsImage[] = new Image[PropsType.COUNT];
	
	Image favicon, titleImage;

	Font baseFont;
	Level level;
	Sound sound;
	
	public Resource(String resourcePath) {
		this.path = resourcePath;
		
		loadFont();
		loadImage();
		loadLevel();
		loadSound();
	}
	
	public void loadImage() {
		favicon = getImage("favicon");
		titleImage = getImage("title");
	}
	
	public void loadLevel() {
		level = new Level(path + "level/");
	}
	
	public void loadFont() {
		try {
			FileInputStream fi = new FileInputStream(path + "font/default.ttf");
			BufferedInputStream fb = new BufferedInputStream(fi);
			baseFont = Font.createFont(Font.TRUETYPE_FONT, fb);
	    } catch (Exception e) {
	    	System.out.println(e.getMessage());
	    	baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	    }
	}
	
	public void loadSound() {
		sound = new Sound(path + "sound/");
	}
	
	public Image getImage(String imageName) {
		try {
            File imageFile = new File(path + "image/" + imageName + ".png");
            return ImageIO.read(imageFile);
        } catch (IOException e) {
        	//System.out.println("image: " + imageName);
            e.printStackTrace();
        }
		return null;
	}
	
	public Image getBlockImage(BlockType type) {
		if (blockImage[type.getIndex()] == null) {
			blockImage[type.getIndex()] = getImage(type.getKeyName());
		}
		return blockImage[type.getIndex()];
	}
	
	public Image getPropsImage(PropsType type) {
		if (propsImage[type.getIndex()] == null) {
			propsImage[type.getIndex()] = getImage(type.getKeyName());
		}
		return propsImage[type.getIndex()];
	}
}
