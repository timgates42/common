package com.tim.image.gif;

public class ByteArrayHash {

	Object[] hash_keys;
	int[] hash_values;
	int elements;
	int collisions;

	public static final int ARRAY_SIZE = 17491;
	public static final int MOD_PRIME = 17489;
	public static final int KEY_PRIME = 9091;

	public ByteArrayHash() {
		hash_keys = new Object[ARRAY_SIZE];
		hash_values = new int[ARRAY_SIZE];
		for( int i = 0; i < ARRAY_SIZE; i++ )
			hash_values[i] = -1;
	}

	public void put( byte[] hash_key, int hash_value ) {
		int hash_pos = hashPos( hash_key );
		while( hash_values[hash_pos] != -1 ) {
			collisions++;
			hash_pos = ++hash_pos % ARRAY_SIZE;
		}
		hash_keys[hash_pos] = hash_key;
		hash_values[hash_pos] = hash_value;
		elements++;
	}

	public void clear() {
		debug( "Table cleared, num collisions " + collisions );
		//		hash_keys = new Object[ARRAY_SIZE];
		//		hash_values = new int[ARRAY_SIZE];
		//		System.gc();
		for( int i = 0; i < ARRAY_SIZE; i++ )
			hash_values[i] = -1;
		elements = 0;
		collisions = 0;
	}

	public int get( byte[] hash_key ) {
		int hash_pos = hashPos( hash_key );
		int val;
		while( ( val = hash_values[hash_pos] ) != -1 && !equals( (byte[]) hash_keys[hash_pos], hash_key ) )
			hash_pos = ++hash_pos % ARRAY_SIZE;
		return val;
	}

	public boolean equals( byte[] a1, byte[] a2 ) {
		boolean equal = a1 != null && ( a1.length == a2.length );
		for( int i = a2.length; equal && --i >= 0; )
			equal = a1[i] == a2[i];
		return equal;
	}

	public int hashPos( byte[] hash_key ) {
		long x = hash_key.length * KEY_PRIME;
		for( int i = 0; i < hash_key.length; i++ )
			x ^= ( (long) ( hash_key[i] & 0xFF ) ) << ( ( 8 * i ) % 51 );
		return (int) ( x % MOD_PRIME );
	}

	public void dispose() {
		if( hash_keys != null )
			clear();
		hash_keys = null;
		hash_values = null;
	}

	protected void debug( String msg ) {
		//System.out.println( "[ByteArrayHashtable] " + msg );
	}

}