package com.tim.image.gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BLOCK {

	public static final int EXTENSION_INTRODUCER = 0x21;
	public static final int APPLICATION_EXTENSION_LABEL = 0xFF;
	public static final int TRAILER = 0x3B;

	public abstract void fromStream( InputStream is ) throws IOException;

	public abstract void toStream( OutputStream os ) throws IOException;

}
