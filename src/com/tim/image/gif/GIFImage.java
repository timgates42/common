package com.tim.image.gif;

import java.util.*;
import java.io.*;


public class GIFImage {

	GIFHeader header;

	/* Other blocks */
	Vector data_stream_blocks;
	public GIFImage() {
		header = new GIFHeader();
		data_stream_blocks = new Vector();
	}
	public GIFImage(InputStream is) throws IOException {
		fromStream(is);
	}

	public GIFHeader getHeader() {
		return header;
	}
	public void setHeader(GIFHeader header) {
		this.header = header;
	}
	public void fromStream(InputStream is) throws IOException {
		debug("Parsing gif.");
		header = new GIFHeader(is);
		debug("Reading blocks.");
		readBlocks(is);
	}
	public void toStream(OutputStream os) throws IOException {
		debug("Saving gif.");
		header.toStream(os);
		debug("Writing blocks.");
		writeBlocks(os);
	}
	protected void readBlocks(InputStream is) throws IOException {
		data_stream_blocks = new Vector();

		int ID;
		while ((ID = is.read()) != -1) {
			BLOCK block = null;
			switch (ID) {
				case ImageDescriptor.BLOCK_ID:
					// ImageDescriptor
					debug("Parsing ImageDescriptor");
					block = new ImageDescriptor(this);
					break;
				case BLOCK.EXTENSION_INTRODUCER:
					debug("Parsing Extension");
					block = readExtension(is);
					break;
				case BLOCK.TRAILER:
					break;
				default:
					debug("Unknown block ID: 0x" + Util.toHexString(ID,2) + " - " + ID + " breaking..");
					return;
					//break;

			}
			if (block != null) {
				block.fromStream(is);
				data_stream_blocks.addElement(block);
			}
		}
	}
	protected BLOCK readExtension(InputStream is) throws IOException {
		int EXT_ID = is.read();
		if (EXT_ID == -1)
			throw new IOException("Expected an ExtensionID but none found!");
		switch (EXT_ID) {
			case GraphicExtension.EXTENSION_ID:
				debug("Parsing GraphicExtension");
				return new GraphicExtension(this);
			case BLOCK.APPLICATION_EXTENSION_LABEL:
				debug("Parsing Application Extension");
            int application_name_length = is.read();
				if (application_name_length <= -1)
					throw new IOException("Expected an application name length but none found!");
				byte[] data = new byte[application_name_length];
            int read = is.read( data, 0, application_name_length );
				if ( read != application_name_length ) {
					throw new IOException("Expected an application name but none found!");
				}
				BLOCK block;
				String name = new String( data );
				if ( NetscapeLoop.NAME.equals( name ) ) {
					block = new NetscapeLoop();
				} else {
					throw new IOException( "Unknown application name " + name );
				}
				return block;
			default:
				debug("Unknown Extension ID");
				return null;
		}
	}

	protected void writeBlocks(OutputStream os) throws IOException {
		for (final Enumeration e = data_stream_blocks.elements();e.hasMoreElements();) {
			BLOCK block = (BLOCK) e.nextElement();
			int ID = -1;
			if (block instanceof ImageDescriptor) {
				ID = ImageDescriptor.BLOCK_ID;
			} else if (block instanceof GraphicExtension) {
				ID = BLOCK.EXTENSION_INTRODUCER;
			}
			os.write(ID & 0xFF);
			block.toStream(os);
		}
		os.write(BLOCK.TRAILER);
	}
	public int getNumBlocks() {
		return data_stream_blocks.size();
	}
	public Enumeration getBlocks() {
		return data_stream_blocks.elements();
	}
	public void setBlocks(Vector v) {
		data_stream_blocks = v;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GIFImage[");
		sb.append(header.toString());
		for (final Enumeration e = data_stream_blocks.elements();e.hasMoreElements();) {
			BLOCK block = (BLOCK) e.nextElement();
			sb.append(",\n");
			sb.append(block);

		}
		sb.append("];");
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 1) {
			InputStream is = new FileInputStream(args[0]);
			GIFImage gif = new GIFImage(is);
			System.out.println(gif);
			//OutputStream os = new FileOutputStream(args[0] + ".new.gif");
			GIFImage animated = Util.animate(gif);
			Util.display(animated);
			System.out.println("\n\nAnimated:\n\n"+animated);
			OutputStream os = new FileOutputStream(args[0] + ".new.gif");
			animated.toStream(os);
			os.flush();
			os.close();
		} else {
			for (int i=0;i<args.length;i++) {
				System.out.println("Loading: " + args[i]);
				GIFImage gif = new GIFImage(new FileInputStream(args[i]));
				System.out.println(gif.toString());
			}
		}
	}

	protected void debug(String msg) {
		//System.out.println("[GIFImage] " + msg);
	}

}