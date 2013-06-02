package com.android.rs.mel;

import org.apache.commons.math3.util.FastMath;

public class HammingFilter {
	private double[] hamFilter;
	int halfLength;
	double invN;
	
	//Generates a hamming filter.  We are assuming an even size window.
	public HammingFilter(int _windowSize){
		hamFilter = new double[_windowSize];
		halfLength = _windowSize/ 2;
		invN =  (double) 1 / (double) (_windowSize - 1);
		genFilter();
	}
	
	private void genFilter(){
		for (int i = 0; i < halfLength; ++i ){
			double val =  0.54f - 0.46f * FastMath.cos(2.0f *  FastMath.PI * (double) i * invN );
			hamFilter[i] = val;
			hamFilter[hamFilter.length - 1 - i] = val;
		}		
	}
	
	public double[] getHamFilter() {
		return hamFilter;
	}
}
