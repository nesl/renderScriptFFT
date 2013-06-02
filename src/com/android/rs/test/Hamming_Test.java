package com.android.rs.test;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

import com.example.renderscriptfft.R;

public class Hamming_Test extends Thread {
	private boolean msgHandled;
	
    public int result;
    protected Context mCtx;
	
    private Resources mRes;
    
    public Hamming_Test(Resources mRes, Context mCtx){
    	super();
    	this.mCtx = mCtx;
    	this.mRes = mRes;
    	msgHandled = false;
    	setup();
    }

	private float[] inputData;
	private float[] outputData;
	private double[] verification;
	  
    public void waitForMessage() {
        while (!msgHandled) {
            yield();
        }
    }
    

    private void setup(){
	      inputData = new float[]{1.f, 1.f, 1.f, 1.f, 1.f, 1.f, 1.f, 1.f,1.f, 1.f, 1.f, 1.f, 1.f, 1.f, 1.f, 1.f};
	      outputData = new float[inputData.length];
	      verification = new double[inputData.length];
	      verification();
	  
    }
    
    private void verification(){
    	/*
    	double a = RC / (RC + dt);
    	verification[0] = inputData[0];
    	
    	for (int i = 1; i < verification.length; ++i){
    		verification[i] = a * (verification[i-1] + inputData[i] - inputData[i-1]);
    	}*/
    }
    public void run() {
    	
    	RenderScript mRS = RenderScript.create(mCtx);
    	Allocation mInAllocation = Allocation.createSized(mRS, Element.F32(mRS), inputData.length);
    	
	    mInAllocation.copyFrom(inputData);
	   
	    Allocation  mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
	    
	    //ScriptC_hamming mScript = new ScriptC_hamming(mRS, mRes, R.raw.hamming);
	    //mScript.set_invN(1.0f / (inputData.length - 1));
	   
	    //mScript.forEach_root(mInAllocation, mOutAllocation);
	   	    
	    mOutAllocation.copyTo(outputData);
	    mInAllocation.copyTo(inputData);
	   
	    
	    System.out.println("Debug: Verification " + Arrays.toString(verification));
	    System.out.println("Debug: outBuff " + Arrays.toString(outputData));
	  }
    }

