#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed

uint32_t numElements = 0;

void setup(uint32_t _numElements){
	numElements = _numElements;
}

void root(const float2 *in, float2 *out, uint32_t x, uint32_t y){
	bool runCase = x % 2;
	//Determine where we are
	uint32_t pos = (uint32_t)!runCase * x/2 + (uint32_t) runCase*(numElements - 1 - (x-1)/2);
	out[pos -  x] = *in;
}