#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed


#include "common.rsh"

//This filterscript takes set of float2 values and returns it's power value
float __attribute__((kernel)) root(float2 in, uint32_t x){
	//Multiply the input and conjugate together to find the power
	float2 power = mul(in, conj(in));
	return power.x;
}