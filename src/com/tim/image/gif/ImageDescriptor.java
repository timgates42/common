package com.tim.image.gif;

import java.io.*;

public class ImageDescriptor extends BLOCK  {
	boolean TEST_CODER = true;
	// 1 byte Field ID
	static final int BLOCK_ID = 0x2C;

	// 4 * 2byte fields: 8 bytes;
	int
		left,
		top,
		width,
		height,
		packed,
		lzwMinCodeSize;
	int[]
		local_color_table;

	byte[] graphic_data;

	GIFImage parent;
	public ImageDescriptor(GIFImage gif) {
		parent = gif;
	}
	public GIFImage getParent() {
		return parent;
	}
	public void setParent(GIFImage gif) {
		parent = gif;
	}
	public int getLeft() {
		return left;
	}
	public void setLeft(int x) {
		left = x;
	}
	public int getTop() {
		return top;
	}
	public void setTop(int x) {
		top = x;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int x) {
		width = x;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int x) {
		height = x;
	}
	public int getMinCodeSize() {
		return lzwMinCodeSize;
	}
	public void setMinCodeSize(int x) {
		lzwMinCodeSize = x;
	}
	protected void setBit(int mask, boolean state) {
		if (state) {
			packed |= mask;
		} else {
			packed &= ~mask;
		}
	}

	public boolean getContainsLocalColorTable() {
		return (packed & 0x80) != 0;
	}
	public void setContainsLocalColorTable(boolean state) {
		setBit(0x80,state);
	}
	public boolean getInterlaced() {
		return (packed & 0x40) != 0;
	}
	public void setInterlaced(boolean state) {
		setBit(0x40,state);
	}
	public boolean getSorted() {
		return (packed & 0x20) != 0;
	}
	public void setSorted(boolean state) {
		setBit(0x20,state);
	}
	public int getReserved() {
		return (packed & 0x18) >> 4;
	}
	public void setReserved(int bits) {
		packed = (packed & ~0x18) | ((bits<<4) & 0x18);
	}
	public int getLocalColorTableSize() {
		return packed & 0x07;
	}
	public void setLocalColorTableSize(int bits) {
		packed = (packed & ~0x07) | (bits & 0x07);
	}
	public int[] getLocalColorTable() {
		return local_color_table;
	}
	public void setLocalColorTable(int[] table) {
		local_color_table = table;
	}
	public byte[] getPixels() {
		return graphic_data;
	}
	public void setPixels(byte[] pixels) {
		graphic_data = pixels;
	}
	public void fromStream(InputStream is) throws IOException {
		byte[] buffer = new byte[9];

		Util.readFully(is,buffer,9);
		left = ((buffer[1]&0xFF)<<8)|(buffer[0]&0xFF);
		top = ((buffer[3]&0xFF)<<8)|(buffer[2]&0xFF);
		width = ((buffer[5]&0xFF)<<8)|(buffer[4]&0xFF);
		height = ((buffer[7]&0xFF)<<8)|(buffer[6]&0xFF);

		packed = buffer[8];

		if (getContainsLocalColorTable()) {
			debug("Expecting local color table");
			local_color_table = Util.readColorTable(is,getLocalColorTableSize());
		}

		lzwMinCodeSize = is.read();
		if (lzwMinCodeSize == -1)
			throw new IOException("End of stream reached before ImageData");

		lzwMinCodeSize&=0xFF;

		int blockSize, blocks = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((blockSize = is.read()) > 0) {
			byte[] block = new byte[blockSize];
			Util.readFully(is,block,blockSize);
			baos.write(block);
			blocks++;
		}
		byte[] cdata = baos.toByteArray();
		debug("Data loaded, " + blocks + " blocks, decompressing");
		graphic_data = LZW.decompress(cdata,lzwMinCodeSize);
		baos.reset();
		baos = null;
		if (TEST_CODER) {
			debug("Testing coder");

			byte[] test = LZW.compress(graphic_data,lzwMinCodeSize);

			boolean equal = true;
			int ip =0;
			for (;equal&&ip<test.length;ip++)
				equal = test[ip] == cdata[ip];

			if (equal)
				debug("Compression Exactly the same");
			else
				debug("Compression differs at " + ip);

			debug("Compressed, Decompressing. Compressed size " + test.length + " original was " + cdata.length + " " + (100f * test.length / cdata.length) + "% of original " + (100f * test.length / graphic_data.length) + " % of raw");
			byte[] test2 = LZW.decompress(test,lzwMinCodeSize);
			equal = test2.length == graphic_data.length;

			ip = 0;
			for (;equal&&ip<test2.length;ip++)
				equal = test2[ip] == graphic_data[ip];

			if (equal) {
				debug("Test Passed");
			} else {
				throw new IOException("Coder FUCKED UP " + test2.length + " / " + graphic_data.length  + " " + ip + " was " + graphic_data[ip] + " now " + test2[ip] );
			}
		}

	}
	public void toStream(OutputStream os) throws IOException {
		byte[] buffer = new byte[9];

		/* LSD */
		buffer[0] = (byte) (left & 0xFF);
		buffer[1] = (byte) ((left >> 8) & 0xFF);

		buffer[2] = (byte) (top & 0xFF);
		buffer[3] = (byte) ((top >> 8) & 0xFF);

		buffer[4] = (byte) (width & 0xFF);
		buffer[5] = (byte) ((width >> 8) & 0xFF);

		buffer[6] = (byte) (height & 0xFF);
		buffer[7] = (byte) ((height >> 8) & 0xFF);

		buffer[8] = (byte) (packed & 0xFF);

		os.write(buffer);

		if (getContainsLocalColorTable()) {
			debug("Expecting local color table");
			Util.writeColorTable(os, getLocalColorTable(), getLocalColorTableSize());
		}

		os.write(lzwMinCodeSize & 0xFF);

		byte[] compressed = LZW.compress(graphic_data,lzwMinCodeSize);
		int pos = 0;
		int amount;
		while ((amount = Math.min(255,compressed.length - pos)) > 0) {
			os.write(amount); // write the amount out
			os.write(compressed,pos,amount); // write the data out
			pos+=amount;
		}
		os.write(0); // block terminator.
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ImageDescriptor[");
		sb.append("left=");
		sb.append(left);
		sb.append(",top=");
		sb.append(top);
		sb.append(",width=");
		sb.append(width);
		sb.append(",height=");
		sb.append(height);
		sb.append(",local_color_table_exists=");
		sb.append(getContainsLocalColorTable()?"true":"false");
		sb.append(",interlaced=");
		sb.append(getInterlaced()?"true":"false");
		sb.append(",sorted=");
		sb.append(getSorted()?"true":"false");
		sb.append(",reserved=");
		sb.append(getReserved());
		if (getContainsLocalColorTable()) {
			sb.append(",local_color_table[");
			for (int i=0;i<local_color_table.length;) {
				if (i%8==0) {
					sb.append('\n');
				} else if (i%4==0) {
					sb.append(' ');
				}
				sb.append(Util.toHexString(i,2));
				sb.append(':');
				sb.append(Util.toHexString(local_color_table[i],6));
				if (++i<local_color_table.length)
					sb.append(',');
			}
			sb.append(']');
		}
		sb.append(",min_code_size=");
		sb.append(getMinCodeSize());
		sb.append(",graphic_data.length=");
		sb.append(graphic_data.length);
		sb.append(']');
		return sb.toString();
	}
	protected void debug(String msg) {
		//System.out.println("[ImageDescriptor] " + msg);
	}

}
