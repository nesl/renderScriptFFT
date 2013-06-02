package com.android.rs.mel;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;

import android.renderscript.Int2;

import com.android.rs.utils.MatrixMath;
import com.android.rs.utils.MelMath;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

public class MelFilterbank {

	private int numFilters, lengthFFT, numRows, numCols;
	private Int2 fftRange;
	private double sampleRateHz, freqLow, freqHigh;
	private double[] rowArray, colArray, valArray;
	private float[][] nonSparseFilterBank;
	private float[] flattenedFilterBank;
	/*
	 * Inputs: p number of filters in filterbank or the filter spacing in
	 * k-mel/bark/erb [ceil(4.6*log10(fs))] n length of fft fs sample rate in Hz
	 * fl low end of the lowest filter as a fraction of fs [default = 0] fh high
	 * end of highest filter as a fraction of fs [default = 0.5]
	 */

	public MelFilterbank(int _numFilters, int _lengthFFT, double _sampleRateHz,
			double _freqLow, double _freqHigh) {
		numFilters = _numFilters;
		lengthFFT = _lengthFFT;
		sampleRateHz = _sampleRateHz;
		freqLow = _freqLow;
		freqHigh = _freqHigh;

		// Default Condition
		int sfact = 2; // 2-any(w=='s'); % 1 if single sided else 2

		// convert frequency limits into mel
		RealMatrix mflh = MelMath.frq2mel(new Array2DRowRealMatrix(
				new double[] { freqLow, freqHigh })
				.scalarMultiply(sampleRateHz).transpose());

		// mel range
		double melrng = mflh.multiply(
				new Array2DRowRealMatrix(new double[] { -1.0, 1.0 })).getEntry(
				0, 0);

		// bin index of highest positive frequency (Nyquist if n is even)
		int fn2 = (int) FastMath.floor(lengthFFT / 2);

		double melinc = melrng / (numFilters + 1);

		// Calculate the FFT bins corresponding to [filter#1-low filter#1-mid
		// filter#p-mid filter#p-high]
		RealMatrix blim = MelMath.mel2frq(
				new Array2DRowRealMatrix(new double[] { 0.0, 1.0, numFilters,
						numFilters + 1.0 }).transpose().scalarMultiply(melinc))
				.scalarMultiply(lengthFFT / sampleRateHz);

		// Mel centre frequencies
		double[] mc = new double[numFilters];
		for (int i = 0; i < numFilters; ++i) {
			mc[i] = mflh.getEntry(0, 0) + (i + 1) * melinc;
		}

		// lowest FFT bin_0 required might be negative
		int b1 = (int) FastMath.floor(blim.getEntry(0, 0)) + 1;

		// highest FFT bin_0 required
		int b4 = (int) FastMath
				.min(fn2, FastMath.ceil(blim.getEntry(0, 3) - 1));

		// now map all the useful FFT bins_0 to filter1 centres
		double[] pfa = new double[b4 - b1 + 1];
		for (int i = 0; i <= (b4 - b1); ++i) {
			pfa[i] = (b1 + i) * sampleRateHz / lengthFFT;
		}

		RealMatrix pf = MelMath
				.frq2mel(new Array2DRowRealMatrix(pfa).transpose())
				.scalarAdd(-mflh.getEntry(0, 0)).scalarMultiply(1.0 / melinc);

		// remove any incorrect entries in pf due to rounding errors
		int pfStart = 0;
		int pfEnd = pfa.length - 1;

		if (pf.getEntry(0, pfStart) < 0.0) {
			pfStart = 1;
		}

		if (pf.getEntry(0, pfEnd) >= numFilters + 1) {
			pfEnd -= 1;
			b4 -= 1;
		}

		pf = pf.getSubMatrix(0, 0, pfStart, pfEnd);

		// FFT bin_0 i contributes to filters_1 fp(1+i-b1)+[0 1]
		RealMatrix fp = MatrixMath.floor(pf);

		// multiplier for upper filter
		RealMatrix pm = pf.add(fp.scalarMultiply(-1.0));

		// FFT bin_1 k2+b1 is the first to contribute to both upper and lower
		// filters
		int k2 = MatrixMath.findFirstElementGreater(fp, 0.0)[1];

		// FFT bin_1 k3+b1 is the last to contribute to both upper and lower
		// filters
		int k3 = MatrixMath.findLastElementLessThan(fp, numFilters)[1];

		// FFT bin_1 k4+b1 is the last to contribute to any filters
		int k4 = pfEnd;

		// % filter number_1
		RealMatrix r = MatrixUtils.createRowRealMatrix(Doubles.concat(fp
				.getSubMatrix(0, 0, 0, k3).getRow(0),
				fp.getSubMatrix(0, 0, k2, k4).scalarAdd(-1.0).getRow(0)));
		

		// FFT bin_1 - b1
		RealMatrix c = MatrixUtils.createRowRealMatrix(Doubles.concat(
				Doubles.toArray(ContiguousSet.create(Range.closed(0, k3),
						DiscreteDomain.integers()).asList()),
				Doubles.toArray(ContiguousSet.create(Range.closed(k2, k4),
						DiscreteDomain.integers()).asList())));

		RealMatrix v = MatrixUtils.createRowRealMatrix(Doubles.concat(
				pm.getSubMatrix(0, 0, 0, k3).getRow(0),
				pm.getSubMatrix(0, 0, k2, k4).scalarMultiply(-1.0)
						.scalarAdd(1.0).getRow(0)));

		if (b1 < 0) {
			c = MatrixMath.abs(c.scalarAdd(b1 - 1)).scalarAdd(-b1 + 1);
		}

		if (sfact == 2) {
			v = MelMath.doubleAllExceptDCandNyquist(v, c, b1, lengthFFT, fn2);
		}

		rowArray = r.getRow(0);
		colArray = c.getRow(0);
		valArray = v.getRow(0);
		numRows = (int) (Doubles.max(rowArray) + 1);
		numCols = (int) (Doubles.max(colArray) + 1);
		nonSparseFilterBank = new float[numRows][numCols];
		for (int i = 0; i < valArray.length; ++i){
			nonSparseFilterBank[(int) rowArray[i]][(int) colArray[i]] = (float) valArray[i];
		}
		
		flattenedFilterBank = Floats.concat(nonSparseFilterBank);
		
		//Lowest fft bin b1, highest fft bin b4
		fftRange = new Int2(b1, b4);
	}
	
	
	public float[] getFlattenedFilterBank() {
		return flattenedFilterBank;
	}

	public int getNumRows() {
		return numRows;
	}


	public int getNumCols() {
		return numCols;
	}


	public Int2 getFFTRange(){
		return fftRange;
	}

	public float[][] getNonSparseFilterBank() {
		return nonSparseFilterBank;
	}

	public double[] getRowArray() {
		return rowArray;
	}

	public double[] getColArray() {
		return colArray;
	}

	public double[] getValArray() {
		return valArray;
	}

}
