#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

rs_allocation m2D;
rs_allocation gOut;

float __attribute__((kernel)) root(float vec_el, uint32_t x){
	uint32_t length2D = rsAllocationGetDimX(m2D);
	
	float sum = 0;  
	for (uint32_t i = 0; i < length2D; ++i ) {
		sum = sum + vec_el  * rsGetElementAt_float(m2D, i, x);
	}
	return sum;
}