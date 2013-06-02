#pragma version(1)
#pragma rs java_package_name(com.android.rs)
#pragma rs_fp_relaxed


uint32_t numElements = 0;

//This filterscript an input and returns the sum of the array in parallel order
void root(const float *in, float *out, const void *usrData,  uint32_t x,  uint32_t y){
	float sum = 0.0f;
	for(uint32_t i = 0; i < numElements; ++i){
		sum += in[i];
	}
	*out = sum;
}

void runRestricted(rs_script mScript, rs_allocation in_alloc, rs_allocation out_alloc) {
	numElements = rsAllocationGetDimX(in_alloc);
	struct rs_script_call restrict_for;
	
	uint32_t emtpy = 0;	
    restrict_for.strategy = RS_FOR_EACH_STRATEGY_DONT_CARE;
    restrict_for.xStart = 0;
    restrict_for.xEnd = 1;
    restrict_for.yStart = 0;
    restrict_for.yEnd = rsAllocationGetDimY(in_alloc);
    restrict_for.zStart = 0;
    restrict_for.zEnd = 0;
    restrict_for.arrayStart = 0;
    restrict_for.arrayEnd = 0;
    rsForEach(mScript,  in_alloc, out_alloc, &emtpy, sizeof(uint32_t), &restrict_for);
}

