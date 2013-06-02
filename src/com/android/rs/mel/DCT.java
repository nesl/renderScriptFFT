package com.android.rs.mel;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

public class DCT {
	int numCoeffs = 0;
	double[] coeffs;
	public DCT(int _numCoeffs){
		numCoeffs = _numCoeffs;
		coeffs = new double[_numCoeffs*2];
		genCoeffs();
	}
	
	private void genCoeffs(){
		coeffs[0] = FastMath.sqrt(2);
		coeffs[1] = 0.0;
		Complex base = new Complex(0.0, 1.0);
		for (int i = 1; i < numCoeffs; ++i){
			Complex tmp = base.multiply((-0.5*FastMath.PI / numCoeffs) * (double) i).exp().multiply(2.0);
			coeffs[2*i] = tmp.getReal();
			coeffs[2*i + 1] = tmp.getImaginary();		
		} 
	}

	public double[] getCoeffs() {
		return coeffs;
	}
	
	
	
}
