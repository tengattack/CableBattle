
import javax.swing.*;

import util.Clip;
import util.MouseState;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

class StagePanelAnimation extends Thread {
	
	static final int WALK_STEP = 6;
	
	StagePanel stagePanel;
	private boolean stopped = false;
	private boolean paused = false;
	
	public StagePanelAnimation(StagePanel stagePanel) {
		this.stagePanel = stagePanel;
	}
	
	public void run() {
		do {
			MapBlock mb;
			Graphics g = stagePanel.getGraphics();
			for (int y = 0; y < StagePanel.BLOCK_HEIGHT; y++) {
				for (int x = 0; x < StagePanel.BLOCK_WIDTH; x++) {
					//mb = null;
					try {
						mb = CableBattle.stage.getMapBlock(x, y);
					} catch (Exception e) {
						if (stopped) {
							break;
						}
						continue;
					}
					boolean notDoAnimation = false;
					BlockPoint meatbp = new BlockPoint();
					BlockPoint pigbp = null;
					if (mb.getProps().type == PropsType.PIG) {
						pigbp = mb.mapprops.getCurrentPoint(new BlockPoint(x, y));	
						if (CableBattle.stage.have(pigbp, PropsType.MEAT, meatbp)) {
							notDoAnimation = true;
						} else {
							BlockPoint cbbp = new BlockPoint();
							while (CableBattle.stage.have(pigbp, PropsType.CABLE, cbbp)) {
								MapBlock tmb = CableBattle.stage.getMapBlock(cbbp);
								// eat the cable
								tmb.getProps().type = PropsType.NONE;
								//tmb.mapprops.draw(g, cbbp, MouseState.LEAVE, stagePanel);
								stagePanel.drawBlock(cbbp, MouseState.LEAVE, true);
							}
						}
					}
					
					if (notDoAnimation || mb.animation()) {
						
						BlockPoint bpLast = stagePanel.bpLast;
						BlockPoint bp = new BlockPoint(x, y);
						
						Clip c;
						if (notDoAnimation && pigbp != null) {
							c = new Clip(pigbp.left(), pigbp.top(), pigbp.left() + BlockPanel.WIDTH, pigbp.top() + BlockPanel.HEIGHT);
						} else {
							c = mb.mapprops.getActiveArea(bp);
						}
						stagePanel.drawBlock(bp, MouseState.LEAVE, false);
						
						MouseState ms = MouseState.LEAVE;
						BlockPoint tbp = bp;
						if (stagePanel.bpLast.available()) {
							if (mb.mapprops.inRange(bpLast, bp)) {
								tbp = bpLast;
								ms = MouseState.HOVER;
							}
						}

						stagePanel.updateBlock(g, c, tbp, ms);
					}
					if (stopped) {
						break;
					}
				}
			}
			try {
				sleep(200);
			} catch (Exception e) {
			}
		} while (!stopped);
	}
	
	public void stopAnimation() {
		stopped = true;
	}
}

public class StagePanel extends DoubleBufferPanel implements MouseMotionListener, MouseListener  {
	
	static final int BLOCK_WIDTH = Stage.BLOCK_WIDTH, BLOCK_HEIGHT = Stage.BLOCK_HEIGHT;
	
	static final int WIDTH = BlockPanel.WIDTH * BLOCK_WIDTH;
	static final int HEIGHT = BlockPanel.HEIGHT * BLOCK_HEIGHT;
	
	BlockPoint bpLast = new BlockPoint();
	StagePanelAnimation animation;

	BufferedImage screenback;
	Graphics gback;
	
	public StagePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		addMouseMotionListener(this);
		addMouseListener(this);
		
		initDoubleBuffer(WIDTH, HEIGHT);
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		for (int x = 0; x < BLOCK_WIDTH; x++) {
			for (int y = 0; y < BLOCK_HEIGHT; y++) {
				drawBlock(new BlockPoint(x, y), MouseState.LEAVE, false);
			}
		}

		g.drawImage(screen, 0, 0, null);
	}
	
	public void initDoubleBuffer(int width, int height) {
		super.initDoubleBuffer(width, height);
		
		screenback = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		gback = screenback.getGraphics();
	}
	
	public void updateMouse(Graphics g, BlockPoint bp, MouseState ms) {
		if (ms == MouseState.LEAVE) {
			return;
		} else {
			g.setColor(Color.BLACK);
		}

		g.drawString(String.format("%d,%d", bp.x, bp.y), bp.left(), (bp.y + 1) * BlockPanel.HEIGHT - 2);
		g.drawRect(bp.left(), bp.top(), BlockPanel.WIDTH - 1, BlockPanel.HEIGHT - 1);
	}
	
	public void updateBlock(Graphics g, BlockPoint bp, MouseState ms) {
		updateBlock(g, new Clip(bp.left(), bp.top(), bp.left() + BlockPanel.WIDTH, bp.top() + BlockPanel.HEIGHT), bp, ms);
	}
	
	public void updateBlock(Graphics g, Clip clip, BlockPoint bp, MouseState ms) {
		g.drawImage(screen, clip.left, clip.top, clip.right, clip.bottom,
				clip.left, clip.top, clip.right, clip.bottom, null);

		updateMouse(g, bp, ms);
	}
	
	public void drawBlock(BlockPoint bp, MouseState ms, boolean update) {
		//CableBattle.stage.draw(g, bp, ms, this);
		
		MapBlock mb = CableBattle.stage.getMapBlock(bp);
		mb.block.draw(gback, bp, ms, this);

		//graphics.drawImage(screenback, 0, 0, null);
		Clip c = mb.mapprops.getActiveArea(bp);
		graphics.drawImage(screenback,
				c.left, c.top, c.right, c.bottom,
				c.left, c.top, c.right, c.bottom,
				null);

		if (!mb.mapprops.range.single()) {
			for (int x2 = bp.x; x2 < bp.x + mb.mapprops.range.w; x2++) {
				for (int y2 = bp.y; y2 < bp.y + mb.mapprops.range.h; y2++) {
					BlockPoint bp2 = new BlockPoint(x2, y2);
					if (bp2.available() && Stage.in(bp2) && !bp2.equals(bp)) {
						MapBlock mb2 = CableBattle.stage.getMapBlock(bp2);
						switch (mb2.getProps().type) {
						case NONE:
						case PIG:
							break;
						default:
							mb2.mapprops.draw(graphics, bp2, MouseState.LEAVE, null);
							break;
						}
					}
				}
			}
		}
		
		mb.mapprops.draw(graphics, bp, ms, this);
		
		if (update) {
			Graphics g = this.getGraphics();
			updateBlock(g, c, bp, ms);
		}
	}
	
	public void startAnimation() {
		stopAnimation();
		animation = new StagePanelAnimation(this);
		animation.start();
	}
	
	public void stopAnimation() {
		if (animation != null) {
			animation.stopAnimation();
			animation = null;
		}
	}
	
	public void refresh() {
		startAnimation();
	}
	
	public BlockPoint hitTest(int x, int y) {
		BlockPoint bp = new BlockPoint();
		
		if (x < WIDTH && x >= 0) {
			bp.setX(x / BlockPanel.WIDTH);
		}
		
		if (y < HEIGHT && y >= 0) {
			bp.setY(y / BlockPanel.HEIGHT);
		}
		
		return bp;
	}
	
	public void mouseClicked(BlockPoint bp) {
		if (CableBattle.stage.setProps(bp, CableBattle.propsBar.getSelectType())) {
			CableBattle.getSound().playEffect(SoundEffect.POP);
			
			//win
			if (CableBattle.stage.checkState()) {
				CableBattle.stage.win();
			}
		} else {
			CableBattle.getSound().playEffect(SoundEffect.DISABLED);
		}
	}
	
	public void mouseMoved(int x, int y, MouseState ms) {
		BlockPoint bp = hitTest(x, y);
		
		if (bp.equals(bpLast)) {
			return;
		}

		if (bpLast.available()) {
			drawBlock(bpLast, MouseState.LEAVE, true);
		}
		if (bp.available()) {
			if (ms == MouseState.CLICK) {
				mouseClicked(bp);
			}
			drawBlock(bp, ms, true);
		}
		bpLast = bp;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseMoved(e.getX(), e.getY(), MouseState.HOVER);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e.getX(), e.getY(), MouseState.CLICK);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// if use mouseClicked the first will not be set when dragging
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (bpLast.available()) {
			drawBlock(bpLast, MouseState.LEAVE, true);
			bpLast.setUnavailable();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		BlockPoint bp = hitTest(e.getX(), e.getY());
		if (bp.available()) {
			mouseClicked(bp);
			drawBlock(bp, MouseState.CLICK, true);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
