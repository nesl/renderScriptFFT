package com.android.rs.utils;

import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;

public class MelMath {
	private final static double k = 1000.0/FastMath.log(1.0 + 1000.0 / 700.0);
	
	private MelMath(){}
		
	public static RealMatrix doubleAllExceptDCandNyquist(RealMatrix v, final RealMatrix c, final double mn, final double n, final double fn2){
		final RealMatrix out = MatrixUtils.createRealMatrix(v.getRowDimension(), v.getColumnDimension());
        DefaultRealMatrixPreservingVisitor mv = new DefaultRealMatrixPreservingVisitor(){
			@Override
			public void visit(int row, int col, double vval) {
				final double cval =  c.getEntry(row, col);
				if (cval + mn > 0 & (cval + mn < n - fn2)){
            		out.setEntry(row, col, vval*2.0);
            	} else {
            		out.setEntry(row, col, vval);
            	}
            	
			}
        };
        
        v.walkInOptimizedOrder(mv);
		return out;	
	}
	
	public static RealMatrix frq2mel(RealMatrix frq){
		final RealMatrix mel = MatrixUtils.createRealMatrix(frq.getRowDimension(), frq.getColumnDimension());
        DefaultRealMatrixPreservingVisitor mv = new DefaultRealMatrixPreservingVisitor(){
			@Override
			public void visit(int row, int col, double curFrq) {
            	final double aF = FastMath.abs(curFrq);
            	mel.setEntry(row, col, FastMath.signum(curFrq)*Math.log(1.0+aF/700.0)*k);
			}
        };
        
        frq.walkInOptimizedOrder(mv);
		return mel;	
	}
	
	public static RealMatrix mel2frq(RealMatrix mel){  
		final RealMatrix frq = MatrixUtils.createRealMatrix(mel.getRowDimension(), mel.getColumnDimension());
		DefaultRealMatrixPreservingVisitor mv = new DefaultRealMatrixPreservingVisitor(){
			@Override
			public void visit(int row, int col, double curMel) {
            	final double aM = FastMath.abs(curMel);
            	frq.setEntry(row, col, 700.0 * FastMath.signum(curMel) * (Math.exp( aM/k ) - 1.0));
			}
        };
        
        mel.walkInOptimizedOrder(mv);
		return frq;	
	}
}
