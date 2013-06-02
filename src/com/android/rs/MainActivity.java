package com.android.rs;

import java.util.Arrays;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;

import com.example.renderscriptfft.R;

public class MainActivity extends Activity {
 

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_main);
	    
	     /*FFT_Test test = new FFT_Test(getResources(), this);
	     test.run();
	     

		iFFT_Test iFFT = new iFFT_Test(getResources(), this);
		iFFT.run();
	    
	    HighPass_Test hp = new HighPass_Test(getResources(), this);
	    hp.run();
	      
	    Hamming_Test hm = new Hamming_Test(getResources(), this);
	    hm.run();*/
	    
	      /*  
	    Mel_Test_MicroBench mt = new Mel_Test_MicroBench(getResources(), this);
	    int numRuns = 20;
	    double[] timeTot = null;
	     for( int i = 0; i < numRuns; ++i){
	    	 mt.run();
	    	 try {
				mt.join();
				List<Long> curtime = mt.getTime();
				if (timeTot == null) timeTot = new double[curtime.size()];
				for (int j = 0; j < curtime.size(); ++j){
					timeTot[j] += (double) curtime.get(j);
					System.out.println("Debug: Cur execution #" + j + ": " + curtime.get(j) );
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    
	     }
	     for (int i = 0; i < timeTot.length; ++i){
	    	 System.out.println("Debug: Avg Time #:" + i + " " + timeTot[i]/(double)numRuns); 
	     }
	     
	    */
	   
	  //Mel Test
	  //Mel_Test mt = new Mel_Test(getResources(), this);
	  //mt.run();
	  
	  //Microphone test
	  Mel_Test_Microphone mp = new Mel_Test_Microphone(getResources(), this);
	  mp.run();

	     
	  }


	}

