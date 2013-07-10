package util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundPlay {
	
    static final int BUFFER_SIZE = 128000;

    private SoundData sd;
	protected SourceDataLine sourceLine;

	public SoundPlay() {
	}
	
	public SoundPlay(SoundData sd) {
		setSoundData(sd);
	}
	
	public void setSoundData(SoundData sd) {
		this.sd = sd;
	}
	
	public SoundData getSoundData() {
		return sd;
	}
	
	public static SoundData getSoundData(String filename) {
    	String strFilename = filename;

    	File soundFile;

        try {
            soundFile = new File(strFilename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
        	FileInputStream fis = new FileInputStream(soundFile);
        	FileChannel fc = fis.getChannel();

        	byte[] buffer = new byte[(int)fc.size()];
        	fis.read(buffer);

        	return new SoundData(buffer);
        	
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
	
	public boolean play(String filename) {
		String strFilename = filename;

    	File soundFile;
    	AudioInputStream audioStream;
    	
    	FileInputStream fis = null;
		BufferedInputStream bis = null;
    	
        try {
            soundFile = new File(strFilename);
            
            fis = new FileInputStream(soundFile);      
			bis = new BufferedInputStream(fis);
    		
            return play(bis);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

	}
	
	public boolean play(InputStream is) {
		AudioInputStream audioStream;
		try {
			audioStream = AudioSystem.getAudioInputStream(is);
			return play(audioStream);
		} catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}

	public boolean play(AudioInputStream audioStream) {

		AudioFormat audioFormat;
		
		audioFormat = audioStream.getFormat();
		
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            sourceLine = (SourceDataLine)AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        
        sourceLine.start();
        
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
            	@SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }
        
        try {
        	audioStream.close();
        } catch (Exception e) {
        }
        
        sourceLine.drain();
        sourceLine.close();
            
        sourceLine = null;
            
        return true;
	}

	public boolean play() {
		if (sd == null) {
			return false;
		}

		byte[] buffer = sd.getBuffer();

        try {
        	ByteArrayInputStream bais = new ByteArrayInputStream(buffer);      	
            return play(bais);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
	}
	
	public void stop() {
		if (sourceLine != null) {
			sourceLine.stop();
		}
	}
}
