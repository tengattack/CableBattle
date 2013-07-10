
import java.awt.*;

import javax.swing.JPanel;

import util.MouseState;

enum BlockType {
	
	FLOOR(0), WALL(1);
	
	private final int type;
	public static final int COUNT = WALL.type + 1;
	
	BlockType(int type) {
        this.type = type;
    }
	
	public String getKeyName() {
		switch (type) {
		case 0:
			return "floor";
		case 1:
			return "wall";
		default:
			return "unknow";
		}
	}
	
	public int getIndex() {
		return type;
	}
}

class BlockPanel {
	static final int WIDTH = 36;
	static final int HEIGHT = 36;
}

class BlockPoint {
	int x, y;
	
	public BlockPoint(int x, int y) {
		set(x, y);
	}
	
	public BlockPoint() {
		setUnavailable();
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public boolean available() {
		return (x != -1 && y != -1);
	}
	
	public void setUnavailable() {
		x = -1;
		y = -1;
	}
	
	public int left() {
		return (this.x * BlockPanel.WIDTH);
	}
	
	public int top() {
		return (this.y * BlockPanel.HEIGHT);
	}
	
	public boolean equals(BlockPoint bp) {
		if (bp.available() && this.available()) {
			return (bp.x == this.x && bp.y == this.y);
		} else if (!bp.available() && !this.available()) {
			return true;
		}
		return false;
    }
}

public class Block {
	BlockType type;
	
	public Block(BlockType type) {
		setType(type);
	}
	
	public BlockType getType() {
		return type;
	}
	
	public void setType(BlockType type) {
		this.type = type;
	}
	
	public void draw(Graphics g, BlockPoint bp, MouseState me, JPanel panel) {
		Image img = CableBattle.cb.resource.getBlockImage(type);
		g.drawImage(img, 
				bp.left(), bp.top(), BlockPanel.WIDTH, BlockPanel.HEIGHT, null);
	}
}
