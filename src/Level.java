
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.dom4j.*; 
import org.dom4j.io.*;

class LevelInfo {
	
	int level;
	String name, description, winmessage;
	String bgm;
	
	ArrayList<PropsItem> itemList;
	Hashtable<BlockPoint, MapBlock> mapBlockList;
	
	MapBlock baseMapBlock;
	BlockPoint starterPoint;

	public LevelInfo(int level) {
		this.level = level;
		
		mapBlockList = new Hashtable<BlockPoint, MapBlock>();
		baseMapBlock = new MapBlock(BlockType.FLOOR, PropsType.NONE);
	}
	
	public LevelInfo(int level, String name, String description) {
		this.level = level;
		this.name = name;
		this.description = description;
	}
	
	public boolean available() {
		return (this.level > 0);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getBgm() {
		return bgm;
	}
	
	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "";
		}
	}
	
	public void setDescription(String description) {
		if (description != null) {
			this.description = description;
		} else {
			this.description = "";
		}
	}
	
	public void setBgm(String bgm) {
		this.bgm = bgm;
	}
	
	public void setWinMessage(String winmessage) {
		this.winmessage = winmessage;
	}
	
	public void addItem(PropsType type, int amount) {
		if (itemList == null) {
			itemList = new ArrayList<PropsItem>();
		}
		itemList.add(new PropsItem(type, (amount <= 0) ? -1 : amount));
	}
	
	public void addMapBlock(BlockPoint bp, MapBlock block) {
		if (block.mapprops.props.type == PropsType.STARTER) {
			starterPoint = bp;
		}
		mapBlockList.put(bp, block);
	}
	
	public void setMapBaseBlock(MapBlock block) {
		if (block != null) {
			baseMapBlock = block;
		}
	}
	
	public ArrayList<PropsItem> getItemList() {
		return itemList;
	}
	
	public void setMapBlockTable(MapBlock[][] mapblocks) {
		for (int y = 0; y < Stage.BLOCK_HEIGHT; y++) {
			for (int x = 0; x < Stage.BLOCK_WIDTH; x++) {
				mapblocks[x][y] = baseMapBlock.clone();
			}
		}
		
		Set<Entry<BlockPoint, MapBlock>> set = mapBlockList.entrySet();
		Entry<BlockPoint, MapBlock> entry;
		BlockPoint foobp;
		for (Iterator i = set.iterator(); i.hasNext(); ) {
			entry = (Entry<BlockPoint, MapBlock>)i.next();
			foobp = entry.getKey();
			
			MapBlock mb = entry.getValue();
			mb.setSystemProps(true);
			mapblocks[foobp.x][foobp.y] = mb.clone();
		}
	}
}

public class Level {
	
	int count = 0;
	String path;
	
	LevelInfo[] levelInfoList;
	
	public Level(String path) {
		
		this.path = path;
		
		try {
			FileInputStream fi = new FileInputStream(path + "config.json");
			BufferedInputStream fb = new BufferedInputStream(fi);
			JSONObject json = new JSONObject(new JSONTokener(fb));
			
			count = json.getInt("count");
			levelInfoList = new LevelInfo[count];
			
	    } catch (Exception e) {
	    	System.out.println(e.getMessage());
	    }
	}
	
	public LevelInfo get(int level) {
		if (level <= 0 || level > count) {
			return null;
		}
		if (levelInfoList[level - 1] == null) {
			try {
				read(level);
			} catch (Exception e) {
				/*StackTraceElement[] ste = e.getStackTrace();
				String str = "";
				for (int i = 0; i < ste.length; i++) {
					str += String.format("(%d) %s %s\r\n", ste[i].getLineNumber(), ste[i].getMethodName(), ste[i].getClassName());
				}*/
				return new LevelInfo(-1, "", e.getMessage());
			}
		}
		return levelInfoList[level - 1];
	}
	
	public MapProps readMapProps(Element props, PropsType defaulttype) {

		PropsType type = defaulttype;
		
		String strType = props.elementText("type");
		try {
			if (strType != null) {
				int itype = Integer.parseInt(strType);
				if (itype >= 0 && itype < PropsType.COUNT) {
					type = PropsType.values()[itype];
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			return null;
		}
		
		MapProps mprops = new MapProps(type);
		String strRange = props.elementText("range");
		try {
			if (strRange != null) {
				String[] strranges = strRange.split("x");
				if (strranges.length == 2) {
					int w = Integer.parseInt(strranges[0]);
					int h = Integer.parseInt(strranges[1]);
					mprops.setRange(w, h);
				}
			}
		} catch (Exception e) {
		}
	
		return mprops;
	}
	
	public MapBlock readMapBlock(Element block, boolean base, MapBlock baseblock) {

		BlockType blocktype;
		PropsType propstype;
		
		if (base || baseblock == null) {
			blocktype = BlockType.FLOOR;
			propstype = PropsType.NONE;
		} else {
			blocktype = baseblock.block.type;
			propstype = baseblock.getProps().type;
		}
		
		try {
			String strType = block.elementText("type");
		
			if (strType != null) {
				int iblocktype = Integer.parseInt(strType);
				if (iblocktype >= 0 && iblocktype < BlockType.COUNT) {
					blocktype = BlockType.values()[iblocktype];
				}
			}
		} catch (Exception e) {
		}
		
		try {
			Element eprops = block.element("props");
		
			if (eprops != null) {
				MapProps mapprops = readMapProps(eprops, propstype);
				if (mapprops != null) {
					return new MapBlock(blocktype, mapprops);
				}
			}
		} catch (Exception e) {
		}

		return new MapBlock(blocktype, propstype);
	}
	
	public boolean read(int level) throws Exception {
		if (level <= 0 || level > count) {
			return false;
		}
		
    	File xmlFile = new File(path + String.format("%d.xml", level));
    	FileInputStream fis = new FileInputStream(xmlFile);  

		SAXReader reader = new SAXReader();
		Document doc = reader.read(fis);
		
		fis.close();
		
		Element root = doc.getRootElement();
		
		Element base = root.element("base");
		Element map = root.element("map");
		Element item = root.element("item");

		LevelInfo li = new LevelInfo(level);
		li.setName(base.elementText("name"));
		li.setDescription(base.elementText("description"));
		li.setWinMessage(base.elementText("winmessage"));
		li.setBgm(base.elementText("bgm"));
		
		Element baseBlock = map.element("baseblock");
		MapBlock mbbase = readMapBlock(baseBlock, true, null);
		li.setMapBaseBlock(mbbase);
		
		Element foo;
		for (Iterator i = map.elementIterator("block"); i.hasNext(); ) {
			foo = (Element)i.next();
			String strX = foo.elementText("x");
			String strY = foo.elementText("y");
			if (strX != null && strY != null) {
				int x = Integer.parseInt(strX);
				int y = Integer.parseInt(strY);
				
				BlockPoint bp = new BlockPoint(x, y);
				if (Stage.in(bp)) {
					MapBlock mb = readMapBlock(foo, false, mbbase);
					if (mb != null) {
						li.addMapBlock(bp, mb);
					}
				}
			}
		}

		// always have none
		li.addItem(PropsType.NONE, -1);
		for (Iterator i = item.elementIterator("props"); i.hasNext(); ) {
			foo = (Element)i.next();
			
			String strType = foo.elementText("type");
			String strAmount = foo.elementText("amount");

			if (strType != null && strAmount != null) {
				int type = Integer.parseInt(strType);
				int amount = Integer.parseInt(strAmount);
				if (type > PropsType.NONE.getIndex() && type < PropsType.COUNT) {
					li.addItem(PropsType.values()[type], amount);
				}
			}
		}
		
		levelInfoList[level - 1] = li;
		return true;
	}

}
