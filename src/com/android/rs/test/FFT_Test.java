package com.android.rs.test;

import java.util.Arrays;

import com.android.rs.ScriptC_fft;
import com.example.renderscriptfft.R;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.RenderScript.RSMessageHandler;

public class FFT_Test extends Thread {
	private boolean msgHandled;
	
    /* These constants must match those in shared.rsh */
    public static final int RS_MSG_TEST_PASSED = 100;
    public static final int RS_MSG_TEST_FAILED = 101;
    public int result;
    protected Context mCtx;
	
    private Resources mRes;
    
    public FFT_Test(Resources mRes, Context mCtx){
    	super();
    	this.mCtx = mCtx;
    	this.mRes = mRes;
    	msgHandled = false;
    	setup();
    }

	private float[] inputData;
	private float[] outputData;

	  
    public void waitForMessage() {
        while (!msgHandled) {
            yield();
        }
    }
    
    protected RSMessageHandler mRsMessage = new RSMessageHandler() {
        public void run() {
            if (result == 0) {
                switch (mID) {
                    case RS_MSG_TEST_PASSED:
                        result = 1;
                        break;
                    case RS_MSG_TEST_FAILED:
                        result = -1;
                        break;
                    default:
                       // RSTest_v14.log("Unit test got unexpected message");
                        return;
                }
            }
            System.out.println("Debug: Message Handled" );
            msgHandled = true;
        }
    };
    
    private void setup(){
    	//  int size = (int) (Math.pow(2.0, 19) ) * 2;
    	  
    	//  inputData = new float[size];
    	//  System.out.println("Debug: inputData Size " + size);
    	//  for(int i = 0; i < size; ++i){
    	//	  inputData[i] = (float) Math.random();
    	 // }
    	  
	     // inputData = new float[]{1.f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f,1.f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f};
	      //inputData = new float[]{1.f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f};
	      inputData = new float[]{1.f, 2.f, 3.f, 4.f};
	      outputData = new float[inputData.length];
	  
    }
    
    public void run() {
    	
    	RenderScript mRS = RenderScript.create(mCtx);
    	int numElements = inputData.length/2;
    	Allocation mInAllocation = Allocation.createSized(mRS, Element.F32_2(mRS), numElements);
	      mInAllocation.copyFrom(inputData);
	      
	      Allocation  mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
	      ScriptC_fft mScript = new ScriptC_fft(mRS, mRes, R.raw.fft);

	      mScript.invoke_runRestricted(mScript, mInAllocation, mOutAllocation);
	      mOutAllocation.copyTo(outputData);
	      mInAllocation.copyTo(inputData);
	      
	      System.out.println("Debug: inBuff " + Arrays.toString(inputData));
	      System.out.println("Debug: outBuff " + Arrays.toString(outputData));
	  }
    }

