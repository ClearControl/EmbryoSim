#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"


float autofluo(float3 dim, float3 voxelpos, sampler_t sampler, __read_only image3d_t  perlin, int timeindex );


#define INOISEDIM 1.0f/NOISEDIM
                                    
__kernel void hisrender(   __write_only    image3d_t  image,
                           __global const  int*       neighboors,
                           __global const  float*     positions,
                           __global const  float*     polarities,
                           __global const  float*     radii,
                                    const  float      intensity,
                                    const  int        timeindex,
                           __read_only     image3d_t  perlin                            
                          )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_NEAREST;

  __local int localneighboors[MAXNEI];
  __local float localpositions[3*MAXNEI];
  __local float localpolarities[3*MAXNEI];
  
  const uint width  = get_image_width(image);
  const uint height = get_image_height(image);
  const uint depth  = get_image_depth(image);
  const float3 dim = (float3){(float)width,(float)height,(float)depth};
  const float3 iaspectr = (float3){(float)width,(float)width,(float)width}/dim;
  
  const uint x = get_global_id(0); 
  const uint y = get_global_id(1);
  const uint z = get_global_id(2);
  
  const uint ox = get_global_offset(0); 
  const uint oy = get_global_offset(1);
  const uint oz = get_global_offset(2);
  
  const float3 voxelpos = (float3){(float)x,(float)y,(float)z};
  
  const uint ngx = get_num_groups(0);
  const uint ngy = get_num_groups(1);
  const uint ngz = get_num_groups(2);
  
  const uint lsx = get_local_size(0);
  const uint lsy = get_local_size(1);
  const uint lsz = get_local_size(2);
  
  const uint gx = get_group_id(0) + ox/lsx;
  const uint gy = get_group_id(1) + oy/lsy;
  const uint gz = get_group_id(2) + oz/lsz;

  const uint lx = get_local_id(0);
  const uint ly = get_local_id(1);
  const uint lz = get_local_id(2);
  
  const uint gi = gx+gy*ngx+gz*ngx*ngy;
  const uint li = lx+ly*lsx+lz*lsx*lsy;
 
  if(li<MAXNEI)
  {
    const int nei = neighboors[gi*MAXNEI+li];
    localneighboors[li] = nei;
    
    if(nei>=0)
    {
      const float3 partpos = vload3(nei,positions)*dim;
      vstore3(partpos,li,localpositions);
      const float3 partpol = vload3(nei,polarities);
      vstore3(partpol,li,localpolarities);
    }
    
  }
  
  barrier(CLK_LOCAL_MEM_FENCE);
 
  float value=0; 
 
  value += autofluo(dim, voxelpos, sampler, perlin, timeindex);
  value += NOISERATIO*rngfloat3(x+timeindex,y+timeindex,z+timeindex);    
 
  if(false && rngfloat3(x,y,z)<0.0001f)
  {
    value += 5.0f;
  }
  
  // we add a little bead for fun and work.
  if(false && x==100 && y==50)
  {
    value += 5.0f;
  }
 
   
  
  if(localneighboors[0]<0)
  {
    write_imagef (image, (int4){x,y,z,0.0f}, intensity*value);
    return;
  }
  
  const float nucleiradiusvoxels = NUCLEIRADIUS*width ;
  
  
  for(int k=0; k<MAXNEI; k++)
  {
    const int nei = localneighboors[k]; 
    if(nei>=0)
    {
      const float3 partpos     = vload3(k,localpositions);
      const float3 relvoxpos   = voxelpos-partpos;
      const float3 relvoxposac = relvoxpos*iaspectr;
      
      const float npnx = rngfloat1((2654435789*1)^nei);
      const float npny = rngfloat1((2654435789*2)^nei);
      const float npnz = rngfloat1((2654435789*3)^nei);
      const float4 npn = (float4){npnx,npny,npnz,0.0f};
      
      const float3 relvoxposacndim  = relvoxposac*INOISEDIM;
      const float4 normrelvoxposac  = (float4){relvoxposacndim.x, relvoxposacndim.y, relvoxposacndim.z, 0.0f};
      const float4 noisepos         = 0.5f+normrelvoxposac+npn;
      const float  levelnoise       = 2.0f*read_imagef(perlin, sampler, noisepos).x-1.0f;
      
      const float3 partpol     = vload3(k,localpolarities);
      const float cosval = fabs(dot(partpol,relvoxposac)/(fast_length(relvoxposac)));
      const float eccentricity = -(0.5f*cosval*cosval)*nucleiradiusvoxels;
      
      const float d      = fast_length(relvoxposac) + eccentricity; 
      const float noisyd = d + NUCLEIROUGHNESS*levelnoise;
      
      //const float level      =  native_recip(1.0f+native_exp2(NUCLEISHARPNESS*(noisyd-nucleiradiusvoxels)));
      const float level      =  1.0f-smoothstep(nucleiradiusvoxels,nucleiradiusvoxels*(2.0f-NUCLEISHARPNESS), noisyd);
      const float noisylevel =  (1.0f+NUCLEITEXTURECONTRAST*levelnoise)*level;
       
      value += noisylevel;
    }
  }
    
 
    
  write_imagef (image, (int4){x,y,z,0}, intensity*value);

}

