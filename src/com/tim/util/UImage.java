/*
 * Created by IntelliJ IDEA.
 * User: Timothy Gates
 * Date: Oct 19, 2001
 * Time: 9:08:18 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.tim.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Date;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Dimension;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.MetadataException;
import com.drew.imaging.jpeg.JpegMetadataReader;


public class UImage {

	public static final int NONE = 0;
	public static final int RIGHT = 1;
	public static final int LEFT = 2;
	public static final int UPSIDEDOWN = 3;

	private File input_file;
	private int xsize = -1;
	private int ysize = -1;
	private int rotation;
    private boolean loaded;
    private Metadata metadata;
    private JPEGImageDecoder jpegDecoder;
    private BufferedImage image; 
    private JPEGDecodeParam decodeParam;

	public UImage( File input_file ) {
        assert(input_file != null);
		this.input_file = input_file;
	}
    
    public UImage( Image image ) {
        assert(image != null);
        this.image = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        this.image.getGraphics().drawImage(image, 0, 0, null);
    }
    
    public Image getImage() throws IOException {
        ensureLoaded();
        return image;
    }

	public void setSize( int xsize, int ysize ) {
		this.xsize = xsize;
		this.ysize = ysize;
	}

	public void setRotation( int rotation ) {
		this.rotation = rotation;
	}

	public void setRotation( String rotation ) {
        assert(rotation != null);
		if( rotation.equalsIgnoreCase( "left" ) ) {
			setRotation( LEFT );
		} else if( rotation.equalsIgnoreCase( "right" ) ) {
			setRotation( RIGHT );
		} else if( rotation.equalsIgnoreCase( "upsidedown" ) ) {
			setRotation( UPSIDEDOWN );
		} else {
			setRotation( NONE );
		}
	}
    
    /**
     * Rather than using the more generic SPI methods to access the JPEG 
     * Encode/Decoder this uses the direct access to transfer the EXIF and
     * IPTC Data the image may have.
     * Exif and Iptc are some interesting app data found in jpegs taken by
     * digital cameras and well worth keeping. Most importantly is the date
     * the picture was taken.
     */
    public JPEGEncodeParam transferExifIptc(JPEGImageDecoder jpegDecoder, JPEGDecodeParam decodeParam, JPEGImageEncoder jpegEncoder, BufferedImage target) {
        assert(jpegDecoder != null && decodeParam != null && jpegEncoder != null && target != null);
        JPEGEncodeParam encodeParam = jpegEncoder.getDefaultJPEGEncodeParam(target);
        byte[][] exifSegment = decodeParam.getMarkerData(JPEGDecodeParam.APP1_MARKER);
        if (exifSegment != null && exifSegment[0].length>0) {
            encodeParam.addMarkerData(JPEGEncodeParam.APP1_MARKER, exifSegment[0]);
        }
        byte[][] iptcSegment = decodeParam.getMarkerData(JPEGDecodeParam.APPD_MARKER);
        if (iptcSegment != null && iptcSegment[0].length>0) {
            encodeParam.addMarkerData(JPEGEncodeParam.APPD_MARKER, iptcSegment[0]);
        }
        return encodeParam;
    }
    
    private void ensureLoaded() throws IOException {
        if(!loaded) {
            if(input_file != null) {
                FileInputStream input = new FileInputStream( input_file );
                jpegDecoder = JPEGCodec.createJPEGDecoder(input);
                image = jpegDecoder.decodeAsBufferedImage();
                decodeParam = jpegDecoder.getJPEGDecodeParam();
                metadata = JpegMetadataReader.readMetadata(decodeParam);
                input.close();
            }
            loaded = true;
        }
        System.out.println("post ensureLoaded " + loaded + " " + input_file + " " + image);
    }
    
    public Date getDate() throws IOException {
         try {
             ensureLoaded();
             if(metadata == null) {
                 return null;
             }
             Directory dir = metadata.getDirectory(ExifDirectory.class);
             return dir.getDate(ExifDirectory.TAG_DATETIME);
         } catch(MetadataException mde) {
             return null;
         }
    }
    
    public void flush() {   
        if(loaded) {
            jpegDecoder = null;
            if(image != null) {
                try { image.flush(); } catch(Exception e) {}
                image = null;
            }
            decodeParam = null;
            loaded = false;
        }
    }

    private Dimension getRotationSize(int xsize, int ysize) {
		if( rotation == LEFT || rotation == RIGHT) {
            return new Dimension(ysize, xsize);
		} else {
            return new Dimension(xsize, ysize);
		}
    }
    
    private Dimension getTransformSize(Dimension oldsize) {
		if ( xsize == -1) {
			xsize = oldsize.width;
		}
		if ( ysize == -1 ) {
			ysize = oldsize.height;
		}
        Dimension rsize = getRotationSize(xsize, ysize);
		int new_width = Math.min( rsize.width, oldsize.width * rsize.height / oldsize.height );
		int new_height = Math.min( rsize.height, oldsize.height * rsize.width / oldsize.width );
        return new Dimension(new_width, new_height);
    }
    
    private BufferedImage transform(Dimension resize, Dimension rotsize, Dimension oldsize) {
        BufferedImage new_image = new BufferedImage( rotsize.width, rotsize.height, BufferedImage.TYPE_INT_RGB );
        for( int y = 0; y < rotsize.height; y++ ) {
            for( int x = 0; x < rotsize.width; x++ ) {
                int rx = x;
                int ry = y;
                if( rotation == LEFT ) {
                    rx = rotsize.height - y - 1;
                    ry = x;
                } else if( rotation == RIGHT ) {
                    rx = y;
                    ry = rotsize.width - x - 1;
                } else if( rotation == UPSIDEDOWN ) {
                    rx = rotsize.width - x - 1;
                    ry = rotsize.height - y - 1;
                }
                double min_x = ( (double) ( rx * oldsize.width ) ) / ( (double) resize.width );
                min_x = Math.max( 0, Math.min( oldsize.width - 1, min_x ) );
                double max_x = ( (double) ( ( rx + 1 ) * oldsize.width ) ) / ( (double) resize.width );
                max_x = Math.min( oldsize.width, Math.max( max_x, min_x + 1 ) );
                double min_y = ( (double) ( ry * oldsize.height ) ) / ( (double) resize.height );
                min_y = Math.max( 0, Math.min( oldsize.height - 1, min_y ) );
                double max_y = ( (double) ( ( ry + 1 ) * oldsize.height ) ) / ( (double) resize.height );
                max_y = Math.min( oldsize.height, Math.max( max_y, min_y + 1 ) );
                double red = 0;
                double blue = 0;
                double green = 0;
                double count = 0;
                for( int x1 = (int) min_x; x1 < max_x; x1++ ) {
                    for( int y1 = (int) min_y; y1 < max_y; y1++ ) {
                        double x_portion = 1;
                        if( x1 < min_x ) {
                            x_portion = x1 + 1 - min_x;
                        } else if( x1 + 1 > max_x ) {
                            x_portion = max_x - x1;
                        }
                        double y_portion = 1;
                        if( y1 < min_y ) {
                            y_portion = y1 + 1 - min_y;
                        } else if( y1 + 1 > max_y ) {
                            y_portion = max_y - y1;
                        }
                        int rgb = image.getRGB( x1, y1 );
                        double portion = x_portion * y_portion;
                        count += portion;
                        red += ( ( rgb >> 16 ) & 0xFF ) * portion;
                        green += ( ( rgb >> 8 ) & 0xFF ) * portion;
                        blue += ( rgb & 0xFF ) * portion;
                    }
                }
                red /= count;
                green /= count;
                blue /= count;
                int rgb = ( (int) red ) << 16 | ( (int) green ) << 8 | (int) blue;
                new_image.setRGB( x, y, rgb );
            }
        }
        return new_image;
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
    }

	public void save( File output_file ) throws IOException {
        assert(output_file != null);
        if(output_file.exists()) {
            throw new IOException(output_file + " exists.");
        }
        BufferedImage imageout = saveImageCommon();
		FileOutputStream output = new FileOutputStream( output_file );
        JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(output);
        JPEGEncodeParam encodeParam;
        if(jpegDecoder != null && decodeParam != null) {
            encodeParam = transferExifIptc(jpegDecoder, decodeParam, jpegEncoder, imageout);
        } else {
            encodeParam = jpegEncoder.getDefaultJPEGEncodeParam(imageout);
        }
        jpegEncoder.encode(imageout, encodeParam);
        imageout.flush();
		output.close();
    }

	public void setImage(BufferedImage image) {
            this.image = image;
        }

    
	public BufferedImage saveImage() {
        try {
            return saveImageCommon();
        } catch (IOException ioe) {
            System.err.println(ioe);
            return null;
        }
    }
    
	public BufferedImage saveImageCommon() throws IOException {
        ensureLoaded();
		Dimension oldsize = new Dimension(image.getWidth(), image.getHeight());
        Dimension resize = getTransformSize(oldsize);
        Dimension rotsize = getRotationSize(resize.width, resize.height);
        BufferedImage imageout;
        if(rotation != NONE || oldsize.width != resize.width || oldsize.height != resize.height) {
            imageout = transform(resize, rotsize, oldsize);
        } else {
            imageout = new BufferedImage(oldsize.width, oldsize.height, BufferedImage.TYPE_INT_RGB);
            imageout.getGraphics().drawImage(image, 0, 0, null);
        }
        return imageout;
	}
    
    public static void main(String[] args) throws IOException {
        UImage test = new UImage(new File(args[0]));
        test.setRotation(RIGHT);
        test.setSize(800,600);
        test.save(new File(args[1]));
    }

    public int getRGB(int x, int y) {
        try { ensureLoaded(); } catch(IOException ioe) { return -1; }
        return image.getRGB( x, y );
    }
    
    public int getWidth() {
        try { ensureLoaded(); } catch(IOException ioe) { return -1; }
        return image.getWidth();
    }
    
    public int getHeight() {
        try { ensureLoaded(); } catch(IOException ioe) { return -1; }
        return image.getHeight();
    }

}
