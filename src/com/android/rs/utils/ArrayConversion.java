package com.android.rs.utils;

import org.apache.commons.math3.util.FastMath;

public class ArrayConversion {
	private ArrayConversion() {
	}

	public static float[] bytesToFloat(byte[] in){
		float[] out = new float[in.length];
		for (int i = 0; i < in.length; ++i) {
			out[i] = (float) in[i];
		}
		return out;
	}
	
	public static float[] doublesToFloats(double[] in) {
		float[] out = new float[in.length];
		for (int i = 0; i < in.length; ++i) {
			out[i] = (float) in[i];
		}
		return out;
	}
	
	public static float[] createRealImag(float[] in, int frameSize) {
		int dataLength = in.length;
		int numFrames = (int) FastMath.ceil((double) dataLength
				/ (double) frameSize);
		
		float[] out = new float[numFrames * frameSize * 2];
		for (int i = 0; i < in.length; ++i) {
			out[i * 2] = in[i];
		}

		return out;
	}
	
}
