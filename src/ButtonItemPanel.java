import javax.swing.*;

import util.MouseState;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

interface ButtonMessage {
	public void onButtonClick(int index);
	public void onButtonSelect(int index);
}

class ButtonStyle {
	int width;
	int height;
	int space;
	//int radius;
	
	int font_style;
	float font_size;
	
	public ButtonStyle(int width, int height, int space, int font_style, float font_size) {
		this.width = width;
		this.height = height;
		this.space = space;
		//this.radius = radius;
		
		this.font_style = font_style;
		this.font_size = font_size;
	}
}

class ButtonItem {
	
	String text;
	int id = 0;
	int left = 0, top = 0, width = 0, height = 0;
	
	static final int RADIUS = 6;
	
	public ButtonItem(String text, int id) {
		this.id = id;
		setText(text);
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setPosition(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	public boolean in(int x, int y) {
		return (left <= x && top <= y && x <= left + width && y <= top + height);
	}
	
	public void draw(Graphics g, MouseState ms) {
		
		switch (ms) {
		case LEAVE:
			g.setColor(Color.BLACK);
			break;
		case HOVER:
			g.setColor(Color.BLUE);
			break;
		case CLICK:
			g.setColor(Color.RED);
			break;
		default:
			g.setColor(Color.BLACK);
			break;
		}

		g.drawRoundRect(left, top, width, height, RADIUS, RADIUS);
		
		FontMetrics fm = g.getFontMetrics();
		int textWidth = fm.stringWidth(text);
		int textAscent = fm.getAscent(), textDescent = fm.getDescent();
		
		int textLeft = (int)(left + (width - textWidth) / 2);
		int textTop = top + (height - (textAscent + textDescent)) / 2 + textAscent;

		g.drawString(text, textLeft, textTop);
	}
}

public class ButtonItemPanel extends DoubleBufferPanel implements MouseMotionListener, MouseListener, ButtonMessage {
	
	ArrayList<ButtonItem> list = new ArrayList<ButtonItem>();
	int lastHoverIndex = -1, lastClickIndex = -1;
	
	Font buttonFont;
	static final ButtonStyle defaultButtonStyle = new ButtonStyle(60, 25, 5, Font.PLAIN, 18);
	
	int width, height;
	boolean bstopDraw = false;
	
	public ButtonItemPanel(int width, int height) {

		this.width = width;
		this.height = height;

		setPreferredSize(new Dimension(width, height));
        setLayout(new FlowLayout());

        initFont();
        initDoubleBuffer(width, height);

		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public void initFont() {
		ButtonStyle bs = getButtonStyle();
		buttonFont = CableBattle.cb.resource.baseFont.deriveFont(bs.font_style, bs.font_size);
	}

	public void initDoubleBuffer(int width, int height) {
		super.initDoubleBuffer(width, height);
		
		Graphics2D g2d = (Graphics2D)graphics;
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		
		graphics.setFont(buttonFont);

		drawBackground(graphics);
	}

	public void paint(Graphics g) {
		super.paint(g);

		for (int i = 0; i < list.size(); i++) {
			drawButton(null, i, MouseState.LEAVE);
		}
		
		g.drawImage(screen, 0, 0, this);
	}
	
	public void setStopDraw(boolean b) {
		bstopDraw = b;
	}

	public void drawButton(Graphics g, int index, MouseState ms) {

		if (bstopDraw) {
			return;
		}

		ButtonItem bi = list.get(index);

		//draw background
		graphics.setColor(Color.WHITE);
		graphics.fillRoundRect(bi.left, bi.top, bi.width, bi.height, ButtonItem.RADIUS, ButtonItem.RADIUS);
		
		bi.draw(graphics, ms);
		
		if (g != null) {
			//g.drawImage(screen, 0, 0, null);
			g.drawImage(screen, bi.left, bi.top, bi.left + bi.width + 1, bi.top + bi.height + 1,
				bi.left, bi.top, bi.left + bi.width + 1, bi.top + bi.height + 1, this);
		}
	}
	
	public ButtonStyle getButtonStyle() {
		return defaultButtonStyle;
	}
	
	public void drawBackground(Graphics g) {
		if (CableBattle.mainWindow != null && CableBattle.mainWindow.backgroundColor != null) {
			g.setColor(CableBattle.mainWindow.backgroundColor);
			g.fillRect(0, 0, width, height);
		}
	}
	
	public void clearButton() {
		list.clear();
		
		lastHoverIndex = lastClickIndex = -1;
		
		drawBackground(graphics);
	}
	
	public void addButton(String text, int id) {
		list.add(new ButtonItem(text, id));
	}
	
	public void calcButton() {
		ButtonStyle bs = getButtonStyle();
		
		int left = (this.width - bs.width) / 2;
		int top = (int)((this.height - (bs.height + bs.space) * list.size()) * 0.618f);
		
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setPosition(left, top, bs.width, bs.height);
			top += bs.height + bs.space;
		}
	}

	public int hitTest(int x, int y) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).in(x, y)) {
				return i;
			}
		}
		return -1;
	}
	
	public void onButtonClick(int index) {
	}
	
	public void onButtonSelect(int index) {
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (lastClickIndex != -1 || lastHoverIndex != -1) {
			Graphics g = this.getGraphics();
			if (lastClickIndex != -1) {
				drawButton(g, lastClickIndex, MouseState.LEAVE);
			}
			if (lastHoverIndex != -1 && lastHoverIndex != lastClickIndex) {
				drawButton(g, lastHoverIndex, MouseState.LEAVE);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int index = hitTest(e.getX(), e.getY());
		if (index == lastClickIndex) {
			return;
		}
		if (index != -1) {
			Graphics g = this.getGraphics();
				
			if (lastClickIndex != -1) {
				drawButton(g, lastClickIndex, MouseState.LEAVE);
			}
			drawButton(g, index, MouseState.CLICK);
			
			lastClickIndex = index;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int index = hitTest(e.getX(), e.getY());
		
		if (index != -1) {
			if (index == lastClickIndex) {

				Graphics g = this.getGraphics();
				drawButton(g, lastClickIndex, MouseState.HOVER);
				
				onButtonClick(index);
			}
		}
		lastClickIndex = -1;
	}
	
	public void mouseMoved(int x, int y, MouseState ms) {
		int index = hitTest(x, y);
		
		if (index == lastHoverIndex) {
			return;
		}
		
		Graphics g = this.getGraphics();
		
		if (lastHoverIndex != -1) {
			drawButton(g, lastHoverIndex, MouseState.LEAVE);
		}
		if (index != -1) {
			drawButton(g, index, ms);
			onButtonSelect(index);
		}
		lastHoverIndex = index;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e.getX(), e.getY(), MouseState.CLICK);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseMoved(e.getX(), e.getY(), MouseState.HOVER);
	}

}
