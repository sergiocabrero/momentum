package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Functions {
	public static int byteToUnsigned(byte _b)
	{
		return (int) _b & 0xFF;		
	}
	
	public static byte unsignedToByte(int _i)
	{
		return (byte) _i;
	}
	
	public static byte[] intToByte(int _i)
	{
		byte[] b = new byte[4];
		b[3] = (byte) (_i);
		_i >>>= 8;
		b[2] = (byte) (_i);
		_i >>>= 8;
		b[1] = (byte) (_i);
		_i >>>= 8;
		b[0] = (byte) (_i);

		return b;
	}
	/**
	 * Converts a two byte array to an integer
	 * @param b a byte array of length 2
	 * @return an int representing the unsigned short
	 */
	public static int unsignedShortToInt(byte[] b) 
	{
	    int i = 0;
	    i |= b[0] & 0xFF;
	    i <<= 8;
	    i |= b[1] & 0xFF;
	    return i;
	}
	
	
	public static byte[] shortToByteArray(int i) {
			byte[] dword = new byte[2];
			dword[1] = (byte) (i & 0x00FF);
			dword[0] = (byte) ((i >> 8) & 0x000000FF);
			return dword;
		}
	
	  /**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
	
	public static boolean compareStringArrays(String[] _A, String[] _B)
	{
		/*
		 * Simple way to compare the content of two arrays
		 * converting them in Lists
		 */
		
		List listA = Arrays.asList(_A);
	    List listB = Arrays.asList(_B);
	 
	    List listR = new ArrayList(listA);
	    listR.removeAll(listB);
	 
	    return (listR.size() == 0);
	 
	}
}
