package com.tim.image.gif;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Thanks to those f#ck1ng idiots at Netscape this black has pretty much become a standard.
 */
public class NetscapeLoop extends BLOCK {

	private static final int LOOP_ID = 0x01;

	public static final String NAME = "NETSCAPE2.0";

	private int loop_iterations;

	public NetscapeLoop() {
	}

	public NetscapeLoop( int loop_iterations ) {
		this.loop_iterations = loop_iterations;
	}

	public int getLoopIterations() {
		return loop_iterations;
	}

	public void fromStream( InputStream is ) throws IOException {
		debug( "Reading Netscape Sub Block" );
		int data_subblock_length = is.read();
		if ( data_subblock_length <= 0 ) {
			throw new IOException("Expected sub-block length but none found!");
		}
		byte[] sub_block = new byte[data_subblock_length];
		int read = is.read( sub_block, 0, data_subblock_length );
		if ( read != data_subblock_length ) {
			throw new IOException("Expected sub-block of length " + data_subblock_length + " but none found!");
		}
		switch( sub_block[0] ) {
		case LOOP_ID:
         loop_iterations = sub_block[1] | ( sub_block[2] << 8 );
			break;
		default:
			debug( "Warning Unknown Sub-Block ID" );
			break;
		}
		int terminator = is.read();
		if ( terminator != 0x00 ) {
			throw new IOException("Expected sub-block terminator but none found!");
		}
		debug( "Read Netscape Sub Block" );
	}

	public void toStream( OutputStream os ) throws IOException {
		os.write( 0x03 );
		os.write( LOOP_ID );
		os.write( loop_iterations & 0xFF );
		os.write( ( loop_iterations >> 8 ) & 0xFF );
		os.write( 0x00 );
	}

	protected void debug(String msg) {
		//System.out.println("[NetscapeLoop] " + msg);
	}

}

