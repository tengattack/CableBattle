
import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import util.Clip;
import util.MouseState;

public enum PropsType {
	
	NONE(0), STARTER(1), ENDER(2), PIG(3), CABLE(4), WIRELESS(5), MEAT(6);
	
	private final int type;
	public static final int COUNT = MEAT.type + 1;
	
	public static final int WIRELESS_AROUND = 3;
	
	public static final String[] keyname = {"none", "starter", "ender", "pig", "cable", "wireless", "meat"};
	public static final String[] name = {"消除", "起点", "终点", "猪", "网线", "无线路由", "肉"};
	public static final String[] tips = {
		"移除在舞台上放置的道具",
		"网络端口，作用范围是以自己为中心的十字方格",
		"客户端端口，作用范围是以自己为中心的十字方格",
		"会在一定范围到处走动，只剪断自己周围的绳子，作用范围是以自己为中心的 3x3 的格子",
		"连接网络端口、客户端或无线路由，作用范围是以自己为中心的十字方格",
		"可以跨越猪或者墙的障碍传递网络，猪不能弄坏路由器，作用范围是以自己为中心的 7x7 的格子，并且下一个路由器必须在上一个路由器的作用范围才能使网络连通",
		"可以让猪停止在它的周围，但不影响猪剪断自己周围的网线，作用范围是以自己为中心的十字方格"
	};

	PropsType(int type) {
        this.type = type;
    }
	
	public String getTypeTips() {
		return tips[type];
	}
	
	public String getName() {
		return name[type];
	}

	public String getKeyName() {
		return keyname[type];
	}
	
	public int getIndex() {
		return type;
	}
	
	public boolean canConnect(PropsType t) {
		if (type == CABLE.type) {
			return (t.type == STARTER.type || t.type == ENDER.type || t.type == WIRELESS.type || t.type == CABLE.type);
		} else if (type == WIRELESS.type) {
			return (t.type == CABLE.type);
		} else {
			return false;
		}
	}
}

class CableClip extends Clip {
	
	static final int BWIDTH = 64, BHEIGHT = 64;
	
	public CableClip(int state) {
		int x = state % 4;
		int y = state / 4;
	
		this.left = x * BWIDTH;
		this.top = y * BHEIGHT;
		this.right = this.left + BWIDTH;
		this.bottom = this.top + BHEIGHT;
	}
}

class PigClip extends Clip {
	
	static final int BWIDTH = 64, BHEIGHT = 64;
	
	static final int WALK_STEP = StagePanelAnimation.WALK_STEP;
	static final int WALK_STEP_COUNT = 3;
	
	int dleft, dtop;
	//static BlockPoint focusbp;
	
	public PigClip(int state, BlockPoint bp, int w, int h) {
		
		/*if (focusbp == null) {
			focusbp = new BlockPoint(bp.x, bp.y);
		}*/
		
		int dw = w - 1, dh = h - 1;
		int c = (dw + dh) * 2;
		
		int iblock = state / WALK_STEP;
		int step = state % WALK_STEP;
		
		//System.out.printf("(%d, %d) ~ (%d, %d)\n", bp.x, bp.y, iblock, step);
		this.dleft = bp.left();
		this.dtop = bp.top();
		
		this.left = (state % WALK_STEP_COUNT) * BWIDTH;
		if (iblock < dw) {
			this.top = 0;
			this.dleft += BlockPanel.WIDTH * ((float)step / WALK_STEP + iblock);
		} else if (iblock < (dw + dh)) {
			this.top = BHEIGHT;
			this.dleft += BlockPanel.WIDTH * dw;
			this.dtop += BlockPanel.HEIGHT * ((float)step / WALK_STEP + iblock - dw);
		} else if (iblock < (dw + dh + dw)) {
			this.top = BHEIGHT * 2;
			this.dtop += BlockPanel.HEIGHT * dh;
			this.dleft += BlockPanel.WIDTH * ((dw - (float)step / WALK_STEP) - (iblock - (dw + dh)));
		} else {
			this.top = BHEIGHT * 3;
			this.dtop += BlockPanel.HEIGHT * ((dh - (float)step / WALK_STEP) - (iblock - (dw + dh + dw)));
		}
		
		/*if (focusbp.equals(bp)) {
			System.out.printf("(%d, %d), (%d, %d), (%d, %d), (%d, %d)\n", bp.x, bp.y, iblock, step, left, top, dleft, dtop);
		}*/
	
		this.right = this.left + BWIDTH;
		this.bottom = this.top + BHEIGHT;
	}
	
	public PigClip(BlockPoint bp) {
		this.dleft = bp.left();
		this.dtop = bp.top();
		
		this.left = 0;
		this.top = BHEIGHT * 4;
		
		this.right = this.left + BWIDTH;
		this.bottom = this.top + BHEIGHT;
	}
}

class PropsTypeDrawer {

	public static void draw(Graphics g, BlockPoint bp, MouseState ms, MapProps mp, JPanel panel) {
		switch (mp.props.type) {
		case PIG:
			{
				PigClip pigsties = new PigClip(bp);
				PigClip c = new PigClip(mp.state, bp, mp.range.w, mp.range.h);
				Image img = CableBattle.cb.resource.getPropsImage(mp.props.type);
				if (img == null) {
					return;
				}
				//猪圈
				g.drawImage(img,
						pigsties.dleft, pigsties.dtop, pigsties.dleft + BlockPanel.WIDTH, pigsties.dtop + BlockPanel.HEIGHT,
						pigsties.left, pigsties.top, pigsties.right, pigsties.bottom,
						panel);
				//猪
				g.drawImage(img,
						c.dleft, c.dtop, c.dleft + BlockPanel.WIDTH, c.dtop + BlockPanel.HEIGHT,
						c.left, c.top, c.right, c.bottom,
						panel);
			}
			break;
		case CABLE:
			{
				CableClip c = new CableClip(mp.state);
				Image img = CableBattle.cb.resource.getPropsImage(mp.props.type);
				if (img == null) {
					return;
				}
				g.drawImage(img,
						bp.left(), bp.top(), bp.left() + BlockPanel.WIDTH, bp.top() + BlockPanel.HEIGHT,
						c.left, c.top, c.right, c.bottom,
						null);
			}
			break;
		default:
			draw(g, bp.left(), bp.top(), ms, mp.props.type, panel);
			break;
		}
	}
	public static void draw(Graphics g, int left, int top, MouseState ms, PropsType type, JPanel panel) {
		switch (type) {
		case CABLE:
			{
				CableClip c = new CableClip(0);
				Image img = CableBattle.cb.resource.getPropsImage(type);
				if (img == null) {
					return;
				}
				g.drawImage(img,
						left, top, left + BlockPanel.WIDTH, top + BlockPanel.HEIGHT,
						c.left, c.top, c.right, c.bottom,
						null);
			}
			break;
		case STARTER:
		case ENDER:
		case WIRELESS:
		case MEAT:
			{
				Image img = CableBattle.cb.resource.getPropsImage(type);
				if (img == null) {
					return;
				}
				g.drawImage(img,
						left, top, BlockPanel.WIDTH, BlockPanel.HEIGHT, null);
			}
			break;
		default:
			break;
		}
	}
}