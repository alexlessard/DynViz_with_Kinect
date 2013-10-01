package com.dyndns.dynviz.pcap;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.dyndns.dynviz.DynViz;

public class PCAPSender implements Runnable {

	public PCAPSender(DynViz parent, String pcapFile) {
		this.parent = parent;
		this.pcapFile = pcapFile;
	}

	private boolean isUDP() throws IOException {
		try {
			curFPos = pktOffs;
			packet = new byte[ethOffs + ipOffs];
			if ((ethOffs + ipOffs) > this.in.read(packet, 0, ethOffs + ipOffs))
				throw new IOException("EOF");
			curFPos += ethOffs + ipOffs;
			System.arraycopy(packet, ethOffs + 0x0C, tmp3, 0, 2);
			if (tmp3[0] == (byte) 0x08 && tmp3[1] == (byte) 0x00) {
				int step = pktOffs + ethOffs + ipOffs + ipInPrtOffs - curFPos;
				if (step > this.in.skip(step))
					throw new IOException("EOF");
				if (this.in.read(tmp3, 0, 1) < 1)
					throw new IOException("EOF");
				curFPos += step + 1;
				if (tmp3[0] == (byte) 0x11) {
					isudp = true;
					return true;
				}
			}
		} catch (Exception e) {
			System.out.println("PCAPSender.isUDP(), error: " + e);
			throw new IOException("EOF");
		}
		isudp = false;
		return false;
	}

	private byte[] get_pkt() throws IOException {
		if (isudp) {
			int c = 0;
			int len = currPktLen - dataOffs + ethOffs;
			byte[] retA = new byte[len];
			try {
				if (curFPos < pktOffs + dataOffs) {
					if ((pktOffs + dataOffs - curFPos) > this.in.skip(pktOffs + dataOffs - curFPos))
						throw new IOException("EOF");
					curFPos += pktOffs + dataOffs - curFPos;
				}
				// System.out.println("curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\ndata offs "+hex(dataOffs));
				c = this.in.read(retA);
				if (c > len)
					throw new IOException("EOF");
				// System.out.println("curpos: "+hex(curFPos));
				// System.out.println("dataLen: "+hex(currPktLen-(ipOffs+udpOffs+udpDtaOffs)));
				// for (int i = 0; i<retA.length; i++)
				// System.out.print(char(retA[i]));
				// System.out.println();
				if (c == -1)
					return null;
				curFPos += c;
				// //System.out.println(char(retA));
				return retA;
			} catch (Exception e) {
				System.out.println("PCAPSender.get_pkt, error: " + e);
				return null;
			}
		}
		return null;
	}

	private boolean read_pkt_hdr() throws IOException {
		isUDP();
		try {
			System.arraycopy(packet, 0, tmp4, 0, 4);
			hiTS = byteArrayToInt(tmp4);
			// System.out.println("hiTS : "+hiTS);
			System.arraycopy(packet, 4, tmp4, 0, 4);
			loTS = byteArrayToInt(tmp4);
			// System.out.println("loTS : "+loTS);
			System.arraycopy(packet, 8, tmp4, 0, 4);
			currPktLen = byteArrayToInt(tmp4);
			// System.out.println("currPktLen : "+currPktLen);
			nextTime = startTime + (hiTS - shiTS) * 1000 + (loTS - sloTS) / 1000;
			// System.out.println("nextTime : "+nextTime);
		} catch (Exception e) {
			System.out.println("PCAPSender.read_pkt_hdr(), error: " + e);
		}

		return isudp;
	}

	private void find_next_pkt() throws IOException {
		do {
			pktOffs += currPktLen + ethOffs;
			if (curFPos < pktOffs) {
				try {
					if ((pktOffs - curFPos) > this.in.skip(pktOffs - curFPos))
						throw new IOException("EOF");
					// println("next packet searching. curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\nskipping: "+hex(pktOffs-curFPos));
				} catch (Exception e) {
					System.out.println("En error appeared while next pkt searching!! " + e);
					break;
				}
				curFPos += pktOffs - curFPos;
				// System.out.println("Next packet, curFPos: "+hex(curFPos)+"\npktOffs at new pkt: "+hex(pktOffs));
			}
			// //System.out.println("find pkt :"+pktOffs);
			// System.out.println("Searching packet...");
		} while (!isDisposed && !read_pkt_hdr());
		// System.out.println("Packet Got");
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY + 1);
		started = true;
		startTime = System.currentTimeMillis();
		nextTime = startTime;

		long tme;
		while (!isDisposed) {
			try {
				tme = System.currentTimeMillis();
				try {
					// System.out.println("te : "+tme);
					if (tme < nextTime - 50) {
						tme = nextTime - System.currentTimeMillis();
						Thread.sleep(tme);
					}
				} catch (Exception e) {
					// System.out.println("PAUSE");
				}
				if (tme >= nextTime) {
					byte[] sendP = get_pkt();
					if (sendP == null)
						continue;
					parent.receive(sendP);

					find_next_pkt();
				}
			} catch (IOException ioe) {
				System.out.println("PCAPSender.run() / Exception: " + ioe);
				if (this.repeat) {
					System.out.println("Reloading the file");
					this.openPCAP();
					startTime = System.currentTimeMillis();
					nextTime = startTime;
				} else {
					dispose();
					return;
				}
			}
		}

	}

	public void dispose() {
		isDisposed = true;
		try {
			if (in != null) {
				IOUtils.closeQuietly(in);
				in = null;
			}
			if (thread != null) {
				thread.interrupt();
				thread = null;
			}
		} catch (Exception e) {
			System.out.println("PCAPSender.dispose() throwed exception " + e);
		}
	}

	public boolean openPCAP() {
		try {
			if (this.in != null) {
				this.curFPos = 0;
				this.pktOffs = 0x18;
				this.in.close();
				this.in = null;
				// this.thread.interrupt();
				// System.out.println("PCAPSender.openPCAP, thread interrupted");
				// this.thread = null;
				// System.out.println("PCAPSender.openPCAP, thread nulled");
			}
			this.in = new FileInputStream(pcapFile);
			System.out.println("PCAPSender.openPCAP, PCAP file loaded: " + pcapFile);
			byte[] bbb = new byte[4];
			in.read(bbb);
			int magic = byteArrayToInt(bbb);
			curFPos += bbb.length;
			// //println("magic "+hex(magic));
			if (magic != 0xA1B2C3D4) {
				System.out.println("PCAPSender.openPCAP, wrong magic number!");
				// bytes = null;

				return false;
			}
			System.out.println("PCAPSender.openPCAP, correct magic number!");
			in.skip(pktOffs - curFPos);
			// println("skipped "+hex(pktOffs-curFPos)+" bytes(+4 -cPos)");
			curFPos = pktOffs;

			System.out.println("PCAPSender.openPCAP, reading header");
			read_pkt_hdr();
			shiTS = hiTS;
			sloTS = loTS;
			System.out.println("PCAPSender.openPCAP, header has read");
		} catch (Exception e) {
			System.out.println("PCAPSender.openPCAP, Exception: " + e);
			return false;
		}
		if (!this.started) {
			this.thread = new Thread(this);
			this.thread.start();
		}
		System.out.println("GoodFile");
		return true;
	}

	private static final int byteArrayToInt(byte[] b) {
		return (b[3] << 24) + ((b[2] & 0xFF) << 16) + ((b[1] & 0xFF) << 8) + (b[0] & 0xFF);
	}

	private final DynViz parent;
	private final String pcapFile;
	private boolean isDisposed;

	Thread thread;
	private byte[] packet;
	private boolean isudp = false;

	private int curFPos = 0;
	private int pktOffs = 0x18;
	private int ethOffs = 0x10;
	private int ipOffs = 0x0E;
	private int udpOffs = 0x14;
	private int ipInPrtOffs = 0x09;
	private int udpDtaOffs = 0x08;
	private int currPktLen = 0;
	private int dataOffs = ethOffs + ipOffs + udpOffs + udpDtaOffs;

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

	private final byte[] tmp3 = new byte[3];
	private final byte[] tmp4 = new byte[4];
}
