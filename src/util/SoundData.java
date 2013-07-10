package util;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundData {

	private byte[] buffer;
	
	public SoundData(byte[] buffer) {
		this.buffer = buffer;
	}
	
	public byte[] getBuffer() {
		return buffer;
	}
}
