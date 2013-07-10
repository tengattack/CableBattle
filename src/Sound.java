
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import util.OggSoundPlay;
import util.SoundData;
import util.SoundPlay;

enum SoundEffect {
	POP(0), USE(1), MENU(2), ITEM(3), DISABLED(4), WIN(5);
	
	private final int type;
	public static final String[] name = {"pop", "use", "menu", "item", "disabled", "win"};
	
	SoundEffect(int type) {
        this.type = type;
    }
	
	public String getName() {
		return name[type];
	}
}

class MakeSoundThread extends Thread {

	private SoundPlay sp;
	
	private Sound sound;
	private String keyName;
	
    private boolean stopped = true;
    private boolean isogg = false;
    private boolean loop = false;
    private boolean cached = false;
    
	public MakeSoundThread(boolean isogg, boolean loop, boolean cached) {
		this.loop = loop;
		this.cached = cached;
		this.isogg = isogg;
		if (isogg) {
			sp = new OggSoundPlay();
		} else {
			sp = new SoundPlay();
		}
    }
	
	public void setSoundData(SoundData sd) {
		sp.setSoundData(sd);
	}
	
	public void setSoundLoad(Sound sound, String keyName) {
		this.sound = sound;
		this.keyName = keyName;
	}
	
	public String getExtName() {
		if (isogg) {
			return "ogg";
		} else {
			return "wav";
		}
	}
	
	public String getFileName() {
		return sound.path + keyName + "." + getExtName();
	}
	
	public void run() {
		
		if (loop) {
			stopped = false;
		} else {
			stopped = true;
		}
		
		if (cached) {
			if (sp.getSoundData() == null) {
				if (sound == null) {
					return;
				}
				
				SoundData sd;
				if (sound.soundTable.containsKey(keyName)) {
					sd = sound.soundTable.get(keyName);
				} else {
					sd = SoundPlay.getSoundData(getFileName());
					if (sd == null) {
						return;
					}
					sound.soundTable.put(keyName, sd);
				}
				sp.setSoundData(sd);
			}
			
			do {
				if (!sp.play()) {
					break;
				}
			} while (!stopped);
		} else {
			do {
				if (!sp.play(getFileName())) {
					break;
				}
			} while (!stopped);
		}
	}
	
	public void stopSound() {
		stopped = true;
		sp.stop();
		/*try {
			wait();
		} catch (Exception e) {
		}*/
	}
}

public class Sound {

	String path;
	
	MakeSoundThread bgm = null;
	ArrayList<MakeSoundThread> effects = new ArrayList<MakeSoundThread>();
	
	Hashtable<String, SoundData> soundTable;
	
	SoundEffect lastEffect = null;
	String lastBgmName = "";
	
	public Sound(String path) {
		this.path = path;
		soundTable = new Hashtable<String, SoundData>();
	}
	
	public void removeFinishedEffect() {
		for (int i = 0; i < effects.size(); i++) {
			if (!effects.get(i).isAlive()) {
				effects.remove(i);
				i--;
			}
		}
	}
	
	public MakeSoundThread loadSound(String keyname, boolean isogg, boolean loop, boolean cached) {
		
		MakeSoundThread t = new MakeSoundThread(isogg, loop, cached);
		
		if (cached && soundTable.containsKey(keyname)) {
			t.setSoundData(soundTable.get(keyname));
		} else {
			//System.out.println("loadSound " + keyname);
			t.setSoundLoad(this, keyname);
		}
		
		return t;
	}
	
	public void playEffect(SoundEffect e) {
		/*if (lastEffect == e && effect.isAlive()) {
			return;
		}*/
		removeFinishedEffect();
		//stopEffect();
		
		MakeSoundThread effect = loadSound("effect/" + e.getName(), false, false, true);
		if (effect != null) {

			lastEffect = e;
			effect.start();
			
			effects.add(effect);
		}
	}
	
	public void stopEffect() {
		removeFinishedEffect();
		for (int i = 0; i < effects.size(); i++) {
			effects.get(i).stopSound();
		}
	}
	
	public void playBgm(String name) {
		if (lastBgmName == name) {
			return;
		}
		
		stopBgm();
		
		bgm = loadSound("bgm/" + name, true, true, false);
		if (bgm != null) {
			lastBgmName = name;
			bgm.start();
		}
	}

	public void stopBgm() {
		if (bgm != null) {
			bgm.stopSound();
			bgm = null;
			lastBgmName = "";
		}
	}
	
	public void stopBgm(String name) {
		if (lastBgmName == name) {
			stopBgm();
		}
	}
	
	public void stop() {
		stopBgm();
		stopEffect();
	}
}
