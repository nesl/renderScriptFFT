package com.android.rs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
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

public class Mel_Test extends Thread {
	private boolean msgHandled;

	public int result;
	protected Context mCtx;

	private Resources mRes;

	public Mel_Test(Resources mRes, Context mCtx) {
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
		frameSize = 128;
		sampleFreq = 8000;
		numFilters = 32;
		nCeps = 12;
		// Open test input file
		File file = new File(Environment.getExternalStorageDirectory(),
				"Download/16b_8k.wav");
		Wave w = new Wave(file.toString());
		
		//Follow Matlab example wave emphasis 
		float[] emphDat = emphData(ArrayConversion.doublesToFloats(w
				.getNormalizedAmplitudes()), 0.97f);
		int shiftLength = 10000 - 1;
		float[] shiftData = new float[emphDat.length - shiftLength];
		System.arraycopy(emphDat, shiftLength, shiftData, 0, shiftData.length);
		//Convert wave data to float2 array and copy over to device.
	    //testData = ArrayConversion.createRealImag(shiftData, frameSize);
		
		//Other testing frames
	    double[] oneframe = new double[] {-0.00148040771484376,-0.00926818847656249,-0.0174041748046875,-0.0262582397460938,-0.0343191528320312,-0.0430511474609375,-0.0381552124023438,-0.0357562255859375,-0.0387728881835938,-0.0271377563476563,-0.0243048095703125,-0.0258804321289063,-0.0200067138671875,-0.0247088623046875,-0.0159265136718750,-0.0137896728515625,-0.0165225219726562,-0.00361297607421876,0.000141906738281239,0.00446533203124999,0.0135824584960937,0.0182949829101562,0.0278622436523437,0.0381231689453125,0.0441467285156250,0.0515347290039063,0.0486660766601563,0.0470684814453125,0.0459906005859375,0.0348916625976563,0.0225064086914062,0.0145141601562500,0.00199493408203125,-0.0106933593750000,-0.0197601318359375,-0.0255126953125000,-0.0319638061523437,-0.0380007934570313,-0.0387234497070312,-0.0368975830078125,-0.0325082397460938,-0.0270681762695313,-0.0257693481445312,-0.0229129028320313,-0.0266326904296875,-0.0219924926757813,-0.0197250366210938,-0.0229885864257813,-0.0161129760742187,-0.00932006835937502,-0.00377868652343752,0.00708953857421873,0.0139178466796875,0.0219769287109375,0.0315420532226562,0.0393862915039063,0.0454501342773438,0.0528784179687500,0.0504467773437500,0.0478942871093750,0.0430541992187500,0.0350985717773438,0.0254959106445312,0.0131369018554688,-0.00180755615234374,-0.0104006958007812,-0.0173822021484375,-0.0247103881835938,-0.0301925659179687,-0.0334927368164062,-0.0405227661132813,-0.0362557983398438,-0.0326461791992188,-0.0327093505859375,-0.0270077514648438,-0.0244635009765625,-0.0259707641601563,-0.0203625488281250,-0.0192959594726563,-0.0182131958007813,-0.0131784057617187,-0.00718719482421876,-0.00125335693359377,0.00791168212890625,0.0152725219726562,0.0200955200195312,0.0263571166992188,0.0360192871093750,0.0398101806640625,0.0459381103515625,0.0477569580078125,0.0445532226562500,0.0398989868164063,0.0396447753906250,0.0279061889648438,0.0203005981445313,0.00735046386718752,-0.00490203857421875,-0.0116632080078125,-0.0195932006835937,-0.0280804443359375,-0.0320996093750000,-0.0396884155273437,-0.0395071411132813,-0.0361996459960938,-0.0328472900390625,-0.0317111206054688,-0.0307559204101563,-0.0233056640625000,-0.0232052612304688,-0.0211001586914062,-0.0147335815429687,-0.0165838623046875,-0.0112481689453125,-0.00208007812500000,-0.000871582031250012,0.0108984375000000,0.0149301147460937,0.0195257568359375,0.0315655517578125,0.0387265014648437,0.0435681152343750,0.0495248413085938,0.0483261108398438,0.0447274780273438,0.0421994018554688,0.0389169311523438,0.0243627929687500};
		testData = ArrayConversion.createRealImag(ArrayConversion.doublesToFloats(oneframe), frameSize);

		//1MB
		//double[] runTest = new double[262144];
		
		//5MB 1310720
		//double[] runTest = new double[1310720];
		
		//10MB 2621440
		//double[] runTest = new double[2621440];
		
		//15MB 3932160
		//double[] runTest = new double[3932160];
		
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

		//Take the log of the data
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

		//Copy final Cepstral Coefficients back to host
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


}
