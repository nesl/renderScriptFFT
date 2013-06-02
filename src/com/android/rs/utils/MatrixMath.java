package com.android.rs.utils;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;

public class MatrixMath {

	private MatrixMath(){}
		
    public static RealMatrix abs(RealMatrix mat) {
        final int rowCount    = mat.getRowDimension();
        final int columnCount = mat.getColumnDimension();
        final RealMatrix out = MatrixUtils.createRealMatrix(rowCount, columnCount);
        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < columnCount; ++col) {
                out.setEntry(row, col, FastMath.abs(mat.getEntry(row, col)));
            }
        }
        return out;
    }
    
    public static RealMatrix floor(RealMatrix mat){
        final int rowCount    = mat.getRowDimension();
        final int columnCount = mat.getColumnDimension();
        final RealMatrix out = MatrixUtils.createRealMatrix(rowCount, columnCount);
        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < columnCount; ++col) {
                out.setEntry(row, col, FastMath.floor(mat.getEntry(row, col)));
            }
        }
        return out;
    }
    
    public static int[] findFirstElementGreater(RealMatrix mat, double val) {
    	int[] index = new int[]{-1,-1};
        final int rowCount    = mat.getRowDimension();
        final int columnCount = mat.getColumnDimension();
        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < columnCount; ++col) {
                if (mat.getEntry(row, col) > val){
                	index[0] = row;
                	index[1] = col;	
                	return index;
                }
            }
        }
        return index;
    }
    
    public static int[] findLastElementLessThan(RealMatrix mat, double val) {
    	int[] index = new int[]{-1,-1};
        final int rowCount    = mat.getRowDimension();
        final int columnCount = mat.getColumnDimension();
        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < columnCount; ++col) {
                if (mat.getEntry(row, col) < val){
                	index[0] = row;
                	index[1] = col;	
                } else{
                	return index;
                }
            }
        }
        return index;
    }
    

}
