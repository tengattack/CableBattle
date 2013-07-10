import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

import util.MouseState;

class History {
	
	BlockPoint point;
	PropsType oldtype, newtype;
	
	public History(BlockPoint point, PropsType oldtype, PropsType newtype) {
		this.point = point;
		this.oldtype = oldtype;
		this.newtype = newtype;
	}
}

public class Stage {
	
	static final int BLOCK_WIDTH = 32, BLOCK_HEIGHT = 18;
	static final int[] ANTI = {0, 3, 4, 1, 2};
	
	MapBlock[][] mapblocks;
	ArrayList<History> historyList = new ArrayList<History>();
	
	private int level = 0;
	
	public Stage() {
	}
	
	public static boolean in(BlockPoint bp) {
		return (bp.x >= 0 && bp.y >= 0 && bp.x < BLOCK_WIDTH && bp.y < BLOCK_HEIGHT);
	}
	
	public boolean setLevel(int level) {
		if (load(level, 0)) {
			this.level = level;
			return true;
		} else {
			this.level = 0;
		}
		return false;
	}
	
	public void clear() {
		historyList.clear();
		mapblocks = new MapBlock[BLOCK_WIDTH][BLOCK_HEIGHT];
		
		System.gc();
	}
	
	public boolean load(int level, int isave) {
		clear();
		
		boolean fromSave = false;
		if (level == 0) {
			fromSave = true;
			SaveInfo si = new SaveInfo();
			if (!CableBattle.cb.sd.loadGame(isave, si, historyList)) {
				CableBattle.mainWindow.levelMessagePanel.setMessage("加载失败", "无法加载该存档");
				return false;
			}
			
			level = si.level;
		}
		
		LevelInfo li = CableBattle.getLevel().get(level);
		if (li.available()) {

			CableBattle.propsBar.setList(li.getItemList());
			li.setMapBlockTable(mapblocks);
			
			if (fromSave) {
				this.level = level;
				for (int i = 0; i < historyList.size(); i++) {
					if (!setProps(historyList.get(i).point, historyList.get(i).newtype, false)) {
						CableBattle.mainWindow.levelMessagePanel.setMessage("加载失败", "存档不匹配");
						return false;
					}
				}
			}

			if (li.getBgm() != null && !li.getBgm().isEmpty()) {
				CableBattle.getSound().playBgm(li.getBgm());
			} else {
				CableBattle.getSound().stopBgm();
			}

			CableBattle.mainWindow.levelMessagePanel.setMessage(level, li.getName(), li.getDescription());
			CableBattle.mainWindow.levelTitlePanel.setLevelInfo(level, li.getName(), li.getDescription());
			CableBattle.mainWindow.stagePanel.refresh();
			CableBattle.mainWindow.propsBarPanel.refresh();
			CableBattle.mainWindow.propsBarPanel.controls.refresh();
			
			return true;
		} else {
			CableBattle.mainWindow.levelMessagePanel.setMessage("加载失败", li.getDescription());
			return false;
		}
	}
	
	public boolean save(int index) {
		return CableBattle.cb.sd.saveGame(index, level, historyList);
	}
	
	public Block getBlock(BlockPoint point) {
		return mapblocks[point.x][point.y].block;
	}
	
	public MapBlock getMapBlock(int x, int y) {
		return mapblocks[x][y];
	}
	
	public MapBlock getMapBlock(BlockPoint point) {
		return getMapBlock(point.x, point.y);
	}

	public Props getProps(BlockPoint point) {
		return mapblocks[point.x][point.y].getProps();
	}
	
	public int getLevel() {
		return level;
	}
	
	public void checkPropsState(BlockPoint bp, boolean center) {
		MapBlock mb = getMapBlock(bp);
		PropsType type = mb.getProps().type;

		if (type == PropsType.CABLE || type == PropsType.WIRELESS || (type == PropsType.NONE && center)) {
			BlockPoint tbp[] = {
				new BlockPoint(bp.x - 1, bp.y),
				new BlockPoint(bp.x, bp.y - 1),
				new BlockPoint(bp.x + 1, bp.y),
				new BlockPoint(bp.x, bp.y + 1)
			};

			Props p_;
			int mask = 0;
			//Graphics g = CableBattle.mainWindow.stagePanel.getGraphics();
			for (int i = 0; i < tbp.length; i++) {
				if (tbp[i].available() && Stage.in(tbp[i])) {
					p_ = CableBattle.stage.getProps(tbp[i]);
					if (type == PropsType.NONE) {
						checkPropsState(tbp[i], false);
						CableBattle.mainWindow.stagePanel.drawBlock(tbp[i], MouseState.LEAVE, true);
					} else if (type.canConnect(p_.type)) {
						mask |= (1 << i);
						if (center && p_.type == PropsType.CABLE) {
							checkPropsState(tbp[i], false);
							CableBattle.mainWindow.stagePanel.drawBlock(tbp[i], MouseState.LEAVE, true);
						}
					}
				}
			}
			mb.mapprops.setState(mask);
		}
	}
	
	public boolean setProps(BlockPoint bp, PropsType type) {
		return setProps(bp, type, true);
	}
	
	public boolean isInPigRange(BlockPoint bp) {
		MapBlock mb;
		for (int y = 0; y < StagePanel.BLOCK_HEIGHT; y++) {
			for (int x = 0; x < StagePanel.BLOCK_WIDTH; x++) {
				mb = getMapBlock(x, y);
				if (mb.getProps().type == PropsType.PIG) {
					BlockPoint pigbp = mb.mapprops.getCurrentPoint(new BlockPoint(x, y));	
					BlockPoint meatbp = new BlockPoint();
					if (CableBattle.stage.have(pigbp, PropsType.MEAT, meatbp)) {
						if (bp.equals(meatbp)) {
							return true;
						}
						if (bp.x >= pigbp.x - 1 && bp.x <= pigbp.x + 1 &&
								bp.y >= pigbp.y - 1 && bp.y <= pigbp.y + 1) {
							return true;
						}
					} else if (mb.mapprops.inAttackRange(bp, new BlockPoint(x, y))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean setProps(BlockPoint bp, PropsType type, boolean history) {
		MapBlock mb = getMapBlock(bp);
		
		Props p = mb.getProps();
		Block b = mb.block;

		if (b.getType() != BlockType.FLOOR) {
			return false;
		}
		if (p.getType() == type) {
			return true;
		}
		if (type == PropsType.CABLE) {
			if (isInPigRange(bp)) {
				return false;
			}
		}
		if (p.allow()) {
			if (CableBattle.propsBar.use(type)) {
				CableBattle.propsBar.recyle(p.getType());
				
				if (history) {
					historyList.add(new History(bp, p.getType(), type));
				}

				p.setType(type);
				checkPropsState(bp, true);
				return true;
			}
		}
		return false;
	}
	
	public boolean checkState() {
		LevelInfo li = CableBattle.cb.resource.level.get(level);
		if (li.starterPoint == null) {
			return false;
		}

		ArrayList<BlockPoint> path = new ArrayList<BlockPoint>();
		if (nextCable(li.starterPoint, 0, path)) {
			return true;
		} else {
			path.clear();
			return nextWireless(li.starterPoint, path);
		}
	}
	
	public boolean nextWireless(BlockPoint bp, ArrayList<BlockPoint> path) {
		path.add(bp);
		for (int y = bp.y - PropsType.WIRELESS_AROUND; y <= bp.y + PropsType.WIRELESS_AROUND; y++) {
			for (int x = bp.x - PropsType.WIRELESS_AROUND; x <= bp.x + PropsType.WIRELESS_AROUND; x++) {
				BlockPoint tbp = new BlockPoint(x, y);
				if (tbp.available() && in(tbp) && !tbp.equals(bp)) {
					switch (getProps(tbp).type) {
					case WIRELESS:
						{
							boolean exist = false;
							for (int i = 0; i < path.size(); i++) {
								if (path.get(i).equals(tbp)) {
									exist = true;
									break;
								}
							}
							if (!exist) {
								if (nextCable(tbp, 0, path)) {
									return true;
								}
								if (nextWireless(tbp, path)) {
									return true;
								}
							}
						}
						break;
					case ENDER:
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean nextCable(BlockPoint bp, int direction, ArrayList<BlockPoint> path) {
		BlockPoint tbp[] = {
			new BlockPoint(bp.x - 1, bp.y),
			new BlockPoint(bp.x, bp.y - 1),
			new BlockPoint(bp.x + 1, bp.y),
			new BlockPoint(bp.x, bp.y + 1)
		};

		if (direction != 0) {
			for (int i = 0; i < path.size(); i++) {
				if (path.get(i).equals(bp)) {
					return false;
				}
			}
			if (getProps(bp).type == PropsType.ENDER) {
				return true;
			}
		}
		path.add(bp);

		for (int i = 0; i < 4; i++) {
			if (direction != 0) {
				if (i + 1 == ANTI[direction]) {
					continue;
				}
			}
			if (tbp[i].available() && Stage.in(tbp[i])) {
				switch (getProps(tbp[i]).type) {
				case CABLE:
					if (nextCable(tbp[i], i + 1, path)) {
						return true;
					}
					break;
				case WIRELESS:
					if (nextWireless(tbp[i], path)) {
						return true;
					}
					break;
				case ENDER:
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean have(BlockPoint bp, PropsType type) {
		return have(bp, type, null);
	}
	
	public boolean have(BlockPoint bp, PropsType type, BlockPoint find) {
		BlockPoint tbp[] = {
			new BlockPoint(bp.x - 1, bp.y),
			new BlockPoint(bp.x, bp.y - 1),
			new BlockPoint(bp.x + 1, bp.y),
			new BlockPoint(bp.x, bp.y + 1)
		};
		
		for (int i = 0; i < 4; i++) {
			if (tbp[i].available() && Stage.in(tbp[i])) {
				if (getProps(tbp[i]).type == type) {
					if (find != null) {
						find.set(tbp[i].x, tbp[i].y);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean undo() {
		if (historyList.size() > 0) {
			int lastIndex = historyList.size() - 1;

			History h = historyList.get(lastIndex);
			historyList.remove(lastIndex);
			
			CableBattle.propsBar.use(h.oldtype);
			CableBattle.propsBar.recyle(h.newtype);

			getProps(h.point).setType(h.oldtype);
			checkPropsState(h.point, true);
			
			CableBattle.mainWindow.stagePanel.drawBlock(h.point, MouseState.LEAVE, true);
			return true;
		}
		return false;
	}
	
	public void win() {
		CableBattle.mainWindow.stagePanel.stopAnimation();

		if (CableBattle.cb.sd.unlockLevel <= level && level < CableBattle.cb.resource.level.count) {
			CableBattle.cb.sd.unlockLevel = level + 1;
		}
		
		LevelInfo li = CableBattle.cb.resource.level.get(level);
		
		String wintitle;
		if (level >= CableBattle.cb.resource.level.count) {
			wintitle = "全部通关";
		} else {
			wintitle = "成功";
		}
		CableBattle.mainWindow.levelMessagePanel.setMessage(wintitle, li.winmessage);

		CableBattle.mainWindow.showWin(level);
	}
}
