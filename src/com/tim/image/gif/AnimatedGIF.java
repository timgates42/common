package com.tim.image.gif;

import java.util.Vector;

public class AnimatedGIF {

	private Vector images;
	private int width;
	private int height;
	private long period;

	public AnimatedGIF( int width, int height ) {
		this.width = width;
		this.height = height;
		images = new Vector();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int numImages() {
		return images.size();
	}

	public long getPeriod() {
		return period;
	}

	public void addImage( int[] data, long delay ) {
		images.addElement( new ImageTimer( data, delay ) );
		period += delay;
	}

	public int[] getImage( int index ) {
		return ( (ImageTimer) images.elementAt( index ) ).getDate();
	}

	public long getDelay( int index ) {
		return ( (ImageTimer) images.elementAt( index ) ).getDelay();
	}

	private static final class ImageTimer {

		private int[] date;
		private long delay;

		public ImageTimer( int[] date, long delay ) {
			this.date = date;
			this.delay = delay;
		}

		public int[] getDate() {
			return date;
		}

		public long getDelay() {
			return delay;
		}

	}

}

