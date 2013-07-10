
import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import util.Clip;
import util.MouseState;

class PropsRange {
	int w = 1, h = 1;
	public void setRange(int w, int h) {
		this.w = w;
		this.h = h;
	}
	public boolean single() {
		return (w <= 1 && h <= 1);
	}
}

class MapProps {

	Props props;
	PropsRange range = new PropsRange();
	
	int state = 0;
	
	public MapProps(PropsType propstype) {
		props = new Props(propstype);
	}
	
	public void setRange(int w, int h) {
		this.range.setRange(w, h);
	}
	
	public boolean inRange(BlockPoint test, BlockPoint propsbp) {
		if (test.x >= propsbp.x && test.y >= propsbp.y &&
				test.x <= propsbp.x + range.w - 1 && test.y <= propsbp.y + range.h - 1) {
			return true;
		}
		return false;
	}
	
	public boolean inAttackRange(BlockPoint test, BlockPoint propsbp) {
		/*if (props.type == PropsType.PIG)*/ {
			if (test.x >= propsbp.x - 1 && test.y >= propsbp.y - 1 &&
					test.x <= propsbp.x + range.w && test.y <= propsbp.y + range.h) {
				return true;
			}
		}
		return false;
	}
	
	public BlockPoint getCurrentPoint(BlockPoint bp) {
		if (props.type == PropsType.PIG) {
			int iblock = state / PigClip.WALK_STEP;
			int step = state % PigClip.WALK_STEP;
			
			int dw = range.w - 1, dh = range.h - 1;
			
			int x = bp.x, y = bp.y;
			if (iblock < dw) {
				x += iblock;
			} else if (iblock < (dw + dh)) {
				x += dw;
				y += (iblock - dw);
			} else if (iblock < (dw + dh + dw)) {
				x += dw - (iblock - (dw + dh));
				y += dh;
			} else {
				y += dh - (iblock - (dw + dh + dw));
			}
			return new BlockPoint(x, y);
		} else {
			return new BlockPoint(bp.x, bp.y);
		}
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public MapProps clone() {
		MapProps mp = new MapProps(props.type);
		mp.props.allow(props.allow());
		mp.setState(state);
		mp.setRange(range.w, range.h);
		return mp;
	}
	
	public void draw(Graphics g, BlockPoint bp, MouseState ms, JPanel panel) {
		PropsTypeDrawer.draw(g, bp, ms, this, panel);
	}
	
	public Clip getActiveArea(BlockPoint bp) {
		/*BlockPoint cpbp = getCurrentPoint(bp);
		if (cpbp.x == bp.x && cpbp.y > bp.y) {
			//左边
			return new Clip(cpbp.left(), cpbp.top() - BlockPanel.HEIGHT, cpbp.left() + BlockPanel.WIDTH, cpbp.top() + BlockPanel.HEIGHT);
		} else if (cpbp.y == bp.y && cpbp.x < bp.x + range.w - 1) {
			//上面
			return new Clip(cpbp.left(), cpbp.top(), cpbp.left() + BlockPanel.WIDTH * 2, cpbp.top() + BlockPanel.HEIGHT);
		} else if (cpbp.y == bp.y + range.h - 1) {
			return new Clip(cpbp.left() - BlockPanel.WIDTH, cpbp.top(), cpbp.left() + BlockPanel.WIDTH, cpbp.top() + BlockPanel.HEIGHT);
		} else {
			//右边
			return new Clip(cpbp.left(), cpbp.top(), cpbp.left() + BlockPanel.WIDTH, cpbp.top() + BlockPanel.HEIGHT * 2);
		}*/
		return new Clip(bp.left(), bp.top(), bp.left() + BlockPanel.WIDTH * range.w, bp.top() + BlockPanel.HEIGHT * range.h);
	}
}

public class Props {
	
	PropsType type;
	boolean ballow;

	//static Border blackline = BorderFactory.createLineBorder(Color.black);
	
	public Props(PropsType type) {
		ballow = true;
		setType(type);
	}
	
	public Props(PropsType type, boolean allow) {
		ballow = allow;
		setType(type);
	}
	
	public PropsType getType() {
		return type;
	}
	
	public void setType(PropsType type) {
		this.type = type;
	}
	
	public boolean allow() {
		return ballow;
	}
	
	public void allow(boolean allow) {
		ballow = allow;
	}
}
