import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//We use these packages to output the image as a file
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import javax.swing.*;

import java.io.FileNotFoundException;

public class Project1 
{
	//the variable we will use to help cycle through images
    static int rolling = 0;
    //our matrix length and width
	static int M = 4;
	static int[][] ditherMatrix = {{0,8,2,10},{12,4,14,6},{3,11,1,9},{15,7,13,5}};
	
	public static int toUnsignedInt(int x)
	{
		return x & 0xff;
	}
	
	/**
	 * Doing simple matrix multiplication using actual multiplication instead of bitwise operations.
	 * 
	 * @param gbr RGB values in the order of GBR 
	 * @param matrix The matrix we multiply to get our YoCoCg matrix values
	 * @return returns the CgYCo values.
	 */
	public static int[] matrixMultiplication(int[] gbr, double[][] matrix)
	{
		int[] product = new int[3];
		for (int i = 0; i < matrix.length; i++)
		{
			int productTotal = 0;
			for (int j = 0; j < matrix[i].length; j++)
			{
				productTotal += gbr[j] * matrix[i][j];
			}
			product[i] = productTotal;
		}
		return product;
	}
	
	/**
	 * Gonna use this matrix at first with multiplication, but then we will implement another version of this exact formula but with bitwise 
	 * calculations to test if there are any significant time differences.
	 * 
	 * @param gbr The gbr values that need to be converted
	 * @return returns the CgYCo values we converted in the function
	 */
	public static int[] convertToYUVMatrix(int[] rgb)
	{
		int[] YUV = new int[3];
		double[][] matrix = {{0.299,0.587,0.114},{-0.147135,-0.28886,0.436},{0.615,-0.51499,-0.10001}};
		YUV = matrixMultiplication(rgb, matrix);
		return YUV;
	}
	
	public static byte[] convertToGrayScale(byte[] image, int pWidth, int pHeight, int[] stripOffsets, int[] stripByteCount, int samplesPer)
	{
		byte grayScaleImage[] = new byte[(pWidth * pHeight) + 10 + (7*12)];
		//first we define the headers
		grayScaleImage[0] = 77;
		grayScaleImage[1] = 77;
		grayScaleImage[2] = 0;
		grayScaleImage[3] = 42;
		grayScaleImage[4] = 0;
		grayScaleImage[5] = 0;
		grayScaleImage[6] = 0;
		grayScaleImage[7] = 8;
		int offsetIFD = 8;
		int newOffset = 10 + (7*12);
		//then grab the RGB values from the tif byte array and convert to YUV. After converting 
		//we simply use the Y value as our gray level intensity.
		int R = 0;
		int G = 1;
		int B = 2;
		int rgb[] = new int[3];
		int yuv[] = new int[3];
		for(int i = 0; i < stripOffsets.length; i++)
		{
			int stripOffset = stripOffsets[i];
			int stripLength = stripByteCount[i]/samplesPer;
			for(int k = 0; k<stripLength; k++)
			{
				rgb[R] = toUnsignedInt(image[stripOffset+(k*samplesPer)]);
				rgb[G] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 1]);
				rgb[B] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 2]);
				yuv = convertToYUVMatrix(rgb);
				grayScaleImage[newOffset] = (byte)yuv[0];
				newOffset += 1;
			}
		}
		//Set IFD entries
		grayScaleImage[offsetIFD] = 0;
		grayScaleImage[offsetIFD+1] = 7;
		offsetIFD += 2;
		//set width
		int k = 0;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 0;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 3;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = (byte)(pWidth/256);
		grayScaleImage[offsetIFD + (k*12) + 9] = (byte)(pWidth%256);
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = 0;
		//set height
		k++;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 1;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 3;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = (byte)(pHeight/256);
		grayScaleImage[offsetIFD + (k*12) + 9] = (byte)(pHeight%256);
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = 0;
		//set bitsPerSample
		k++;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 2;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 3;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = 0;
		grayScaleImage[offsetIFD + (k*12) + 9] = 8;
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = 0;
		//set compression
		k++;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 3;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 3;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = 0;
		grayScaleImage[offsetIFD + (k*12) + 9] = 1;
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = 0;
		//set PhotometricInterpretation
		k++;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 6;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 3;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = 0;
		grayScaleImage[offsetIFD + (k*12) + 9] = 1;
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = 0;
		//set StripOffsets
		k++;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 17;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 4;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = 0;
		grayScaleImage[offsetIFD + (k*12) + 9] = 0;
		grayScaleImage[offsetIFD + (k*12) + 10] = 0;
		grayScaleImage[offsetIFD + (k*12) + 11] = (byte)(10 + (7*12));
		//set StripByteCounts
		k++;
		int byteCount = pWidth *pHeight;
		grayScaleImage[offsetIFD + (k*12)] = 1;
		grayScaleImage[offsetIFD + (k*12) + 1] = 23;
		grayScaleImage[offsetIFD + (k*12) + 2] = 0;
		grayScaleImage[offsetIFD + (k*12) + 3] = 4;
		grayScaleImage[offsetIFD + (k*12) + 4] = 0;
		grayScaleImage[offsetIFD + (k*12) + 5] = 0;
		grayScaleImage[offsetIFD + (k*12) + 6] = 0;
		grayScaleImage[offsetIFD + (k*12) + 7] = 1;
		grayScaleImage[offsetIFD + (k*12) + 8] = 0;
		grayScaleImage[offsetIFD + (k*12) + 9] = (byte)(byteCount/Math.pow(2, 16));
		grayScaleImage[offsetIFD + (k*12) + 10] = (byte)(byteCount/Math.pow(2, 8));
		grayScaleImage[offsetIFD + (k*12) + 11] = (byte)(byteCount%256);
		return grayScaleImage;
	}
	
	public static int getIntensity(int val)
	{
		return (int)(((double)val/255)*(M*M));
	}
	
	/**
	 * Returns the ordered dithering for the specific section of the picture that is given.
	 * 
	 * @param pixelValue The section of the image to be order dithered
	 * @return returns the resulting matrix after going through order dithering
	 */
	public static byte orderedDithering(int hMult, int wMult, int picture, int[][] dith)
	{
		if(getIntensity(picture) >= dith[hMult][wMult])
			{
				return (byte)255;
			}
			else
			{
				return 0;
			}
	}
	
	public static byte[] convertToDithered(byte[] image, int pWidth, int pHeight, int[] stripOffsets, int[] stripByteCount, int samplesPer)
	{
		byte[] ditheredImage = new byte[(pWidth * pHeight) + 10 + (7*12)];
		//first we define the headers
		ditheredImage[0] = 77;
		ditheredImage[1] = 77;
		ditheredImage[2] = 0;
		ditheredImage[3] = 42;
		ditheredImage[4] = 0;
		ditheredImage[5] = 0;
		ditheredImage[6] = 0;
		ditheredImage[7] = 8;
		int offsetIFD = 8;
		int newOffset = 10 + (7*12);
		int pixel = 0;
		
		int R = 0;
		int G = 1;
		int B = 2;
		int rgb[] = new int[3];
		int yuv[] = new int[3];
		//given our dither matrix, we simply grab the modulus of the current pixel to find w
		for(int i = 0; i < stripOffsets.length; i++)
		{
			int stripOffset = stripOffsets[i];
			int stripLength = stripByteCount[i]/samplesPer;
			for(int k = 0; k<stripLength; k++)
			{
				rgb[R] = toUnsignedInt(image[stripOffset+(k*samplesPer)]);
				rgb[G] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 1]);
				rgb[B] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 2]);
				yuv = convertToYUVMatrix(rgb);
				ditheredImage[newOffset] = orderedDithering(((pixel/512)%4), ((pixel%4)), yuv[0], ditherMatrix);
				newOffset += 1;
				pixel++;
			}
		}
		
		//Set IFD entries
		ditheredImage[offsetIFD] = 0;
		ditheredImage[offsetIFD+1] = 7;
		offsetIFD += 2;
		//set width
		int k = 0;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 0;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 3;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = (byte)(pWidth/256);
		ditheredImage[offsetIFD + (k*12) + 9] = (byte)(pWidth%256);
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = 0;
		//set height
		k++;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 1;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 3;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = (byte)(pHeight/256);
		ditheredImage[offsetIFD + (k*12) + 9] = (byte)(pHeight%256);
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = 0;
		//set bitsPerSample
		k++;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 2;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 3;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = 0;
		ditheredImage[offsetIFD + (k*12) + 9] = 8;
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = 0;
		//set compression
		k++;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 3;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 3;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = 0;
		ditheredImage[offsetIFD + (k*12) + 9] = 1;
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = 0;
		//set PhotometricInterpretation
		k++;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 6;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 3;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = 0;
		ditheredImage[offsetIFD + (k*12) + 9] = 1;
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = 0;
		//set StripOffsets
		k++;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 17;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 4;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = 0;
		ditheredImage[offsetIFD + (k*12) + 9] = 0;
		ditheredImage[offsetIFD + (k*12) + 10] = 0;
		ditheredImage[offsetIFD + (k*12) + 11] = (byte)(10 + (7*12));
		//set StripByteCounts
		k++;
		int byteCount = pWidth *pHeight;
		ditheredImage[offsetIFD + (k*12)] = 1;
		ditheredImage[offsetIFD + (k*12) + 1] = 23;
		ditheredImage[offsetIFD + (k*12) + 2] = 0;
		ditheredImage[offsetIFD + (k*12) + 3] = 4;
		ditheredImage[offsetIFD + (k*12) + 4] = 0;
		ditheredImage[offsetIFD + (k*12) + 5] = 0;
		ditheredImage[offsetIFD + (k*12) + 6] = 0;
		ditheredImage[offsetIFD + (k*12) + 7] = 1;
		ditheredImage[offsetIFD + (k*12) + 8] = 0;
		ditheredImage[offsetIFD + (k*12) + 9] = (byte)(byteCount/Math.pow(2, 16));
		ditheredImage[offsetIFD + (k*12) + 10] = (byte)(byteCount/Math.pow(2, 8));
		ditheredImage[offsetIFD + (k*12) + 11] = (byte)(byteCount%256);
		return ditheredImage;
	}
	
	public static int[] convertToRGBMatrix(int[] yuv)
	{
		int[] rgb = new int[3];
		double[][] matrix = {{1,0,1.13983},{1,-0.39465,-0.58060},{1,2.03211,0}};
		rgb = matrixMultiplication(yuv, matrix);
		return rgb;
	}
	
	public static byte[] convertDynamicRange(byte[] image, int pWidth, int pHeight, int[] stripOffsets, int[] stripByteCount, int samplesPer)
	{
		
		byte[] rangeAdjustedImage = new byte[((pWidth * pHeight) * 3) + 10 + (8*12)];
		//first we define the headers
		rangeAdjustedImage[0] = 77;
		rangeAdjustedImage[1] = 77;
		rangeAdjustedImage[2] = 0;
		rangeAdjustedImage[3] = 42;
		rangeAdjustedImage[4] = 0;
		rangeAdjustedImage[5] = 0;
		rangeAdjustedImage[6] = 0;
		rangeAdjustedImage[7] = 8;
		int pixels = pWidth * pHeight;
		int offsetIFD = 8;
		int newOffset = 10 + (8*12);
		
		int R = 0;
		int G = 1;
		int B = 2;
		int rgb[] = new int[3];
		int yuv[] = new int[3];		
		//define the threshold (or possibly 2 thresholds if we want to bound for parts that are too bright)
		double threshold = (double)50/255;
		
		for(int i = 0; i < stripOffsets.length; i++)
		{
			int stripOffset = stripOffsets[i];
			int stripLength = stripByteCount[i]/samplesPer;
			for(int k = 0; k<stripLength; k++)
			{
				rgb[R] = toUnsignedInt(image[stripOffset+(k*samplesPer)]);
				rgb[G] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 1]);
				rgb[B] = toUnsignedInt(image[stripOffset+(k*samplesPer) + 2]);
				yuv = convertToYUVMatrix(rgb);
				if(((double)yuv[0]/255) < threshold)
				{
					yuv[0] += (int)(threshold * 255);
					rgb = convertToRGBMatrix(yuv);
				}
				rangeAdjustedImage[newOffset] = (byte)rgb[R];
				rangeAdjustedImage[newOffset + 1] = (byte)rgb[G];
				rangeAdjustedImage[newOffset + 2] = (byte)rgb[B];
				newOffset += 3;
			}
		}
		
		//Set IFD entries
		rangeAdjustedImage[offsetIFD] = 0;
		rangeAdjustedImage[offsetIFD+1] = 8;
		offsetIFD += 2;
		//set width
		int k = 0;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = (byte)(pWidth/256);
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = (byte)(pWidth%256);
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set height
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = (byte)(pHeight/256);
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = (byte)(pHeight%256);
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set bitsPerSample
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 2;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = 8;
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set compression
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set PhotometricInterpretation
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 6;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = 2;
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set StripOffsets
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 17;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 4;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = (byte)10 + (8*12);
		//set SamplesPerPixel
		k++;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 21;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = 3;
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = 0;
		//set StripByteCounts
		k++;
		int byteCount = 3 * pixels;
		rangeAdjustedImage[offsetIFD + (k*12)] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 1] = 23;
		rangeAdjustedImage[offsetIFD + (k*12) + 2] = 0 ;
		rangeAdjustedImage[offsetIFD + (k*12) + 3] = 4;
		rangeAdjustedImage[offsetIFD + (k*12) + 4] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 5] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 6] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 7] = 1;
		rangeAdjustedImage[offsetIFD + (k*12) + 8] = 0;
		rangeAdjustedImage[offsetIFD + (k*12) + 9] = (byte)(byteCount/Math.pow(2, 16));
		rangeAdjustedImage[offsetIFD + (k*12) + 10] = (byte)(byteCount/Math.pow(2, 8));
		rangeAdjustedImage[offsetIFD + (k*12) + 11] = (byte)(byteCount%256);
		return rangeAdjustedImage;
	}
	
	public static byte[] reverse(byte[] array)
	{
		byte[] reversed = new byte[array.length];
		for(int i = 0; i < array.length; i++)
		{
			reversed[array.length-1-i] = array[i];
		}
		return reversed;
	}
	
	public static void main(String[] args)
	{
		//the dimension of the image we'll be dealing with
		int pWidth = 1;
		int pHeight = 1;
		//The representation of our values (B/W grayscale or RGB)
		String reptn = "";
		//our bits per sample (the number per R, G or B value in RGB for example.)
		int bitsPerSample = 1;
		//The compression type of the file
		String compression = "uncompressed";
		int[] stripOffSet = {};
		int samplesPerPixel = 0;
		int planarConfiguration = 0;
		int[] stripByteCount = {};
		int numIFD = 0;
		

		try
		{
			File displayImage = new File("lena.tif");
	        JFrame frame = new JFrame("Image");
	        JPanel panel = new JPanel();
			JFileChooser fc = new JFileChooser();
			JLabel imageLabel = new JLabel();
			int returnValue = fc.showOpenDialog(frame);
			if(returnValue == JFileChooser.APPROVE_OPTION)
			{
				displayImage = fc.getSelectedFile();
				BufferedImage image = ImageIO.read(displayImage);
		        ImageIcon imageIcon = new ImageIcon(image);
		        imageLabel.setIcon(imageIcon);
	            panel.add(imageLabel);
			}
			
			//File tifImage = new File("G:/EclipseWorkspace/CMPT365Project1/src/n3509.tif");
			FileInputStream fileStream = new FileInputStream(displayImage);
			byte bytes[] = new byte[(int)displayImage.length()];
			int numOfBytes = fileStream.read(bytes);
			//The first 2 bytes of the file determines where the most significant bytes start (ascending or descending)
			//Descending = 77 Ascending = 74 in integer values
			boolean descending = bytes[0] == 73 && bytes[1] == 73;;
			byte[] tmpFour = new byte[4];
			byte[] tmpTwo = new byte[2];
			byte[] tmp;
			if(descending)
			{
				//reverse header
				tmpFour[0] = bytes[4];
				tmpFour[1] = bytes[5];
				tmpFour[2] = bytes[6];
				tmpFour[3] = bytes[7];
				tmp = reverse(tmpFour);
				bytes[4] = tmp[0];
				bytes[5] = tmp[1];
				bytes[6] = tmp[2];
				bytes[7] = tmp[3];
				
			}
			System.out.println(bytes[0] + " " + bytes[1] + " " + bytes[2] + " " + bytes[3]);
			System.out.println(bytes[4] + " " + bytes[5] + " " + bytes[6] + " " + bytes[7]);
			System.out.println("Our total byte length is: " + bytes.length);
			//We then find the IFD offset by taking the next 4 bytes
			numIFD = ((((toUnsignedInt(bytes[4]) << 8) + toUnsignedInt(bytes[5])) << 8) + (toUnsignedInt(bytes[6])) << 8) + toUnsignedInt(bytes[7]);
			System.out.println("IFD offset is: " + numIFD);
			//The first 2 bytes tells us how many IFD entries there are
			if(descending)
			{
				tmpTwo[0] = bytes[numIFD];
				tmpTwo[1] = bytes[numIFD+1];
				tmp = reverse(tmpTwo);
				bytes[numIFD] = tmp[0];
				bytes[numIFD+1] = tmp[1];
			}
			int ifdCount = ((toUnsignedInt(bytes[numIFD]) << 8) + toUnsignedInt(bytes[numIFD+1]));
			numIFD += 2;
			System.out.println("We have " + ifdCount + " IFD entries\n");
			System.out.println("Our integer values of each IFD entry is: ");
			//What IFD tag we're looking at
			int tag = 0;
			//the number of values that are stored
			int count = 0;
			//The different types in IFD are 1 = byte, 2 = ASCII (7bit ascii code), 3 = short, 2 byte unsigned int, 4 = long, 4byte unsigned int, 5 = rational 2 longs first numerator then denominator
			int type = 0;
			//The respective values for each of these types.
			int value[] = new int[4];
			
			if(descending)
			{
				for(int i = 0; i < ifdCount; i++)
				{
					//reverse header
					tmpTwo[0] = bytes[numIFD + (i*12)];
					tmpTwo[1] = bytes[numIFD + (i*12) + 1];
					tmp = reverse(tmpTwo);
					bytes[numIFD + (i*12)] = tmp[0];
					bytes[numIFD + (i*12) + 1] = tmp[1];
					//reverse type
					tmpTwo[0] = bytes[numIFD + (i*12) + 2];
					tmpTwo[1] = bytes[numIFD + (i*12) + 3];
					tmp = reverse(tmpTwo);
					bytes[numIFD + (i*12) + 2] = tmp[0];
					bytes[numIFD + (i*12) + 3] = tmp[1];
					//reverse count
					tmpFour[0] = bytes[numIFD + (i*12) + 4];
					tmpFour[1] = bytes[numIFD + (i*12) + 5];
					tmpFour[2] = bytes[numIFD + (i*12) + 6];
					tmpFour[3] = bytes[numIFD + (i*12) + 7];
					tmp = reverse(tmpFour);
					bytes[numIFD + (i*12) + 4] = tmp[0];
					bytes[numIFD + (i*12) + 5] = tmp[1];
					bytes[numIFD + (i*12) + 6] = tmp[2];
					bytes[numIFD + (i*12) + 7] = tmp[3];
					//reverse value/offset
					int varCount = ((((bytes[numIFD + (i*12) +4] << 8) + bytes[numIFD + (i*12) +5]) << 8) + (bytes[numIFD + (i*12) +6]) << 8) + bytes[numIFD + (i*12) +7];
					int varType = (bytes[numIFD + (i*12) + 2] << 8) + bytes[numIFD + (i*12) + 3];
					if(varCount < 2 && varType == 3)
					{
						tmpTwo[0] = bytes[numIFD + (i*12) + 8];
						tmpTwo[1] = bytes[numIFD + (i*12) + 9];
						tmp = reverse(tmpTwo);
						bytes[numIFD + (i*12) + 8] = tmp[0];
						bytes[numIFD + (i*12) + 9] = tmp[1];

					}
					else
					{
						tmpFour[0] = bytes[numIFD + (i*12) + 8];
						tmpFour[1] = bytes[numIFD + (i*12) + 9];
						tmpFour[2] = bytes[numIFD + (i*12) + 10];
						tmpFour[3] = bytes[numIFD + (i*12) + 11];
						tmp = reverse(tmpFour);
						bytes[numIFD + (i*12) + 8] = tmp[0];
						bytes[numIFD + (i*12) + 9] = tmp[1];
						bytes[numIFD + (i*12) + 10] = tmp[2];
						bytes[numIFD + (i*12) + 11] = tmp[3];
					}
				}
			}
			//print out all the tags just as numbers so we can see what we're dealing with
			for(int i = 0; i < ifdCount; i++)
			{
				for(int k = 0; k < 12; k++)
				{
					System.out.print(bytes[numIFD+ (i*12) + k] + " ");
				}
				System.out.println();
			}
			//print out all the tags and what our IFDs are and then process them to get the data we need
			for(int i = 0; i < ifdCount; i++)
			{
				tag = (bytes[numIFD + (i*12)] << 8) + toUnsignedInt(bytes[numIFD + (i*12) + 1]);
				System.out.println(tag + " is our tag value");
				type = (bytes[numIFD + (i*12) + 2] << 8) + bytes[numIFD + (i*12) + 3];
				System.out.println(type + " is our type value");
				count = ((((bytes[numIFD + (i*12) +4] << 8) + bytes[numIFD + (i*12) +5]) << 8) + (bytes[numIFD + (i*12) +6]) << 8) + toUnsignedInt(bytes[numIFD + (i*12) +7]);
				System.out.println(count + " is our count value");
				value = new int[count];
				switch(type)
				{
				case 1:
					break;
				case 2:
					break;
				case 3:
					if(count == 1)
					{
						value[0] = ((bytes[numIFD + (i*12) +8] << 8) + toUnsignedInt(bytes[numIFD + (i*12) +9]));
					}
					else
					{
						value = new int[count];
						int offset = ((((bytes[numIFD + (i*12) +8] << 8) + bytes[numIFD + (i*12) +9]) << 8) + toUnsignedInt(bytes[numIFD + (i*12) +10]) << 8) + toUnsignedInt(bytes[numIFD + (i*12) +11]);
						System.out.println("Offset: " + offset);
						for(int j = 0; j < count; j++)
						{
							if(descending)
							{
								tmpTwo[0] = bytes[offset + (j*2)];
								tmpTwo[1] = bytes[offset + (j*2) +1];
								tmp = reverse(tmpTwo);
								bytes[offset + (j*2)] = tmp[0];
								bytes[offset + (j*2) +1] = tmp[1];
							}
							value[j] = (toUnsignedInt(bytes[offset + (j*2)]) << 8) + toUnsignedInt(bytes[offset + (j*2) +1]);
						}
					}
					break;
				case 4:
					if(count == 1)
					{
						value[0] = ((((bytes[numIFD + (i*12) +8] << 8) + bytes[numIFD + (i*12) +9]) << 8) + (bytes[numIFD + (i*12) +10]) << 8) +toUnsignedInt(bytes[numIFD + (i*12) +11]);
					}
					else
					{
						value = new int[count];
						int offset = ((((bytes[numIFD + (i*12) +8] << 8) + bytes[numIFD + (i*12) +9]) << 8) + (bytes[numIFD + (i*12) +10]) << 8) + toUnsignedInt(bytes[numIFD + (i*12) +11]);
						System.out.println("Offset: " + offset);
						for(int j = 0; j < count; j++)
						{
							if(descending)
							{
								tmpFour[0] = bytes[offset + (j*4)];
								tmpFour[1] = bytes[offset + (j*4) +1];
								tmpFour[2] = bytes[offset + (j*4) +2];
								tmpFour[3] = bytes[offset + (j*4) +3];
								tmp = reverse(tmpFour);
								bytes[offset + (j*4)] = tmp[0];
								bytes[offset + (j*4) +1] = tmp[1];
								bytes[offset + (j*4) +2] = tmp[2];
								bytes[offset + (j*4) +3] = tmp[3];
							}
							value[j] =  ((((bytes[offset + (j*4)] << 8) + toUnsignedInt(bytes[offset + (j*4) +1])) << 8) + (toUnsignedInt(bytes[offset + (j*4) +2])) << 8) + toUnsignedInt(bytes[offset + (j*4) +3]);
						}
					}
					break;
				default:
					//System.out.print("We shouldn't get here");
					break;
				}
				//checking the tag value we do something with what the tag data holds.
				if(tag == 256)
				{
					pWidth = value[0];
				}
				if(tag == 257)
				{
					pHeight = value[0];
				}
				if(tag == 262)
				{
					switch(value[0])
					{
					case 0:
						reptn = "WZero";
						break;
					case 1:
						reptn = "BZero";
						break;
					case 2:
						reptn = "RGB";
						break;
					}
				}
				if(tag == 258)
				{
					bitsPerSample = 8;
				}
				if(tag == 259)
				{
					switch(value[0])
					{
					case 1:
						compression = "uncompressed";
						break;
					}
				}
				if(tag == 273)
				{
					stripOffSet = value;
				}
				if(tag == 277)
				{
					samplesPerPixel = value[0];
				}
				if(tag == 279)
				{
					stripByteCount = value;
				}
				if(tag == 284)
				{
					planarConfiguration = value[0];
				}
			}
			//close our input stream
			fileStream.close();
			//convert our now read image into the respective rendered formats
			byte[] grayImage = convertToGrayScale(bytes, pWidth, pHeight, stripOffSet, stripByteCount, samplesPerPixel);
			byte[] ditheredImage = convertToDithered(bytes, pWidth, pHeight, stripOffSet, stripByteCount, samplesPerPixel);
			byte[] rangedImage = convertDynamicRange(bytes, pWidth, pHeight, stripOffSet, stripByteCount, samplesPerPixel);
			File dithImageFile = new File("dithered.tif");
			File newImageFile = new File("grayScale.tif");
			File rangedFile = new File("ranged.tif");
			//write out each of our processed images back into a tif file stated above
			FileOutputStream stream = new FileOutputStream(newImageFile);
			for(int i = 0; i < grayImage.length; i++)
			{
				stream.write(grayImage[i]);
			}
			stream.flush();
			stream.close();
			stream = new FileOutputStream(dithImageFile);
			for(int i = 0; i < ditheredImage.length; i++)
			{
				stream.write(ditheredImage[i]);
			}
			stream.flush();
			stream.close();
			stream = new FileOutputStream(rangedFile);
			for(int i = 0; i < rangedImage.length; i++)
			{
				stream.write(rangedImage[i]);
			}
			stream.flush();
			stream.close();
			//after writing out our new tif images, display them in our panel by swapping labels
			BufferedImage image1 = ImageIO.read(displayImage);
			ImageIcon normalImage = new ImageIcon(image1);
			BufferedImage image2 = ImageIO.read(dithImageFile);
	        ImageIcon dithImageIcon = new ImageIcon(image2);
			BufferedImage image3 = ImageIO.read(newImageFile);
	        ImageIcon grayImageIcon = new ImageIcon(image3);
			BufferedImage image4 = ImageIO.read(rangedFile);
	        ImageIcon rangeImageIcon = new ImageIcon(image4);
	        JButton next = new JButton("Next");
	        next.addActionListener(new ActionListener()
	        		{
	        		public void actionPerformed(ActionEvent e){ 
	        			
	        			if( rolling > 4)
	        			{
	        				rolling=0;
	        			}
	        			switch(rolling++)
	        			{
	        			case 0: imageLabel.setIcon(normalImage); break;
	        			case 1: imageLabel.setIcon(dithImageIcon); break;
	        			case 2: imageLabel.setIcon(grayImageIcon); break;
	        			case 3: imageLabel.setIcon(rangeImageIcon); break;
	        			}
	        		}
	        		});
	        panel.add(next);
	        
	        JButton quit = new JButton("quit");
	        quit.addActionListener(new ActionListener()
	        		{
	        		public void actionPerformed(ActionEvent e){ 
	        			if(e.getSource() == quit)
	        			{
	        				System.exit(0);
	        			}
	        		}
	        		});
	        panel.add(quit);
            panel.setLayout(new FlowLayout());
            frame.add(panel);
            frame.setSize(1000,600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
		}
		catch(FileNotFoundException e)
		{
			System.out.print("Can't find file");
		}
		catch(IOException e)
		{
			System.out.print("Couldn't find output");
			System.out.println(e.toString());
		}
	}
	
	
}

