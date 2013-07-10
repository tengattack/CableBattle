
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

class SaveInfo {
	int index;
	long time;
	int level;
	int historyCount;
	
	public SaveInfo() {
		set(-1, -1, -1, -1);
	}

	public SaveInfo(int index, long time, int level, int historyCount) {
		set(index, time, level, historyCount);
	}
	
	public void set(int index, long time, int level, int historyCount) {
		this.index = index;
		this.time = time;
		this.level = level;
		this.historyCount = historyCount;
	}
}

public class SaveData {
	
	String path;
	
	int unlockLevel = 0, jsonUnlockLevel = -1;
	boolean saved = false, jsonSaved;
	
	static final String KEY_UNLOCK_LEVEL = "unlock_level";
	static final String KEY_SAVED = "saved";
	
	static final String FILE_SYSTEM = "system.json";
	
	static final int SAVE_DATA_COUNT = 8;
	static final byte[] SAVE_DATA_MAGIC = {(byte)'C', (byte)'B', (byte)'S', (byte)' '};
	
	public SaveData(String path) {
		this.path = path;
		
		try {
			File dirFile = new File(path);
	        if (!dirFile.exists()) {
	        	dirFile.mkdirs();
	        }
		} catch (Exception e) {
	    	System.out.println(e.getMessage());
	    }
		
		load();
	}
	
	public boolean change() {
		return (unlockLevel != jsonUnlockLevel || saved != jsonSaved);
	}
	
	public boolean loadState(int index) {
		return true;
	}
	
	public void load() {
		
		JSONObject json = null;
		try {
			FileInputStream fi = new FileInputStream(path + FILE_SYSTEM);
			BufferedInputStream fb = new BufferedInputStream(fi);
			json = new JSONObject(new JSONTokener(fb));
		} catch (Exception e) {
	    	System.out.println(e.getMessage());
	    }
		if (json != null) {
			try {
				jsonUnlockLevel = json.getInt(KEY_UNLOCK_LEVEL);
			} catch (Exception e) {
		    }
			try {
				jsonSaved = json.getBoolean(KEY_SAVED);
			} catch (Exception e) {
		    }
			unlockLevel = jsonUnlockLevel;
			saved = jsonSaved;
		}
		
		if (unlockLevel < 0) {
			unlockLevel = 0;
		} /*else if (unlockLevel > CableBattle.getLevel().count) {
			unlockLevel = CableBattle.getLevel().count;
		}*/
	}
	
	public boolean loadGame(int index, SaveInfo info) {
		return loadGame(index, info, null);
	}
	
	public boolean loadGame(int index, SaveInfo info, ArrayList<History> historyList) {
		try {
			FileInputStream fis = new FileInputStream(path + String.valueOf(index));
			FileChannel fc = fis.getChannel();

			int filesize = (int)fc.size();
			ByteBuffer bb = ByteBuffer.allocate(filesize);
			fc.read(bb);
			
			bb.rewind();
			
			fc.close();
			fis.close();
			
			byte[] magic = new byte[SAVE_DATA_MAGIC.length];
			bb.get(magic);
			
			/*if (!SAVE_DATA_MAGIC.equals(magic)) {
				System.out.println("!equals");
				return false;
			}*/
			for (int i = 0; i < SAVE_DATA_MAGIC.length; i++) {
				if (magic[i] != SAVE_DATA_MAGIC[i]) {
					return false;
				}
			}
			
			long time = bb.getLong();
			int level = bb.get();
			int historyCount = bb.getInt();
			
			int buflength = SAVE_DATA_MAGIC.length + 8 + 1 + 4 + historyCount * 4;
			if (filesize != buflength) {
				return false;
			}
			
			if (historyList != null) {
				for (int i = 0; i < historyCount; i++) {
					int x = bb.get();
					int y = bb.get();
					PropsType oldtype = PropsType.values()[bb.get()];
					PropsType newtype = PropsType.values()[bb.get()];
					historyList.add(new History(new BlockPoint(x, y), oldtype, newtype));
				}
			}
			
			info.set(index, time, level, historyCount);
			
			return true;
		} catch (IOException e) {
	    	return false;
	    } catch (Exception e) {
	    	return false;
	    }
	}
	
	public boolean saveGame(int index, int level, ArrayList<History> historyList) {

		long time = System.currentTimeMillis();
		int historyCount = historyList.size();
		
		int buflength = SAVE_DATA_MAGIC.length + 8 + 1 + 4 + historyCount * 4;
		ByteBuffer bb = ByteBuffer.allocate(buflength);
		bb.put(SAVE_DATA_MAGIC);
		bb.putLong(time);
		bb.put((byte)level);
		bb.putInt(historyCount);
		
		History h;
		for (int i = 0; i < historyCount; i++) {
			h = historyList.get(i);
			bb.put((byte)h.point.x);
			bb.put((byte)h.point.y);
			bb.put((byte)h.oldtype.getIndex());
			bb.put((byte)h.newtype.getIndex());
		}
		
		bb.flip();
		
		try {
			FileOutputStream fos = new FileOutputStream(path + String.valueOf(index), false);
	        FileChannel channel = fos.getChannel();
	        channel.write(bb);
	        channel.close();
	        fos.close();
	        
	        this.saved = true;
	        
	        return true;
	    } catch (IOException e) {
	    	return false;
	    }
	}
	
	public void save() {
		if (!change()) {
			return;
		}
		//System.out.println("save()");
		
		JSONObject jsonSystem = new JSONObject();
		jsonSystem.put(KEY_UNLOCK_LEVEL, unlockLevel);
		jsonSystem.put(KEY_SAVED, saved);

		try {
			FileWriter fw = new FileWriter(path + FILE_SYSTEM);
			jsonSystem.write(fw);
			fw.close();
		} catch (Exception e) {
	    	System.out.println(e.getMessage());
	    }

	}
}
