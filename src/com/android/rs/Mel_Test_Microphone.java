package com.android.rs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Int2;
import android.renderscript.RenderScript;
import android.renderscript.Type;

import com.android.rs.mel.DCT;
import com.android.rs.mel.HammingFilter;
import com.android.rs.mel.MelFilterbank;
import com.android.rs.utils.ArrayConversion;
import com.example.renderscriptfft.R;
import com.musicg.wave.Wave;

public class Mel_Test_Microphone extends Thread {
	private boolean msgHandled;

	public int result;
	protected Context mCtx;

	private Resources mRes;

	public Mel_Test_Microphone(Resources mRes, Context mCtx) {
		super();
		this.mCtx = mCtx;
		this.mRes = mRes;
		msgHandled = false;
		setup();
	}

	private int sampleFreq, frameSize, numFrames, numFilters, xWidth, nCeps;
	private Int2 fftRange;
	private RenderScript mRS;
	private ScriptC_fft fftScript;
	private ScriptC_filterMultiplication fMult;
	private ScriptC_powerCalc powerCalc;
	private ScriptC_parallelSum parSum;
	private ScriptC_logDataSet logDataSet;
	private ScriptC_melFilterResps melResp;
	private ScriptC_rearrangeFilterResp rearrangeFilterResp;
	private ScriptC_computeDCT computeDCT;
	private Allocation mAudioAllocation, mHamFilterData, mMelFilterData, mDCTData, mFFTAllocation, mSamplesInFreq, 
					   mPowerInFreq, mSumPowerInFreq, mMelFilterResps, mRearrangeResp, mFFTResp, mDCTY, mMelCeps;
	private float[] testData;

	public void waitForMessage() {
		while (!msgHandled) {
			yield();
		}
	}

	private void setup() {
		mRS = RenderScript.create(mCtx);
		
	      int bufferSize=AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO,
	              AudioFormat.ENCODING_PCM_16BIT);
	      byte[] buffer1 = new byte[bufferSize];
	      fillBuffer(buffer1, bufferSize);
	      
	      
		frameSize = 128;
		sampleFreq = 8000;
		numFilters = 32;
		nCeps = 12;
		// Open test input file
		//int shiftLength = 0;
	    testData = ArrayConversion.createRealImag(ArrayConversion.bytesToFloat(buffer1), frameSize);
		
		
		//testData = ArrayConversion.createRealImag(ArrayConversion.doublesToFloats(runTest), frameSize);
		//Convert wave data to float2 array and copy over to device.
		
	    //Note: In the future we probably want to create a separate method to
		//do this in real time for mic input
		numFrames = (testData.length / 2) / frameSize;
		Type.Builder typeBuilder = new Type.Builder(mRS, Element.F32_2(mRS));
		typeBuilder.setX(frameSize).setY(numFrames);
		mAudioAllocation = Allocation.createTyped(mRS,typeBuilder.create());
		
		//Create hamming window that will be used 
		HammingFilter hf = new HammingFilter(frameSize);
		float[] hamData = ArrayConversion.doublesToFloats(hf.getHamFilter());
		mHamFilterData = Allocation.createSized(mRS, Element.F32(mRS), hamData.length);
		mHamFilterData.copyFrom(hamData);
		
		//Create Mel Filter bank
		MelFilterbank mf = new MelFilterbank(numFilters, frameSize, sampleFreq, 0, .5);
		float[] melFilterBank = mf.getFlattenedFilterBank();
		fftRange = mf.getFFTRange();
		
		//Calculate DCT
		DCT dtc = new DCT(numFilters);
		mDCTData = Allocation.createSized(mRS, Element.F32_2(mRS), numFilters);
		mDCTData.copyFrom(ArrayConversion.doublesToFloats(dtc.getCoeffs()));
		
		
		//Set up scripts
		
		//Hamming Filter
		fMult = new ScriptC_filterMultiplication(mRS, mRes, R.raw.filtermultiplication);
		fMult.set_gFilter(mHamFilterData);
		
		//FFT
		fftScript = new ScriptC_fft(mRS, mRes, R.raw.fft);
		mFFTAllocation = Allocation.createTyped(mRS,typeBuilder.create());
				
		//Samples in Freq
		xWidth = fftRange.y - fftRange.x + 1; //y is upper limit. x is lower
		typeBuilder.setX(xWidth).setY(numFrames);
		mSamplesInFreq = Allocation.createTyped(mRS,typeBuilder.create());
		
		//Power Scripts
		powerCalc = new ScriptC_powerCalc(mRS, mRes, R.raw.powercalc);
		typeBuilder = new Type.Builder(mRS, Element.F32(mRS));
		typeBuilder.setX(xWidth).setY(numFrames);
		mPowerInFreq = Allocation.createTyped(mRS,typeBuilder.create());
		parSum = new ScriptC_parallelSum(mRS, mRes, R.raw.parallelsum);
		typeBuilder.setX(1).setY(numFrames);
		mSumPowerInFreq = Allocation.createTyped(mRS,typeBuilder.create());
		logDataSet = new ScriptC_logDataSet(mRS, mRes, R.raw.logdataset);
		
		//Mel Filter Responses
		typeBuilder.setX(mf.getNumCols()).setY(mf.getNumRows());
		mMelFilterData = Allocation.createTyped(mRS, typeBuilder.create());
		mMelFilterData.copy2DRangeFrom(0, 0, mf.getNumCols(), mf.getNumRows(), melFilterBank);
		melResp = new ScriptC_melFilterResps(mRS, mRes, R.raw.melfilterresps);
		melResp.set_gMelFilterBank(mMelFilterData);		
		melResp.set_gInputFreq(mSamplesInFreq);
		melResp.set_numElements(mf.getNumCols());
		melResp.set_numFilters(mf.getNumRows());
		
		
		typeBuilder = new Type.Builder(mRS, Element.F32_2(mRS));
		typeBuilder.setX(numFilters).setY(numFrames);
		mMelFilterResps = Allocation.createTyped(mRS, typeBuilder.create());
		
		rearrangeFilterResp = new ScriptC_rearrangeFilterResp(mRS, mRes, R.raw.rearrangefilterresp);
		rearrangeFilterResp.invoke_setup(numFilters);
		
		mRearrangeResp = Allocation.createTyped(mRS, typeBuilder.create());
		mFFTResp = Allocation.createTyped(mRS, typeBuilder.create());
		
		typeBuilder = new Type.Builder(mRS, Element.F32(mRS));
		typeBuilder.setX(numFilters).setY(numFrames);
		mDCTY = Allocation.createTyped(mRS, typeBuilder.create());
		computeDCT = new ScriptC_computeDCT(mRS, mRes, R.raw.computedct);
		computeDCT.invoke_setup(numFilters, mDCTData);
		
		typeBuilder.setX(nCeps+1).setY(numFrames);
		mMelCeps = Allocation.createTyped(mRS, typeBuilder.create());
		
	}
	
	public void run() {
		
		//Copy current audio data
		mAudioAllocation.copy2DRangeFrom(0, 0, frameSize, numFrames, testData);
	
		//Smooth samples for all input windows (Apply Hamming window)
		fMult.forEach_root(mAudioAllocation, mAudioAllocation);
		
		//Perform fft
		fftScript.invoke_runRestricted(fftScript, mAudioAllocation, mFFTAllocation);

		//Depending on the iteration, the final data in the FFT can be either the in or out array
		Allocation fftOut;
		if (fftScript.get_finalFFT() == 0)
			fftOut = mFFTAllocation;
		 else 
			fftOut = mAudioAllocation;
		
		
		//Get valid range of fft (equivalent of samples_in_freq(fftA:fftB)) in original Matlab code
		mSamplesInFreq.copy2DRangeFrom(0, 0, xWidth, numFrames, fftOut, fftRange.x, 0);
		
		//Find the power of the samples
		powerCalc.forEach_root(mSamplesInFreq, mPowerInFreq);

		//Sum the power up (this can be improved with a better parallel reduction) 
		parSum.invoke_runRestricted(parSum, mPowerInFreq, mSumPowerInFreq);
		logDataSet.forEach_root(mSumPowerInFreq,mSumPowerInFreq);

		//Compute the Mel Filter Responses
		melResp.forEach_root(mMelFilterResps, mMelFilterResps);	
		rearrangeFilterResp.forEach_root(mMelFilterResps, mRearrangeResp);

		//Perform fft on rearranged responses
		fftScript.invoke_runRestricted(fftScript, mRearrangeResp, mFFTResp);

		//Depending on the iteration, the final data in the FFT can be either  the in or out array
		if (fftScript.get_finalFFT() == 0)
			fftOut = mFFTResp;
		else 
			fftOut = mRearrangeResp;
		
		//Compute Discrete Cosine transform 
		computeDCT.forEach_root(fftOut, mDCTY);
		
		//Compute Mel Ceps
		mMelCeps.copy2DRangeFrom(0, 0, 1, numFrames, mSumPowerInFreq, 0, 0);
		mMelCeps.copy2DRangeFrom(1, 0, nCeps, numFrames, mDCTY, 1, 0);

		float[] debug = new float[(nCeps+1)*numFrames];
		mMelCeps.copyTo(debug);
	}
	
	//This is the pre-emphasis filter that was used for the input data in matlab
	private float[] emphData(float[] data, float emph){
		float[] tmpData = new float[data.length];
		
		tmpData[0] = data[0];
		for (int i = 1; i < data.length; ++i){
			tmpData[i] = data[i] - emph * data[i-1];
		}
		return tmpData;
	}

	  private byte[] fillBuffer(byte[] audioData, int bufferSize) {

		    AudioRecord recorder = new AudioRecord(AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
		            AudioFormat.ENCODING_PCM_16BIT, bufferSize); // instantiate the
		                                                            // AudioRecorder
		    if (recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED)
		        recorder.startRecording(); // check to see if the Recorder
		                                    // has stopped or is not
		                                    // recording, and make it
		                                    // record.

		    recorder.read(audioData, 0, bufferSize); // read the PCM
		                                                // audio data
		                                                // into the
		                                                // audioData
		                                                // array

		    if (recorder.getState() == android.media.AudioRecord.RECORDSTATE_RECORDING)
		        recorder.stop(); // stop the recorder

		    return audioData;

		}

}
