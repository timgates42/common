package com.tim.image.gif;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;


public class Util {


	static final char[] hex = "0123456789ABCDEF".toCharArray();

	public static final String toHexString(int x, int digits) {
		x&=0x7FFFFFFF;
		StringBuffer sb = new StringBuffer();
		while (--digits>=0) {
			sb.append(hex[x%16]);
			x/=16;
		}
		return sb.reverse().toString();
	}
	public static final String binCode(int x, int digits) {
		StringBuffer sb = new StringBuffer();
		while (--digits>=0) {
			sb.append(x%2==0?'0':'1');
			x/=2;
		}
		return sb.reverse().toString();
	}
	public static void readFully(InputStream is, byte[] b) throws IOException {
		readFully(is,b,0,b.length);
	}
	public static void readFully(InputStream is, byte[] b, int len) throws IOException {
		readFully(is,b,0,len);
	}
	public static void readFully(InputStream is, byte[] b,int off, int len) throws IOException {

		int pos = off;
		int amount, amountRead = -1;
		while ((amount = len - pos + off) > 0 && (amountRead = is.read(b,pos,amount)) != -1) {
			pos+=amountRead;
		}
		if (amountRead == -1)
			throw new IOException("End of stream reached before data fully read!");

	}
 	public static int[] readColorTable(InputStream is, int table_size) throws IOException {
		int entries = (int) Math.pow(2,table_size+1);
		int[] temp = new int[entries];
		byte[] buffer = new byte[3];
		for (int i=0;i<entries;i++) {
			readFully(is, buffer,3);
			temp[i] = ((buffer[0]&0xFF)<<16) | ((buffer[1]&0xFF)<<8) | (buffer[2]&0xFF);
		}
		return temp;
	}
	public static void writeColorTable(OutputStream os, int[] table, int table_size) throws IOException {
		int entries = (int) Math.pow(2,table_size+1);
		byte[] buffer = new byte[3 * entries];
		int index=0;
		for (int i=0;i<entries;i++) {
			int temp = table[i];
			buffer[index++] = (byte) ((temp >> 16) & 0xFF);
			buffer[index++] = (byte) ((temp >> 8) & 0xFF);
			buffer[index++] = (byte) (temp & 0xFF);
		}
		os.write(buffer);
		buffer = null;
	}
	public static GIFImage animate(GIFImage gif) {
		GIFImage animated = new GIFImage();
		GIFHeader header = gif.getHeader();
		animated.setHeader(header);

		int[] gTable = header.getGlobalColorTable();

		GraphicExtension mod = null;
		Vector animation = new Vector();
		System.out.println("Animating.");
		for (final Enumeration e = gif.getBlocks();e.hasMoreElements();) {
			BLOCK block = (BLOCK) e.nextElement();
			if (block instanceof GraphicExtension) {
				mod = (GraphicExtension) block;
			} else if (block instanceof ImageDescriptor) {
				ImageDescriptor id = (ImageDescriptor) block;

				int[] table = gTable;
				int size = header.getGlobalColorTableSize();
				if (id.getContainsLocalColorTable()) {
					table = id.getLocalColorTable();
					size = id.getLocalColorTableSize();
				}

				int
					w = id.getWidth(),
					h = id.getHeight(),
					l = id.getLeft(),
					t = id.getTop();

				byte[] pixels = id.getPixels();

				boolean interlaced = id.getInterlaced();

				if (interlaced) {
					byte[] data = new byte[pixels.length];
					int pass = 1, row = -8, jump=8;
					for (int i=0;i<pixels.length;i++) {
						final int x = i%w;
						if (x == 0) {
							row+=jump;
							if (row>=h) {
								pass++;
								switch (pass) {
									case 2:
										row=4;
										jump=8;
										break;
									case 3:
										row=2;
										jump=4;
										break;
									case 4:
										row=1;
										jump=2;
										break;
								}

							}
						}
						// There must be a better way, im just too lazy to think of it.
						// There must be a function of i
						/*
						Group 1 : Every 8th. row, starting with row 0.              (Pass 1)
						Group 2 : Every 8th. row, starting with row 4.              (Pass 2)
						Group 3 : Every 4th. row, starting with row 2.              (Pass 3)
						Group 4 : Every 2nd. row, starting with row 1.              (Pass 4)
						*/
						data[(row * w) + x] = pixels[i];
					}
					pixels = data;
				}
				if ( mod != null ) {
					mod.setDelayTime(1);
					mod.setDisposalMethod(2);
				}
				header.setContainsGlobalColorTable(true);
				header.setGlobalColorTable(table);
				header.setGlobalColorTableSize(size);
				float frames = 40;
				float dx = w/frames, dy = h/frames;

				for (int i=0;i<frames;i++) {

					System.out.println("Animating frame " + i);


					int
						sx = (int) (dx*i),
						sy = (int) (dy*i);
					byte[] shifted = shiftLeft(pixels,w,h,sx);
					shifted = shiftUp(shifted,w,h,sy);
					ImageDescriptor id2 = new ImageDescriptor(animated);
					id2.setLeft(l);
					id2.setTop(t);
					id2.setWidth(w);
					id2.setHeight(h);
					id2.setContainsLocalColorTable(false);
					id2.setInterlaced(false);
					id2.setSorted(id.getSorted());
					id2.setReserved(0);
					id2.setLocalColorTableSize(0);
					id2.setLocalColorTable(null);
					id2.setPixels(shifted);
					id2.setMinCodeSize(id.getMinCodeSize());
					if (mod != null) {
						animation.addElement(mod);
					}
					animation.addElement(id2);
					//break;


				}
				mod = null;
				break;
			}
		}
		animated.setBlocks(animation);
		return animated;


	}
	public static byte[] shiftLeft(byte[] pixels, int w,int h, int step_x) {
		byte[] shifted = new byte[pixels.length];
		for (int y=0;y<h;y++) {
			int off = y*w;
			System.arraycopy(pixels,off+step_x,shifted,off,w-step_x);
			System.arraycopy(pixels,off,shifted,off+(w-step_x),step_x);
		}
		return shifted;

	}
	public static byte[] shiftUp(byte[] pixels,int w, int h, int step_y) {
		int up = step_y * w;
		byte[] shift_up = new byte[up];
		System.arraycopy(pixels,0,shift_up,0,up);
		System.arraycopy(pixels,up,pixels,0,(w*h)-up);
		System.arraycopy(shift_up,0,pixels,(w*h)-up,up);
		return pixels;
	}
	public static void display(GIFImage gif) {
		GIFHeader header = gif.getHeader();
		int[] gTable = header.getGlobalColorTable();
		GraphicExtension mod = null;

		Toolkit tk = Toolkit.getDefaultToolkit();

		Vector sequence = new Vector();
		for (final Enumeration e = gif.getBlocks();e.hasMoreElements();) {
			BLOCK block = (BLOCK) e.nextElement();
			if (block instanceof GraphicExtension) {
				mod = (GraphicExtension) block;
			} else if (block instanceof ImageDescriptor) {
				ImageDescriptor id = (ImageDescriptor) block;

				int[] table = gTable;

				if (id.getContainsLocalColorTable())
					table = id.getLocalColorTable();

				int bg = -1;
				if (mod != null) {
					if (mod.getTransparentColorFlag())
						bg = mod.getTransparentColorIndex();
				}
				// This next section of code is not thread safe
				for (int i=0;i<table.length;i++) {
					if (i == bg) {
						table[i] &= ~0xFF000000;
					} else {
						table[i] |= 0xFF000000;
					}
				}

				byte[] pixels = id.getPixels();

				int
					w = id.getWidth(),
					h = id.getHeight(),
					l = id.getLeft(),
					t = id.getTop();

				int[] data = new int[w * h];

				boolean interlaced = id.getInterlaced();
				int pass = 1, row = -8, jump=8;
				for (int i=0;i<data.length && i<pixels.length;i++) {
					if (interlaced) {
						final int x = i%w;
						if (x == 0) {
							row+=jump;
							if (row>=h) {
								pass++;
								switch (pass) {
									case 2:
										row=4;
										jump=8;
										break;
									case 3:
										row=2;
										jump=4;
										break;
									case 4:
										row=1;
										jump=2;
										break;
								}

							}
						}
						// There must be a better way, im just too lazy to think of it.
						// There must be a function of i
						/*
						Group 1 : Every 8th. row, starting with row 0.              (Pass 1)
						Group 2 : Every 8th. row, starting with row 4.              (Pass 2)
						Group 3 : Every 4th. row, starting with row 2.              (Pass 3)
						Group 4 : Every 2nd. row, starting with row 1.              (Pass 4)
						*/
						data[(row * w) + x]=table[pixels[i]&0xFF];
					} else {
						data[i]=table[pixels[i]&0xFF];
					}
				}

				Image img = tk.createImage(new MemoryImageSource(w,h,data,0,w));

				Sequence s = new Sequence(img,mod,id);
				sequence.addElement(s);
				mod = null;
			}
		}
		SequencePlayer player = new SequencePlayer(header.getWidth(), header.getHeight(),sequence);
		player.setVisible(true);
		Insets ins = player.getInsets();
		int fwidth = header.getWidth() + ins.left + ins.right;
		int fheight = header.getHeight() + ins.top + ins.bottom;
		System.out.println("Width: " + fwidth + " height: " + fheight);
		player.setSize(fwidth, fheight);
		player.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(-1);
			}
		});
	}
	private static class Sequence {

		Image img;
		GraphicExtension mod;
		ImageDescriptor descriptor;
		public Sequence(Image img, GraphicExtension mod, ImageDescriptor descriptor) {
			this.img = img;
			this.mod = mod;
			this.descriptor = descriptor;
		}
		public int getLeft() {
			return descriptor.getLeft();
		}
		public int getTop() {
			return descriptor.getTop();
		}
		public int getWidth() {
			return descriptor.getWidth();
		}
		public int getHeight() {
			return descriptor.getHeight();
		}
		public int getDelay() {
			return (mod == null) ? -1 : mod.getDelayTime();
		}
		public Image getImage() {
			return img;
		}

	}
	private static class SequencePlayer extends Frame implements Runnable {

		Vector sequence;

		Sequence current;
		Image buffer;
		Graphics bufferG;
		int w,h;
		Thread engine;

		int index;

		boolean painted;
		Object paintLock;
		public SequencePlayer(int w, int h, Vector seq) {
			paintLock = new Object();
			sequence = seq;
			index = 0;
			current = (Sequence) seq.elementAt(0);
			this.w = w;
			this.h = h;
			if (seq.size() > 1) {
				engine = new Thread(this);
				engine.start();
			}
		}

		public void run() {
			Thread me = Thread.currentThread();
			while (me == engine) {
				int currentDelay = current.getDelay();
				if (currentDelay > -1) {
					long split = System.currentTimeMillis();
					long delta;
					while ((delta = currentDelay - (System.currentTimeMillis() - split)) > 0) {
						try {
							Thread.sleep(delta);
						} catch (InterruptedException e) {
							break;
						}
					}
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						break;
					}
				}
				index=++index%sequence.size();
				current=(Sequence) sequence.elementAt(index);
				synchronized (paintLock) {
					repaint();
					painted = false;
					while (engine == me && ! painted) {
						try {
							paintLock.wait();
						} catch (Exception e) {

						}
					}
				}
			}
		}

		public void update(Graphics g) {
			paint(g);
		}
		public void paint(Graphics g) {

			if (buffer == null) {
				buffer = createImage(w,h);
				bufferG = buffer.getGraphics();
			}
			Insets ins = getInsets();
			synchronized (paintLock) {
				if (painted) {
					g.drawImage(buffer,ins.left,ins.top,null);
					return;
				}
			}
			bufferG.setColor(Color.black);
			bufferG.fillRect(0,0,w,h);
			if (current != null) {
				Image cur = current.getImage();
				bufferG.drawImage(cur,current.getLeft(),current.getTop(),null);
			}
			g.drawImage(buffer,ins.left,ins.top,null);
			synchronized (paintLock) {
				painted = true;
				paintLock.notifyAll();
			}

		}
	}


	private static final Hashtable CACHE = new Hashtable();

	public static AnimatedGIF read( String file ) throws IOException {
		synchronized( CACHE ) {
			if ( CACHE.containsKey( file ) ) {
				return (AnimatedGIF) CACHE.get( file );
			}
			AnimatedGIF gif = read( new FileInputStream( file ) );
			CACHE.put( file, gif );
			return gif;
		}
	}
	public static AnimatedGIF read( InputStream input ) throws IOException {
		GIFImage gif = new GIFImage( input );
		GIFHeader header = gif.getHeader();
		int[] gTable = header.getGlobalColorTable();
		GraphicExtension mod = null;
      int[] bImage = new int[header.getWidth()*header.getHeight()];
		int[] oldImage = new int[header.getWidth()*header.getHeight()];
		AnimatedGIF result = new AnimatedGIF( header.getWidth(), header.getHeight() );
		int w = 0;
		int h = 0;
		int l = 0;
		int t = 0;
		int full_width = header.getWidth();
		for (final Enumeration e = gif.getBlocks();e.hasMoreElements();) {
			BLOCK block = (BLOCK) e.nextElement();
			if (block instanceof GraphicExtension) {
				mod = (GraphicExtension) block;
			} else if (block instanceof ImageDescriptor) {
				ImageDescriptor id = (ImageDescriptor) block;

				int[] table = gTable;

				if (id.getContainsLocalColorTable())
					table = id.getLocalColorTable();

				int bg = -1;
				if (mod != null) {
					if (mod.getTransparentColorFlag())
						bg = mod.getTransparentColorIndex();
				}
				// This next section of code is not thread safe
				for (int i=0;i<table.length;i++) {
					if (i == bg) {
						table[i] &= ~0xFF000000;
					} else {
						table[i] |= 0xFF000000;
					}
				}

				byte[] pixels = id.getPixels();

				w = id.getWidth();
				h = id.getHeight();
				l = id.getLeft();
				t = id.getTop();

				boolean interlaced = id.getInterlaced();
				int pass = 1, row = -8, jump=8;
				for (int i=0;i<bImage.length && i<pixels.length;i++) {
					final int x = i%w;
					final int y = i/w;
					if (interlaced) {
						if (x == 0) {
							row+=jump;
							if (row>=h) {
								pass++;
								switch (pass) {
									case 2:
										row=4;
										jump=8;
										break;
									case 3:
										row=2;
										jump=4;
										break;
									case 4:
										row=1;
										jump=2;
										break;
								}

							}
						}
						// There must be a better way, im just too lazy to think of it.
						// There must be a function of i
						/*
						Group 1 : Every 8th. row, starting with row 0.              (Pass 1)
						Group 2 : Every 8th. row, starting with row 4.              (Pass 2)
						Group 3 : Every 4th. row, starting with row 2.              (Pass 3)
						Group 4 : Every 2nd. row, starting with row 1.              (Pass 4)
						*/
						bImage[((row+t) * full_width) + x + l]=table[pixels[i]&0xFF];
					} else {
						bImage[((y+t) * full_width) + x + l]=table[pixels[i]&0xFF];
					}
				}

				int[] data = new int[bImage.length];
				System.arraycopy( bImage, 0, data, 0, bImage.length );
				result.addImage( data, ( mod == null ) ? 10000 : mod.getDelayTime() );
				if ( mod != null ) {
					switch( mod.getDisposalMethod() ) {
					case GraphicExtension.DISPOSAL_CLEAR_REGION:
						for ( int x = 0; x < w; x++ ) {
							for ( int y = 0; y < h; y++ ) {
								bImage[y*full_width+x] = 0;
							}
						}
						System.arraycopy( bImage, 0, oldImage, 0, bImage.length );
						break;
					case GraphicExtension.DISPOSAL_NO_ACTION:
					case GraphicExtension.DISPOSAL_NO_DISPOSE:
						System.arraycopy( bImage, 0, oldImage, 0, bImage.length );
						break;
					case GraphicExtension.DISPOSAL_RESTORE:
						System.arraycopy( oldImage, 0, bImage, 0, bImage.length );
						break;
					}
				}
				mod = null;
			}
		}
		return result;
	}

}