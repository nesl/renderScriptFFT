package com.android.rs.test;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

import com.android.rs.ScriptC_high_pass;
import com.example.renderscriptfft.R;

public class HighPass_Test extends Thread {
	private boolean msgHandled;
	
    public int result;
    protected Context mCtx;
	
    private Resources mRes;
    
    public HighPass_Test(Resources mRes, Context mCtx){
    	super();
    	this.mCtx = mCtx;
    	this.mRes = mRes;
    	msgHandled = false;
    	setup();
    }

	private float[] inputData;
	private float[] outputData;
	private double[] verification;
	private float dt;
	private float RC;
	  
    public void waitForMessage() {
        while (!msgHandled) {
            yield();
        }
    }
    

    private void setup(){
	      inputData = new float[]{1.f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f,1.f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f};
	      outputData = new float[inputData.length];
	      verification = new double[inputData.length];
	      dt = 0.1f;
	      RC = 1.0f;
	  
    }
    
    public void run() {
    	
    	RenderScript mRS = RenderScript.create(mCtx);
    	Allocation mInAllocation = Allocation.createSized(mRS, Element.F32_2(mRS), inputData.length/2);
    	
	    mInAllocation.copyFrom(inputData);
	   
	    Allocation  mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
	    
	    ScriptC_high_pass mScript = new ScriptC_high_pass(mRS, mRes, R.raw.high_pass);
	    mScript.set_gIn(mInAllocation);
	    mScript.set_gOut(mOutAllocation);
	    mScript.set_a(RC / (RC + dt));
	   
	    
	    //So far rendesrcipt seems to be able to detect this is a serial-like algorithm.
	    //If in your testing you start to get error change it back to the serial or linear restriction
	    mScript.forEach_root(mInAllocation, mOutAllocation);
	   // mScript.invoke_runRestricted(mScript, dt, RC); 
	   	    
	    mOutAllocation.copyTo(outputData);
	    mInAllocation.copyTo(inputData);
	   
	    
	    System.out.println("Debug: Verification " + Arrays.toString(verification));
	    System.out.println("Debug: outBuff " + Arrays.toString(outputData));
	  }
    }

