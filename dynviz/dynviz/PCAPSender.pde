
import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class PCAPSender implements Runnable
{
  public PCAPSender(Object _owner)
  {
    this.owner = _owner;

    //    try {
    //			if ( owner instanceof PApplet ) 
    //				((PApplet)owner).registerDispose( this );
    //		}
    //		catch( NoClassDefFoundError e ) {;}
  }

  private void callReceiveHandler( byte[] data ) 
  throws NoSuchMethodException {

    Class[] types;		// arguments class types
    Object[] values;	// arguments values
    Method method;

    try {
      types	= new Class[] { 
        data.getClass()
        };
      values	= new Object[] { 
        data
      };
      method	= owner.getClass().getDeclaredMethod(receiveHandler, types);
      method.invoke( owner, values );
    }
    catch( IllegalAccessException e ) { 
      ;
    }
    catch( InvocationTargetException e ) { 
      e.printStackTrace();
    }
  }


  private boolean isUDP() throws IOException
  {
    try
    {
      curFPos = pktOffs;
      packet = new byte[ethOffs+ipOffs];
      byte[] tmp = new byte[3];
      if ((ethOffs+ipOffs)> this.in.read(packet, 0, ethOffs+ipOffs))
        throw new IOException("EOF");
      curFPos += ethOffs+ipOffs;
      System.arraycopy(packet, ethOffs+0x0C, tmp, 0,  2);
      if (tmp[0] == (byte)0x08 && tmp[1] == (byte)0x00 )
      {
        int step = pktOffs+ethOffs+ipOffs+ipInPrtOffs-curFPos;
        if (step > this.in.skip(step))
          throw new IOException("EOF");
        if (this.in.read(tmp, 0, 1) <1)
          throw new IOException("EOF");
        curFPos += step+1;
        if (tmp[0] == (byte)0x11)
        {
          isudp = true; 
          return true;
        }
      }
    }
    catch(Exception e)
    {
      println("PCAPSender.isUDP(), error: "+e);
      throw new IOException("EOF");
    }
    isudp = false;
    return false;
  }

  private byte[] get_pkt() throws IOException
  {
    if (isudp)
    {
      int c = 0;
      byte[] retA= new byte[currPktLen-dataOffs+ethOffs];
      try
      {
        if (curFPos < pktOffs+dataOffs)    
        {    
          if ((pktOffs+dataOffs-curFPos)> this.in.skip(pktOffs+dataOffs-curFPos))
            throw new IOException("EOF");
          curFPos+=pktOffs+dataOffs-curFPos;
        }
        //println("curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\ndata offs "+hex(dataOffs));
        c = this.in.read(retA);
        if (c > currPktLen-dataOffs+ethOffs)
          throw new IOException("EOF");
        //println("curpos: "+hex(curFPos));
        //println("dataLen: "+hex(currPktLen-(ipOffs+udpOffs+udpDtaOffs)));
        //        for (int i = 0; i<retA.length; i++)
        //          print(char(retA[i]));
        //println();
        if (c == -1)
          return null;
        curFPos += c;
        // //println(char(retA));
        return retA;
      }
      catch(Exception e)
      {
        println("PCAPSender.get_pkt, error: "+e);
        return null;
      }
    }
    return null;
  }

  private boolean read_pkt_hdr() throws IOException
  {
    isUDP();
    try
    {

      byte[] tmp = new byte[4];
      System.arraycopy(packet, 0, tmp, 0, 4);
      hiTS = byteArrayToInt(tmp);
      //println("hiTS : "+hiTS);
      System.arraycopy(packet, 4, tmp, 0,  4);
      loTS = byteArrayToInt(tmp);
      //println("loTS : "+loTS);
      System.arraycopy(packet, 8, tmp, 0,  4);
      currPktLen = byteArrayToInt(tmp);
      //println("currPktLen : "+currPktLen);
      nextTime = startTime + (hiTS-shiTS)*1000+(loTS-sloTS)/1000;
      //println("nextTime : "+nextTime);
    }
    catch(Exception e)
    {
      println("PCAPSender.read_pkt_hdr(), error: "+e);
    }


    return isudp;
  }

  private void find_next_pkt() throws IOException
  {
    do
    {      
      pktOffs += currPktLen+ethOffs;
      if (curFPos < pktOffs)
      {
        try
        {
          if ((pktOffs-curFPos)> this.in.skip(pktOffs-curFPos))
            throw new IOException("EOF");
          //println("next packet searching. curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\nskipping: "+hex(pktOffs-curFPos));
        }
        catch(Exception e)
        {
          println("En error appeared while next pkt searching!! "+e);
          break;
        }
        curFPos += pktOffs-curFPos;
        //println("Next packet, curFPos: "+hex(curFPos)+"\npktOffs at new pkt: "+hex(pktOffs));
      }
      ////println("find pkt :"+pktOffs);
      //println("Searching packet...");
    }
    while(!read_pkt_hdr());
    //println("Packet Got");
  }

  public void run()
  {
    started =true;  
    //      this.thread = new Thread(this);
    //      thread.start();
    startTime = System.currentTimeMillis();
    nextTime = startTime;

    long tme;
    while (true)
    {      
      try
      {
        tme = System.currentTimeMillis();
        try
        {
          ////println("te : "+tme);
          if(tme<nextTime-50)
          {
            tme = nextTime- System.currentTimeMillis() - 40;
            thread.sleep(tme);
          }
        }
        catch(Exception e)
        {
          //println("PAUSE");
        }
        if (tme >= nextTime)
        {
          //println("Thread, sending");
          byte[] sendP = get_pkt();
          if (sendP == null)
            continue;
          //println("Sending packet...");
          try
          {          
            callReceiveHandler(sendP);
          }
          catch(NoSuchMethodException e)
          {
            println("NO "+receiveHandler+" method in dinvyz class!!!");
          }

          find_next_pkt();
        }
      }
      catch(IOException ioe)
      {

        println("Exception: "+ioe);
        if (this.repeat)
        {
          println("Reloading the file");
          this.openPCAP();
          startTime = System.currentTimeMillis();
          nextTime = startTime;
        }
        else        
          return;
      }
    }
    
  }

  public void dispose()
  {
    try
    {
      this.thread.interrupt();
      this.in.close();
      this.in = null;
      this.thread = null;
    }
    catch(Exception e)
    {
      println("PCAPSender.dispose() throwed exception "+e);
    }
  }

  private boolean openPCAP()
  {
    try
    {
      if (this.in != null)
      {        
        this.curFPos = 0;
        this.pktOffs = 0x18;
        this.in.close();
        this.in = null;
        //this.thread.interrupt();
        //println("PCAPSender.openPCAP, thread interrupted");
        //this.thread = null;
        //println("PCAPSender.openPCAP, thread nulled");
      }
      this.in = new FileInputStream(pcapFile);
      println("PCAPSender.openPCAP, PCAP file loaded: "+this.in);
      byte[] bbb = new byte[4];
      in.read(bbb);
      int magic = byteArrayToInt(bbb);
      curFPos += bbb.length;
      ////println("magic "+hex(magic));
      if (magic != 0xA1B2C3D4)
      {
        println("PCAPSender.openPCAP, wrong magic number!");
        //bytes = null;
        
        return false;
      }
      println("PCAPSender.openPCAP, correct magic number!");
      in.skip(pktOffs-curFPos);
      //println("skipped "+hex(pktOffs-curFPos)+" bytes(+4 -cPos)");
      curFPos = pktOffs;

      println("PCAPSender.openPCAP, reading header");
      read_pkt_hdr();
      shiTS= hiTS;
      sloTS = loTS;
      println("PCAPSender.openPCAP, header has read");
    }
    catch (Exception e)
    {
      println("PCAPSender.openPCAP, Exception: "+e);
      return false;
    }
    if (!this.started)
    {
      this.thread = new Thread(this);
      this.thread.start();
    }
    println("GoodFile");
    return true;
  }

  // private byte[] bytes;
  Thread thread	= null;
  private byte[] packet;
  private boolean isudp = false;
  private Object owner = null;

  private final String pcapFile = "C:\\Users\\kgray\\Desktop\\My Stuff\\DynViz\\sample_data.pcap";

  private int curFPos = 0;
  private int pktOffs = 0x18;
  private  int ethOffs = 0x10;
  private int ipOffs = 0x0E;
  private int udpOffs = 0x14;
  private int ipInPrtOffs = 0x09;
  private int udpDtaOffs = 0x08;
  private int udpPktLengthAt = 0x04;
  private int currPktLen = 0;
  private  int dataOffs = ethOffs+ipOffs+udpOffs+udpDtaOffs;

  private String receiveHandler = "receive";

  private long nextTime = 0;
  private int hiTS = 0;
  private int loTS = 0;

  private int shiTS = 0;
  private int sloTS = 0;
  private long startTime;
  public boolean started = false;
  
  // PCAP file send looping
  public boolean repeat = true;

  private FileInputStream in = null;
}

