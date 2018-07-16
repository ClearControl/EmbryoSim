#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"
#include [OCLlib] "linear/matrix.cl" 
  
  
#define KS 1  
   
inline float lightsheetfun( const float lambda,
                            const float intensity,
                            const float w0,
                            const float lsheight,  
                            const float4 lsp, 
                            const float4 lsa,
                            const float4 lsn, 
                            const float4 pos)
{
  const float4 rel = pos-lsp;
  const float x = dot(lsa,rel);
  const float y = dot(cross(lsa,lsn),rel);
  const float z = fabs(dot(lsn,rel)); // + fast_length(rel.yz);
  
  const float w02  = w0*w0;
  const float nx = (x*lambda)/(M_PI*w02);
  const float wx2 = w02*(1.0f+nx*nx); 
  const float a2 = w02/wx2;
  const float b2 = (z*z)/wx2;
  
  const float sheet = intensity*a2*native_exp(-2*b2);
  
  const float value = sheet*(1-smoothstep(0.5f*lsheight, 0.5f*lsheight+0.01f,fabs(y)));
  
  return value;
}   
   
                                
__kernel void propagate(   __read_only    image3d_t  scatterphantom,
                           __constant     float*     matrix,
                           __write_only   image3d_t  lightmapout,
                           __read_only    image2d_t  binput,
                           __write_only   image2d_t  boutput,
                           __read_only    image2d_t  sinput,
                           __write_only   image2d_t  soutput,
                           const          int        x,
                           const          float      lambda,
                           const          float      intensity,
                           const          float      scatterconstant,                           
                           const          float      scatterloss,
                           const          float      sigmamin,
                           const          float      sigmamax,
                           const          float      w0,
                           const          float      lsheight,
                           const          float      lspx,
                           const          float      lspy,
                           const          float      lspz,
                           const          float      lsax,
                           const          float      lsay,
                           const          float      lsaz,
                           const          float      lsnx,
                           const          float      lsny,
                           const          float      lsnz
                       )
{
  const sampler_t normsamplerclamp     = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP         | CLK_FILTER_LINEAR;
  const sampler_t normsamplerclampedge = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler           = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const float16 matrix16  = matrix_load(0, matrix);
 
  const int lmwidth  = get_image_width(lightmapout);
  const int lmheight = get_image_height(lightmapout);
  const int lmdepth  = get_image_depth(lightmapout);
  const float4 lmdim = (float4){(float)lmwidth,(float)lmheight,(float)lmdepth, 1.0f};
  const float4 ilmdim = 1.0f/lmdim;
 
  const int smwidth  = get_image_width(scatterphantom);
  const int smheight = get_image_height(scatterphantom);
  const int smdepth  = get_image_depth(scatterphantom);
  const float4 smdim = (float4){(float)smwidth,(float)smheight,(float)smdepth, 1.0f};

  
  const float4 lsp    = (float4){lspx, lspy, lspz, 0.0f};
  const float4 lspvox = lsp*smdim; //note: aspect ratio?
  const float4 lsa    = (float4){lsax, lsay, lsaz, 0.0f};
  const float4 lsavox = lsa*smdim; //note: aspect ratio?
  const float4 lsn    = (float4){lsnx, lsny, lsnz, 0.0f};
  const float4 lsnvox = lsn*smdim; //note: aspect ratio?
  
  const int y = get_global_id(0); 
  const int z = get_global_id(1);
  
  const int height = get_global_size(0); 
  const int depth  = get_global_size(1);
  
  const int oy = get_global_offset(0); 
  const int oz = get_global_offset(1);
   
  const float2 dvec  = (float2){-lsay/fabs(lsax), -lsaz/fabs(lsax)}*ilmdim.yz;
  
  const float2 halfshift = (float2){0.5f,0.5f}*ilmdim.yz;

  const float4 pos = ((float4){0.5f, 0.5f, 0.5f, 0.0f}+(float4){(float)x,(float)y,(float)z, 0.0f})*ilmdim;
  
  //printf("lsa(%f,%f,%f)\n",lsa.x, lsa.y, lsa.z);
  //printf("lsn(%f,%f,%f)\n",lsn.x, lsn.y, lsn.z);
  
  
  // formula for ballistic light distribution of lightsheet:
  const float ballistic0 =  lightsheetfun(lambda, intensity, w0, lsheight, lsp, lsa, lsn, pos);
  
  // [READ IMAGE] proportion of ballistic light that made it through in previous planes:
  const float oldballisticratio =  read_imagef(binput, normsamplerclampedge, dvec+halfshift+(float2){y,z}*ilmdim.yz).x;
 
  // scattering map value at voxel: 
  const float scattermapvalue = trans_read_imagef(scatterphantom, normsamplerclampedge, matrix16, pos).x;
  
  // local transfer from ballistic light to scattered light: (0 -> no transfer, 1 -> max transfer)
  const float transfer = 1.0f-native_exp2(-(scatterconstant/lmwidth)*scattermapvalue);
  
  // updated ballistic ratio:
  const float ballisticratio = oldballisticratio*(1.0f-transfer);
  
  // [WRITE IMAGE] update ballistic light ratio map:
  write_imagef (boutput, (int2){y,z}, ballisticratio);
  
  // amount of ballistic light at current voxel:
  const float ballistic = ballistic0 * ballisticratio;
  
  // amount of ballistic light transfered to scattering at current voxel:
  const float transferredlight = transfer*oldballisticratio*ballistic0;
  
  // scattering sigma has a min and a part modulated by density of scattering media:
  const float sigma = sigmamin + transfer*(sigmamax-sigmamin);
  
  // [READ IMAGE] collect light from previous plane that diffused to current voxel:
  // Note: turns out its slower to do separable-diffusion, because of the image cache - 
  // its better to touch image memory once instead of in multiple passes, might not be
  // true for bigger kernels. Strange.
  float previouslyscattered = 0.0f;
  for(  int iz=z-KS; iz<=z+KS; iz++)
    for(int iy=y-KS; iy<=y+KS; iy++)
    {
      previouslyscattered += read_imagef(sinput, normsamplerclampedge, dvec+halfshift+(float2){(float)iy,(float)iz}*ilmdim.yz).x;
    }
  previouslyscattered *= sigma; // weight /**/
  
  // [READ IMAGE] collect scattered light from previous plane without diffusion:
  previouslyscattered +=  (1-sigma)*read_imagef(sinput, normsamplerclampedge, dvec+halfshift+(float2){(float)y,(float)z}*ilmdim.yz).x;
  
  // normalize to have conservation from one plane to the next:
  previouslyscattered *= 1.0f/((2*KS+1)*(2*KS+1)*sigma+(1-sigma));
  
  // we add the light that was locally transfered from ballistic to scattered:
  const float scattered = scatterloss*previouslyscattered + transferredlight;
  
  // [WRITE IMAGE] update diffused light map:
  write_imagef (soutput, (int2){y,z}, scattered);

  // Light at a given point in space is the sum of the scattered and ballistic light:
  const float light = ballistic + scattered; // ballistic + scattered;
 
  // which voxel to read/write to:
  const int4 voxelcoord = (int4){x,y,z,0};

  // [WRITE IMAGE] Write lightmap value:
  write_imagef(lightmapout, voxelcoord, light); 

}

