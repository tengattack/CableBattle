
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.border.*;

import util.MouseState;

class PropsItemPanel extends DoubleBufferPanel implements MouseMotionListener, MouseListener, ButtonMessage {
	
	static final int WIDTH = PropsBarPanel.WIDTH / 2 - OperatePanel.WIDTH;
	static final int HEIGHT = 64;
	
	static final int ITEM_WIDTH = 58;
	static final int ITEM_HEIGHT = 58;
	
	static final int PROPS_LEFT = (ITEM_WIDTH - BlockPanel.WIDTH) / 2;
	static final int PROPS_TOP = 2;
	
	static final int PROPS_NAME_X = 2;
	static final int PROPS_NAME_Y = ITEM_HEIGHT - 2;
	
	static final int AMOUNT_X = 2;
	static final int AMOUNT_Y = 2;
	
	static final int SPACE = 3;
	static final int SPACE_WIDTH = 6;
	static final int RADIUS = 6;
	
	int lastHoverIndex = -1, lastClickIndex = -1, lastSelectIndex = -1;
	
	int startLeft = SPACE;
	
	Font font;
	
	public PropsItemPanel() {
		super(false);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		font = CableBattle.cb.resource.baseFont.deriveFont(Font.PLAIN, 12);
		initDoubleBuffer(WIDTH, HEIGHT);

		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		for (int i = 0; i < CableBattle.propsBar.list.size(); i++) {
			drawItem(null, i, MouseState.LEAVE);
		}
		
		g.drawImage(screen, 0, 0, this);
	}
	
	public void initDoubleBuffer(int width, int height) {
		super.initDoubleBuffer(width, height);
		
		Graphics2D g2d = (Graphics2D)graphics;
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		
		graphics.setFont(font);
		//graphics.clearRect(0, 0, WIDTH, HEIGHT);
	}
	
	public void drawItem(Graphics g, int index, MouseState ms) {
		PropsItem item = CableBattle.propsBar.getItem(index);

		//int left = SPACE + ((ITEM_WIDTH + SPACE_WIDTH) * index); //LEFT_ALIGNMENT
		int left = startLeft + ((ITEM_WIDTH + SPACE_WIDTH) * index);
		int top = SPACE;
		
		/*Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);*/
		
		// draw background
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillRoundRect(left, top, ITEM_WIDTH, ITEM_HEIGHT, RADIUS, RADIUS);

		switch (ms) {
		case HOVER:
			graphics.setColor(Color.GREEN);
			break;
		case CLICK:
		default:
			if (CableBattle.propsBar.isSelect(index)) {
				graphics.setColor(Color.RED);
			} else if (ms == MouseState.CLICK) {
				graphics.setColor(Color.BLUE);
			} else {
				graphics.setColor(Color.BLACK);
			}
			break;
		}
		
		graphics.drawRoundRect(left, top, ITEM_WIDTH, ITEM_HEIGHT, RADIUS, RADIUS);
		
		PropsTypeDrawer.draw(graphics, left + PROPS_LEFT, top + PROPS_TOP, ms, item.type, this);
		
		// make sure text color is black
		graphics.setColor(Color.BLACK);
		graphics.drawString(item.type.getName(), left + PROPS_NAME_X, top + PROPS_NAME_Y);
		graphics.drawString(((item.amount == -1) ? "∞" : String.format("%d", item.amount)),
			left + AMOUNT_X, top + AMOUNT_Y + graphics.getFont().getSize());
		
		if (g != null) {
			g.drawImage(screen, left, top, left + ITEM_WIDTH + 1, top + ITEM_HEIGHT + 1,
				left, top, left + ITEM_WIDTH + 1, top + ITEM_HEIGHT + 1, this);
		}
	}
	
	/*public void updateLeft() {
		int count = CableBattle.propsBar.list.size();
		if (count == 0) {
			startLeft = SPACE;
		} else {
			startLeft = (MainWindow.WIDTH - ((ITEM_WIDTH + SPACE_WIDTH) * count)) / 2;
		}
	}*/
	
	public void refresh() {
		lastHoverIndex = lastClickIndex = lastSelectIndex = -1;
		
		graphics.setColor(CableBattle.mainWindow.backgroundColor);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		repaint();
	}
	
	public void updateItem(int index) {
		drawItem(getGraphics(), index, MouseState.LEAVE);
	}
	
	public int hitTest(int x, int y) {
		if (y > SPACE && y < SPACE + ITEM_HEIGHT) {
			int t = x - startLeft;
			if (t > 0 && (t % (ITEM_WIDTH + SPACE_WIDTH) < ITEM_WIDTH)) {
				int index = t / (ITEM_WIDTH + SPACE_WIDTH);
				if (index < CableBattle.propsBar.list.size()) {
					return index;
				}
			}
		}
		return -1;
	}
	
	public void onButtonClick(int index) {
		if (CableBattle.propsBar.select(index)) {
			if (index == lastSelectIndex) {
				return;
			}
			
			Graphics g = this.getGraphics();
			
			if (lastSelectIndex != -1) {
				drawItem(g, lastSelectIndex, MouseState.LEAVE);
			}
			drawItem(g, index, MouseState.CLICK);
			
			lastSelectIndex = index;
			
			CableBattle.mainWindow.propsBarPanel.refresh();
		}
	}
	
	public void onButtonSelect(int index) {
		CableBattle.getSound().playEffect(SoundEffect.ITEM);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
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
				drawItem(g, lastClickIndex, MouseState.LEAVE);
			}
			if (lastHoverIndex != -1 && lastHoverIndex != lastClickIndex) {
				drawItem(g, lastHoverIndex, MouseState.LEAVE);
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
				drawItem(g, lastClickIndex, MouseState.LEAVE);
			}
			drawItem(g, index, MouseState.CLICK);
			
			lastClickIndex = index;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int index = hitTest(e.getX(), e.getY());
		
		if (index != -1) {
			if (index == lastClickIndex) {

				CableBattle.getSound().playEffect(SoundEffect.USE);
				
				Graphics g = this.getGraphics();
				drawItem(g, lastClickIndex, MouseState.HOVER);
				
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
			drawItem(g, lastHoverIndex, MouseState.LEAVE);
		}
		if (index != -1) {
			drawItem(g, index, ms);
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

class PropsInfoPanel extends DescriptionPanel {
	
	static final int WIDTH = PropsBarPanel.WIDTH / 2;
	static final int HEIGHT = PropsBarPanel.HEIGHT;
	
	PropsType type = PropsType.NONE;
	
	public PropsInfoPanel() {
		super(WIDTH, HEIGHT);
	}
	
	public void setPropsInfo(PropsType type) {
		this.type = type;
		
		this.name = type.getName();
		this.description = type.getTypeTips();
		
		repaint();
	}
}

public class PropsBarPanel extends JPanel {
	
	static final int WIDTH = MainWindow.WIDTH;
	static final int HEIGHT = PropsItemPanel.HEIGHT;

	PropsItemPanel controls;
	PropsInfoPanel info;
	OperatePanel operate;
	
	public PropsBarPanel() {
		//super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		Border blackline = BorderFactory.createLineBorder(Color.BLACK);
		setBorder(blackline);
		
		controls = new PropsItemPanel();
		info = new PropsInfoPanel();
		operate = new OperatePanel();
 
		add(controls);
		add(info);
		add(operate);
	}

	public void refresh() {
		setPropsInfo(CableBattle.propsBar.getSelectType());
	}
	
	public void setPropsInfo(PropsType type) {
		info.setPropsInfo(type);
	}
}
