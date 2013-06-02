#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

rs_allocation gFilter;

//This filterscript takes an input array of 2D Values (each row is a window of data) apply the same multiplication on that window
float2 __attribute__((kernel)) root(float2 in, uint32_t x){
	return in * rsGetElementAt_float(gFilter, x);
}
