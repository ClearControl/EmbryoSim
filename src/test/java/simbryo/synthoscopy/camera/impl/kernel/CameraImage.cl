#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"
          
                                
__kernel void upscale(     __read_only    image2d_t    imagein,
                           __write_only   image2d_t    imageout,
                           const          float        nxmin,
                           const          float        nxscale,
                           const          float        nymin,
                           const          float        nyscale
                     )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width   = get_global_size(0); 
  const int height  = get_global_size(1);
  
  const float nx = ((float)x+0.5f)/width;
  const float ny = ((float)y+0.5f)/height;

  const float value = read_imagef(imagein, normsampler, (float2){nxmin+nx*nxscale,nymin+ny*nyscale}).x;
  
  write_imagef  (imageout, (int2){x,y}, (float4){value,0.0f,0.0f,0.0f});
  
}



__kernel void camnoise(   __read_only    image2d_t  imagein,
                          __write_only   image2d_t  imageout,
                          const          int        timeindex,
                          const          float      exposure,
                          const          float      shotnoise,
                          const          float      offset,
                          const          float      gain,
                          const          float      offsetbias,
                          const          float      gainbias,
                          const          float      offsetnoise,
                          const          float      gainnoise
                      )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width   = get_global_size(0); 
  const int height  = get_global_size(1);
  
  // generating some entropy (its noise_xy and not noi_sexy ;-):
  const unsigned int noisexy1  = rnguint2(x,y);
  const unsigned int noisexy2  = rnguint1(noisexy1);
  const unsigned int noisexyt1 = rnguint1(noisexy2+timeindex);
  const unsigned int noisexyt2 = rnguint1(noisexyt1);
  const unsigned int noisexyt3 = rnguint1(noisexyt2);
  
  const float noisexy1f  = rngfloat(noisexy1);
  const float noisexy2f  = rngfloat(noisexy2);
  const float noisexyt1f = rngfloat(noisexyt1);
  const float noisexyt2f = rngfloat(noisexyt2);
  const float noisexyt3f = rngfloat(noisexyt3);
  
  
  const float fluovalue  = exposure*read_imagef( imagein, intsampler, (int2){x,y}).x;
  
  const float shotnoisevalue   = shotnoise*native_sqrt(fluovalue)*clampedcauchy(noisexyt1f,9);
  
  const float offsetbiasvalue  = offsetbias*clampedcauchy(noisexy1f,9);
  const float gainbiasvalue    = gainbias*clampedcauchy(noisexy2f,9);
  
  const float offsetnoisetemp  = clampedcauchy(noisexyt2f,9);
  const float offsetnoisevalue = offsetnoise*offsetnoisetemp*offsetnoisetemp*rngsign1(noisexyt1);
  const float gainnoisevalue   = gainnoise*clampedcauchy(noisexyt3f,9);
  
  float detectorvalue = offset + offsetbiasvalue + offsetnoisevalue + gain*(1+gainbiasvalue+gainnoisevalue)*(shotnoisevalue+fluovalue);    
  
  #if defined FLOAT
   write_imagef  (imageout, (int2){x,y},  (float4){detectorvalue,0.0f,0.0f,0.0f});
  #elif defined UINT
   write_imageui (imageout, (int2){x,y}, (uint4){convert_ushort(detectorvalue),0.0f,0.0f,0.0f});
  #elif defined INT
   write_imagei  (imageout, (int2){x,y},  (uint4){convert_short(detectorvalue),0.0f,0.0f,0.0f});
  #endif
  
}

