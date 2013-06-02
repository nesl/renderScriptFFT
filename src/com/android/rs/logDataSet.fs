#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed


#include "common.rsh"

//This filterscript takes the log of the input data
float __attribute__((kernel)) root(float in, uint32_t x){
	return log(in);
}