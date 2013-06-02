#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

#include "common.rsh"

rs_allocation gMelFilterBank;
rs_allocation gInputFreq;
uint32_t numElements = 0;
uint32_t numFilters = 0;

//This filterscript an input and returns the sum of the array in parallel order
float2 __attribute__((kernel)) root(float2 vec_el, uint32_t x){
	float sum = 0.0f;  
	for (uint32_t i = 0; i < numElements; ++i ) {
		sum += abs_im(rsGetElementAt_float2(gInputFreq,i, x/numFilters ))  * rsGetElementAt_float(gMelFilterBank, i, x%numFilters);
	}
	
	return (float2) {log(sum), 0.f};
}
