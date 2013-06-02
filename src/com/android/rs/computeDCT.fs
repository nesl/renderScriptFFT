#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

#include "common.rsh"

rs_allocation gDTC;

float sqrt2npassinv = 0.f;

void setup(uint32_t npassband, rs_allocation _DTC){
	sqrt2npassinv = 1.0f / sqrt(2.f*(float)npassband);
	gDTC = _DTC;
}

float __attribute__((kernel)) root(float2 vec_el, uint32_t x){
	return mul(vec_el, rsGetElementAt_float2(gDTC,x)).x * sqrt2npassinv;
}