package com.tim.image.gif;

import java.io.*;


public class GraphicExtension extends BLOCK  {
	// 1 byte Field ID (Always 0x21)
	static final int
		EXTENSION_ID = 0xF9;

	int
		packed;
	int
		// 2 byte
		delay_time,
		// 1 byte
		transparent_color_index;
	GIFImage parent;

	public GraphicExtension(GIFImage gif) {
		parent = gif;
	}
	protected void setBit(int mask, boolean state) {
		if (state) {
			packed |= mask;
		} else {
			packed &= ~mask;
		}
	}

	public static final int DISPOSAL_NO_ACTION = 0;
	public static final int DISPOSAL_NO_DISPOSE = 1;
	public static final int DISPOSAL_CLEAR_REGION = 2;
	public static final int DISPOSAL_RESTORE = 3;

	public int getReserved() {
		return (packed & 0xE0) >> 5;
	}
	public void setReserved(int bits) {
		packed = (packed & ~0xE0) | ((bits<<5) & 0xE0);
	}
	public int getDisposalMethod() {
		return (packed & 0x1C) >> 2;
	}
	public void setDisposalMethod(int bits) {
		packed = (packed & ~0x1C) | ((bits<<2) & 0x1C);
	}
	public boolean getUserInputFlag() {
		return (packed & 0x02) != 0;
	}
	public void setUserInputFlag(boolean state) {
		setBit(0x02,state);
	}
	public boolean getTransparentColorFlag() {
		return (packed & 0x01) != 0;
	}
	public void setTransparentColorFlag(boolean state) {
		setBit(0x01,state);
	}
	public int getDelayTime() {
		return delay_time;
	}
	public void setDelayTime(int dtime) {
		delay_time = dtime;
	}
	public int getTransparentColorIndex() {
		return delay_time;
	}
	public void setTransparentColorIndex(int index) {
		transparent_color_index = index;
	}
	public void fromStream(InputStream is) throws IOException {

		byte[] buffer = new byte[6];

		Util.readFully(is,buffer,6);

		//block_size = buffer[0] & 0xFF; Is always 4 with this block.

		packed = buffer[1] & 0xFF;

		delay_time = ((buffer[3]&0xFF)<<8)|(buffer[2]&0xFF);

		transparent_color_index = buffer[4] & 0xFF;
		if ((buffer[5] & 0xFF) != 0)
			throw new IOException("Non zero block terminator");

	}
	public void toStream(OutputStream os) throws IOException {
		byte[] buffer = new byte[7];
		buffer[0] = (byte) (EXTENSION_ID & 0xFF); // Block Size;
		buffer[1] = 4; // Block Size;
		buffer[2] = (byte) (packed & 0xFF);
		buffer[3] = (byte) (delay_time & 0xFF);
		buffer[4] = (byte) ((delay_time & 0xFF00)>>8);
		buffer[5] = (byte) (transparent_color_index & 0xFF);
		buffer[6] = 0; // Block Terminator.
		os.write(buffer);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GraphicExtension[");
//			sb.append("block_size=");
//			sb.append(block_size);
		sb.append("reserved=");
		sb.append(getReserved());
		sb.append(",disposal_method=");
		sb.append(getDisposalMethod());
		sb.append(",user_input_flag=");
		sb.append(getUserInputFlag()?"true":"false");
		sb.append(",transparent_color_flag=");
		sb.append(getTransparentColorFlag()?"true":"false");
		sb.append(",delay_time=");
		sb.append(delay_time);
		sb.append(",transparent_color_index=");
		sb.append(transparent_color_index);
		sb.append(']');
		return sb.toString();
	}

}
