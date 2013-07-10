package util;

import java.io.InputStream;

import com.jcraft.jorbis.*;
import com.jcraft.jogg.*;

import javax.sound.sampled.*;

public class OggSoundPlay extends SoundPlay {
	
	static final int BUFSIZE=4096*2;
	static int convsize=BUFSIZE*2;
	static byte[] convbuffer=new byte[convsize];

	private int RETRY=3;
	int retry=RETRY;
	
	byte[] buffer;
  
	SyncState oy;
	StreamState os;
	Page og;
	Packet op;
	Info vi;
	Comment vc;
	DspState vd;
	Block vb;
	
	int bytes=0;

	int format;
	int rate=0;
	int channels=0;
	int left_vol_scale=100;
	int right_vol_scale=100;
	String current_source=null;

	int frameSizeInBytes;
	int bufferLengthInBytes;
	
	InputStream bitStream=null;
	
	boolean stopped;
	
	void init_jorbis(){
		oy=new SyncState();
		os=new StreamState();
		og=new Page();
		op=new Packet();
		
		vi=new Info();
		vc=new Comment();
		vd=new DspState();
		vb=new Block(vd);
		
		buffer=null;
		bytes=0;
		stopped = false;
		
		oy.init();
	}
	
	SourceDataLine getsourceLine(int channels, int rate) {
	    if(sourceLine==null||this.rate!=rate||this.channels!=channels){
	      if(sourceLine!=null){
	        sourceLine.drain();
	        sourceLine.stop();
	        sourceLine.close();
	      }
	      init_audio(channels, rate);
	      sourceLine.start();
	    }
	    return sourceLine;
	}
	
	void init_audio(int channels, int rate) {
	    try{
	      //ClassLoader originalClassLoader=null;
	      //try{
	      //  originalClassLoader=Thread.currentThread().getContextClassLoader();
	      //  Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
	      //}
	      //catch(Exception ee){
	      //  System.out.println(ee);
	      //}
	      AudioFormat audioFormat=new AudioFormat((float)rate, 16, channels, true, // PCM_Signed
	          false // littleEndian
	      );
	      DataLine.Info info=new DataLine.Info(SourceDataLine.class, audioFormat,
	          AudioSystem.NOT_SPECIFIED);
	      if(!AudioSystem.isLineSupported(info)){
	        //System.out.println("Line " + info + " not supported.");
	        return;
	      }

	      try{
	        sourceLine=(SourceDataLine)AudioSystem.getLine(info);
	        //sourceLine.addLineListener(this);
	        sourceLine.open(audioFormat);
	      }
	      catch(LineUnavailableException ex){
	        System.out.println("Unable to open the sourceDataLine: "+ex);
	        return;
	      }
	      catch(IllegalArgumentException ex){
	        System.out.println("Illegal Argument: "+ex);
	        return;
	      }

	      frameSizeInBytes=audioFormat.getFrameSize();
	      int bufferLengthInFrames=sourceLine.getBufferSize()/frameSizeInBytes/2;
	      bufferLengthInBytes=bufferLengthInFrames*frameSizeInBytes;

	      //if(originalClassLoader!=null)
	      //  Thread.currentThread().setContextClassLoader(originalClassLoader);

	      this.rate=rate;
	      this.channels=channels;
	    }
	    catch(Exception ee){
	      System.out.println(ee);
	    }
	  }
	
	  private boolean play_stream() {

	    boolean chained=false;

	    init_jorbis();

	    retry=RETRY;

	    //System.out.println("play_stream>");

	    loop: while(true){

		  if (stopped) {
	    	  break;
	      }

	      int eos=0;

	      int index=oy.buffer(BUFSIZE);
	      buffer=oy.data;
	      try{
	        bytes=bitStream.read(buffer, index, BUFSIZE);
	      }
	      catch(Exception e){
	    	e.printStackTrace();
	        return false;
	      }

	      oy.wrote(bytes);

	      if(chained){ //
	        chained=false; //   
	      } //
	      else{ //
	        if(oy.pageout(og)!=1){
	          if(bytes<BUFSIZE)
	            break;
	          System.err.println("Input does not appear to be an Ogg bitstream.");
	          return false;
	        }
	      } //
	      os.init(og.serialno());
	      os.reset();

	      vi.init();
	      vc.init();

	      if(os.pagein(og)<0){
	        // error; stream version mismatch perhaps
	        System.err.println("Error reading first page of Ogg bitstream data.");
	        return false;
	      }

	      retry=RETRY;

	      if(os.packetout(op)!=1){
	        // no page? must not be vorbis
	        System.err.println("Error reading initial header packet.");
	        break;
	        //      return false;
	      }

	      if(vi.synthesis_headerin(vc, op)<0){
	        // error case; not a vorbis header
	        System.err
	            .println("This Ogg bitstream does not contain Vorbis audio data.");
	        return false;
	      }

	      int i=0;

	      while(i<2){
	        while(i<2){
	          int result=oy.pageout(og);
	          if(result==0)
	            break; // Need more data
	          if(result==1){
	            os.pagein(og);
	            while(i<2){
	              result=os.packetout(op);
	              if(result==0)
	                break;
	              if(result==-1){
	                System.err.println("Corrupt secondary header.  Exiting.");
	                //return false;
	                break loop;
	              }
	              vi.synthesis_headerin(vc, op);
	              i++;
	            }
	          }
	        }

	        index=oy.buffer(BUFSIZE);
	        buffer=oy.data;
	        try{
	          bytes=bitStream.read(buffer, index, BUFSIZE);
	        }
	        catch(Exception e){
		      e.printStackTrace();
	          return false;
	        }
	        if(bytes==0&&i<2){
	          System.err.println("End of file before finding all Vorbis headers!");
	          return false;
	        }
	        oy.wrote(bytes);
	      }

	      /*{
	        byte[][] ptr=vc.user_comments;
	        StringBuffer sb=null;
	        if(acontext!=null)
	          sb=new StringBuffer();

	        for(int j=0; j<ptr.length; j++){
	          if(ptr[j]==null)
	            break;
	          System.err
	              .println("Comment: "+new String(ptr[j], 0, ptr[j].length-1));
	          if(sb!=null)
	            sb.append(" "+new String(ptr[j], 0, ptr[j].length-1));
	        }
	        System.err.println("Bitstream is "+vi.channels+" channel, "+vi.rate
	            +"Hz");
	        System.err.println("Encoded by: "
	            +new String(vc.vendor, 0, vc.vendor.length-1)+"\n");
	        if(sb!=null)
	          acontext.showStatus(sb.toString());
	      }*/

	      convsize=BUFSIZE/vi.channels;

	      vd.synthesis_init(vi);
	      vb.init(vd);

	      float[][][] _pcmf=new float[1][][];
	      int[] _index=new int[vi.channels];

	      getsourceLine(vi.channels, vi.rate);

	      while(eos==0){
	        while(eos==0){

	          /*if(player!=me){
	            try{
	              bitStream.close();
	              sourceLine.drain();
	              sourceLine.stop();
	              sourceLine.close();
	              sourceLine=null;
	            }
	            catch(Exception ee){
	            }
	            return false;
	          }*/

	          int result=oy.pageout(og);
	          if(result==0)
	            break; // need more data
	          if(result==-1){ // missing or corrupt data at this page position
	          //	    System.err.println("Corrupt or missing data in bitstream; continuing...");
	          }
	          else{
	            os.pagein(og);

	            if(og.granulepos()==0){ //
	              chained=true; //
	              eos=1; // 
	              break; //
	            } //

	            while(true){
	              result=os.packetout(op);
	              if(result==0)
	                break; // need more data
	              if(result==-1){ // missing or corrupt data at this page position
	                // no reason to complain; already complained above

	                //System.err.println("no reason to complain; already complained above");
	              }
	              else{
	                // we have a packet.  Decode it
	                int samples;
	                if(vb.synthesis(op)==0){ // test for success!
	                  vd.synthesis_blockin(vb);
	                }
	                while((samples=vd.synthesis_pcmout(_pcmf, _index))>0){
	                  float[][] pcmf=_pcmf[0];
	                  int bout=(samples<convsize ? samples : convsize);

	                  // convert doubles to 16 bit signed ints (host order) and
	                  // interleave
	                  for(i=0; i<vi.channels; i++){
	                    int ptr=i*2;
	                    //int ptr=i;
	                    int mono=_index[i];
	                    for(int j=0; j<bout; j++){
	                      int val=(int)(pcmf[i][mono+j]*32767.);
	                      if(val>32767){
	                        val=32767;
	                      }
	                      if(val<-32768){
	                        val=-32768;
	                      }
	                      if(val<0)
	                        val=val|0x8000;
	                      convbuffer[ptr]=(byte)(val);
	                      convbuffer[ptr+1]=(byte)(val>>>8);
	                      ptr+=2*(vi.channels);
	                    }
	                  }
	                  
	                  if (sourceLine == null) {
	                	  //maybe stopped
	                	  eos=1;
	                	  break;
	                  }
	                  
	                  sourceLine.write(convbuffer, 0, 2*vi.channels*bout);
	                  vd.synthesis_read(bout);
	                }
	              }
	            }
	            if(og.eos()!=0)
	              eos=1;
	          }
	        }

	        if(eos==0){
	          index=oy.buffer(BUFSIZE);
	          buffer=oy.data;
	          try{
	            bytes=bitStream.read(buffer, index, BUFSIZE);
	          }
	          catch(Exception e){
	            e.printStackTrace();
	            return false;
	          }
	          if(bytes==-1){
	            break;
	          }
	          oy.wrote(bytes);
	          if(bytes==0)
	            eos=1;
	        }
	      }

	      os.clear();
	      vb.clear();
	      vd.clear();
	      vi.clear();
	    }

	    oy.clear();

	    try{
	      if(bitStream!=null)
	        bitStream.close();
	    }
	    catch(Exception e){
	    }
	    
	    return true;
	}
	  
	public boolean play(InputStream is) {
		bitStream = is;
		return play_stream();
	}
	  
	public void stop(){
      try{
        //sourceLine.drain();
        sourceLine.stop();
        sourceLine.close();
        
        stopped = true;
        sourceLine=null;
        
        // it will close in play_steam()
        /*if(bitStream!=null)
          bitStream.close();*/
      }
      catch(Exception e){
      }
	}
}
