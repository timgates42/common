package com.tim.image.gif;

import java.io.*;

public class GIFHeader {

	public GIFHeader() {

	}
	public GIFHeader(InputStream is) throws IOException {
		fromStream(is);
	}
	public void fromStream(InputStream is) throws IOException {
		readHeader(is);
		readLSD(is);
	}
	public void toStream(OutputStream os) throws IOException {
		writeHeader(os);
		writeLSD(os);
	}

 	/*
 	 * Gif Header; 6 bytes
 	 *
 	 */

	final byte[]
		/* Header: Signature; 3 bytes "GIF" */
		sig = new byte[] { (byte) 'G', (byte) 'I', (byte) 'F' };
	byte[]
		/* Header: Version; 3 bytes "87a" "89a" */
		version = new byte[3];

	protected void readHeader(InputStream is) throws IOException{
		debug("Reading header.");
		byte[] buffer = new byte[3];
		Util.readFully(is,buffer,3);

		boolean sig_ok = true;
		for (int i=3;sig_ok && --i>=0;sig_ok=buffer[i]==sig[i]);
		if (!sig_ok)
			throw new IOException("Signature Expected in GIF but not found!");

		Util.readFully(is,version,3);

	}
	protected void writeHeader(OutputStream os) throws IOException {
		os.write(sig);
		os.write(version);
	}
	/*
	 * Logical Screen Descriptor; 6 bytes
	 *
	 */

	int
		/* width; 2 bytes */
		width,
		/* height; 2 bytes */
		height,
		/* Packed Bitset; 1 byte */
		packed;

	int
		/* LSD: Background color index; 1 byte */
		background_color_index,
		/* LSD: Aspect Ratio; 1 byte; Aspect Ratio = (Pixel Aspect Ratio + 15) / 64 */
		pixel_aspect_ratio;
	float
		aspect_ratio; // Calculated from pixel_aspect_ratio

	/* Global Color Table (if flag is true) */
	int[] global_color_table; // 0x00RRGGBB

	/* Read in a Logical Screen Descriptor */

	protected void readLSD(InputStream is) throws IOException{
		debug("Reading LSD.");
		byte[] buffer = new byte[7];

		Util.readFully(is,buffer,7);
		width = ((buffer[1]&0xFF)<<8)|(buffer[0]&0xFF);
		height = ((buffer[3]&0xFF)<<8)|(buffer[2]&0xFF);

		packed = buffer[4];

		background_color_index = buffer[5] & 0xFF;
		setPixelAspectRatio(buffer[6] & 0xFF);

		if (getContainsGlobalColorTable())
			global_color_table = Util.readColorTable(is,getGlobalColorTableSize());

	}
	protected void writeLSD(OutputStream os) throws IOException {
		byte[] buffer = new byte[7];

		buffer[0] = (byte) (width & 0xFF);
		buffer[1] = (byte) ((width >> 8) & 0xFF);
		buffer[2] = (byte) (height & 0xFF);
		buffer[3] = (byte) ((height >> 8) & 0xFF);
		buffer[4] = (byte) packed;
		buffer[5] = (byte) background_color_index;
		buffer[6] = (byte) pixel_aspect_ratio;

		os.write(buffer);
		if (getContainsGlobalColorTable())
			Util.writeColorTable(os,getGlobalColorTable(), getGlobalColorTableSize());
	}

	/* Get/Set Method for header */
	protected void setBit(int mask, boolean state) {
		if (state) {
			packed |= mask;
		} else {
			packed &= ~mask;
		}
	}

	public int getWidth() {
		return width;
	}
	public void setWidth(int w) {
		width = w;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int h) {
		height = h;
	}

	public String getVersion() {
		return new String(version);
	}
	public void setVersion(String newver) {
		version = new byte[3];
		byte[] ver = newver.getBytes();
		System.arraycopy(ver,0,version,0,Math.min(3,ver.length));
	}


	/* Packed BitSet Methods */
	public boolean getContainsGlobalColorTable() {
		return (packed & 0x80) != 0;
	}
	public void setContainsGlobalColorTable(boolean state) {
		setBit(0x80,state);
	}

	public int getBitsPerPixel() {
		return (packed & 0x70) >> 4;
	}
	public void setBitsPerPixel(int bits) {
		packed = (packed & ~0x70) | ((bits<<4) & 0x70);
	}
	public boolean getSorted() {
		return (packed & 0x08) != 0;
	}
	public void setSorted(boolean state) {
		setBit(0x08,state);
	}
	public int getGlobalColorTableSize() {
		return packed & 0x07;
	}
	public void setGlobalColorTableSize(int bits) {
		packed = (packed & ~0x07) | (bits & 0x07);
	}
	public int getBackgroundColorIndex() {
		return background_color_index;
	}
	public void setBackgroundColorIndex(int bg) {
		background_color_index = bg;
	}
	public int getPixelAspectRatio() {
		return pixel_aspect_ratio;
	}
	public void setPixelAspectRatio(int ratio) {

		pixel_aspect_ratio = ratio;

		if (pixel_aspect_ratio > 0) {
			aspect_ratio = (pixel_aspect_ratio + 15) / 64f;
		} else {
			aspect_ratio = -1;
		}

	}
	public int[] getGlobalColorTable() {
		return global_color_table;
	}
	public void setGlobalColorTable(int[] table) {
		global_color_table = table;
	}
	public float getAspectRatio() {
		return aspect_ratio;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GIF");
		sb.append(new String(version,0,3));
		sb.append("[LSD[width=");
		sb.append(width);
		sb.append(",height=");
		sb.append(height);
		sb.append(",global_color_table_exists=");
		sb.append(getContainsGlobalColorTable()?"true":"false");
		sb.append(",bits_per_pixel=");
		sb.append(getBitsPerPixel());
		sb.append("+1,sorted=");
		sb.append(getSorted()?"true":"false");
		sb.append(",global_color_table_size=");
		sb.append(getGlobalColorTableSize());
		sb.append("+1,background_color_index=");
		sb.append(background_color_index);
		if (getContainsGlobalColorTable()) {
			sb.append("[0x");
			sb.append(Util.toHexString(global_color_table[background_color_index],6));
			sb.append(']');
		}
		sb.append(",pixel_aspect_ratio=");
		sb.append(pixel_aspect_ratio);
		sb.append(",(calculated)aspect_ratio=");
		sb.append(aspect_ratio);
		sb.append(']');
		if (getContainsGlobalColorTable()) {
			sb.append(",global_color_table[");
			for (int i=0;i<global_color_table.length;) {
				if (i%8==0) {
					sb.append('\n');
				} else if (i%4==0) {
					sb.append(' ');
				}
				sb.append(Util.toHexString(i,2));
				sb.append(':');
				sb.append(Util.toHexString(global_color_table[i],6));
				if (++i<global_color_table.length)
					sb.append(',');
			}
			sb.append(']');
		}
		sb.append(']');
		return sb.toString();
	}
	protected void debug(String msg) {
		//System.out.println("[GIFHeader] " + msg);
	}

}