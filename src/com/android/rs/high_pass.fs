#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

rs_allocation gIn;
rs_allocation gOut;

float a = 0;
   
float2 __attribute__((kernel)) root(float2 in, uint32_t x){
	if ( x == 0 ) return in;
	else return   a * (rsGetElementAt_float2(gOut, x-1)  + rsGetElementAt_float2(gIn, x) - rsGetElementAt_float2(gIn, x-1));
	
}