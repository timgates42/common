package com.tim.image.gif;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LZW {
	private static final byte[] append(byte[] a, byte b) {
		byte[] c = new byte[a.length+1];
		System.arraycopy(a,0,c,0,a.length);
		c[a.length] = b;
		return c;
	}
	final static int
		COMPRESS = 0, WORD_SIZE = 1, LEN = 2;


	public static void packBits(ByteArrayOutputStream baos, int[] state, int code) throws IOException {
		state[COMPRESS] |= (code << state[LEN]);
		state[LEN]+=state[WORD_SIZE];

		while (state[LEN] >= 16) {
			baos.write((state[COMPRESS] & 0x00FF));
			baos.write((state[COMPRESS] & 0xFF00) >> 8);
			state[COMPRESS]>>=16;
			state[LEN]-=16;
		}
	}
	public static byte[] compress(byte[] data, int code_size) throws IOException {

		ByteArrayHash hash = new ByteArrayHash();
		// Initialize the table with all root values
		final int
			TWO_POW_CODE_SIZE = (int) Math.pow(2,code_size),
			clear_code = TWO_POW_CODE_SIZE,
			end_of_stream_code = clear_code + 1,
			initial_code_index = clear_code + 2,
			initial_word_size = code_size + 1,
			last_bytes = data.length - 4095;


		ByteArrayOutputStream baos = new ByteArrayOutputStream();


		int[] state = new int[3]; // compress, wordsize, len

		state[LEN] = state[COMPRESS] = 0;
		state[WORD_SIZE] = initial_word_size;

		packBits(baos,state,clear_code);

		int codeIndex = initial_code_index;
		int word_size_bump = (int) Math.pow(2,state[WORD_SIZE]) - 1;

		byte[] current_prefix = new byte[0];

		for (int i=0;i<data.length;i++) {
			byte ch = data[i];
			if (current_prefix.length == 0) {
				current_prefix = new byte[] { ch };
			} else {
				byte[] current_string = append(current_prefix,ch);

				if (hash.get(current_string) != -1) {
					current_prefix = current_string;
				} else {

					int code = current_prefix.length == 1?current_prefix[0] & 0xFF:hash.get(current_prefix);

					packBits(baos,state,code);

					if (codeIndex == 4095) {
						// No more entries can fit,
						// Clear the table;
						packBits(baos,state,clear_code);
						// reset word size
						state[WORD_SIZE] = initial_word_size;
						codeIndex = initial_code_index;
						hash.clear();
						word_size_bump = (int) Math.pow(2,state[WORD_SIZE]) - 1;
					} else {
						if (codeIndex<4095 && codeIndex > word_size_bump) { // we bump up the word size when we reach (2^word_size) - 1
							state[WORD_SIZE]++;
							word_size_bump = (int) Math.pow(2,state[WORD_SIZE]) - 1;
						}
						hash.put(current_string,codeIndex++);
					}
					current_prefix = new byte[] { ch };

				}


			}

		}
		int code = current_prefix.length == 1?current_prefix[0] & 0xFF:hash.get(current_prefix);

		packBits(baos,state,code);

		hash.clear();
		hash = null;

		packBits(baos,state,end_of_stream_code);
		while (state[LEN] > 0) {
			baos.write((state[COMPRESS] & 0xFF));
			state[COMPRESS]>>=8;
			state[LEN]-=8;
		}
		byte[] actual = baos.toByteArray();
		baos.reset();
		return actual;
	}
	public static byte[] decompress(byte[] data, int code_size) throws IOException {

		final int
			TWO_POW_CODE_SIZE = (int) Math.pow(2,code_size),
			clear_code = TWO_POW_CODE_SIZE,
			end_of_stream_code = clear_code + 1,
			initial_code_index = clear_code + 2,
			initial_word_size = code_size + 1;

		Object[] table = new Object[4096];
		for (int i=TWO_POW_CODE_SIZE;--i>=0;)
			table[i] = new byte[] { (byte) i };

		int code_index = initial_code_index;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int word_size = initial_word_size;
		int word_size_bump = (int) Math.pow(2,word_size) - 1;

		int index = 0;

		int decompress = 0, len = 0;

		boolean first = true;
		byte[] old_byte = new byte[0];

		while (index < data.length) {
			decompress |= ((data[index++] & 0xFF) << len);
			len+=8;
			while (len >= word_size) {
				int code = decompress & word_size_bump;
				decompress>>=word_size;
				len-=word_size;

				if (code == clear_code) {
					word_size = initial_word_size;
					word_size_bump = (int) Math.pow(2,word_size) - 1;
					code_index = initial_code_index;
					for (int i=code_index;i<table.length;i++)
						table[i] = null;
					first = true;
				} else if (code == end_of_stream_code) {
					if (index<data.length)
						throw new IOException("Warning: End of stream reached but more data available.");
					break;
				} else {
					if (first) {
						old_byte = (byte[]) table[code];
						baos.write(old_byte);
						first = false;
					} else {
						if (code < 4096) {
							byte[] lookup = (byte[]) table[code];

							if (lookup == null)
								lookup = append(old_byte,old_byte[0]);

							baos.write(lookup);
							if (code_index<4096) {
								table[code_index++] = append(old_byte,lookup[0]);
								if (code_index > word_size_bump && word_size < 12) {
									word_size++;
									word_size_bump = (int) Math.pow(2,word_size) - 1;
								}

							}
							old_byte = lookup;
						} else {
							throw new RuntimeException("Decode warning. Missed Clear Code? Data Corrupt? - Code: " + code + " bin: " + Util.binCode(code,word_size) + " minCodeSize: " + initial_word_size + " at: " + index);
						}
					}
				}
			}
		}

		byte[] actual = baos.toByteArray();
		baos.reset();
		baos = null;
		return actual;

	}
	public static void main(String[] args) throws IOException {
		byte[] initial_table = new byte[] {
			(byte) 'A',
			(byte) 'B',
			(byte) 'C',
			(byte) 'D',
		};
		byte[] data = new byte[] {
			(byte) 'A',
			(byte) 'A',
			(byte) 'A',
			(byte) 'A',
			(byte) 'A',
			(byte) 'A',
			(byte) 'A',
			(byte) 'B',
			(byte) 'B',
			(byte) 'B',
			(byte) 'B',
			(byte) 'B',
			(byte) 'B',
			(byte) 'B',
		};
		byte[] translated = new byte[data.length];
		for (int i=0;i<data.length;i++) {
			for (int y=0;y<initial_table.length;y++) {
				if (initial_table[y] == data[i]) {
					translated[i] = (byte) y;
					break;
				}
			}
		}

		int code_size = 2;
		byte[] compressed = compress(translated,code_size);
		debug("Compressed to: " + compressed.length + "b was " + translated.length + "b compressed size: " + (100f * compressed.length/translated.length ) + "%");
		int c = 0, len = 0, x = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i=0;i<compressed.length;i++) {
			sb2.append(Util.binCode(compressed[i],8));
			sb2.append('\n');

		}
		debug(sb.reverse().toString());

		debug("Bytes:\n" + sb2.toString());
		debug("Decompressing...");
		byte[] decompressed = decompress(compressed,code_size);

		byte[] detranslated = new byte[decompressed.length];
		for (int i=0;i<decompressed.length;i++) {
			for (int y=0;y<initial_table.length;y++) {
				if (y == decompressed[i]) {
					detranslated[i] = initial_table[y];
					break;
				}
			}
		}

		for (int i=0;i<detranslated.length;i++) {
			debug((char) detranslated[i] + " ");
		}

		boolean equal = data.length == detranslated.length;
		for (int i=data.length;equal&&--i>=0;) {
			equal = data[i] == detranslated[i];
			if (!equal)
				debug("At pos " + i + " " + data[i] + "/" + detranslated[i]);
		}
		debug("Worked? " + equal + " " + data.length + "/" + detranslated.length);


	}

	private static final void debug ( String s ) {
	}

}