
#include [OCLlib] "noise/noisetexture.cl"

float rngfloat1(uint x);



inline float autofluo(float3 dim, float3 voxelpos, sampler_t sampler, __read_only image3d_t  perlin, int timeindex )
{
  const float3 normpos = voxelpos/dim;
  const float3 centnormpos = normpos - 0.5f;
  const float3 axis = (float3){ELLIPSOIDA, ELLIPSOIDB, ELLIPSOIDC};
  const float3 scaledcentnormpos = centnormpos/axis;
  const float distance = fast_length(scaledcentnormpos)-(ELLIPSOIDR+2*NUCLEIRADIUS);
  
  const float insdistance = fmax(0.0f,-distance);
  
  const float insmask = smoothstep(0.00f, 0.001f, insdistance);

  if(insmask==0.0f)
    return 0.0f;

  const float4 noisepos       = (float4){normpos.x,normpos.y,normpos.z, 0.0f};
  //const float noiseval        = read_imagef(perlin, sampler, noisepos).x;
  const float noiseval        = fbm3(1, 250.0f*noisepos.xyz ,6);
  

  const float autoyolk       = 0.07f+0.8f*smoothstep(0.04f, 0.1f, insdistance)*(1-0.8f*smoothstep(0.1f, 0.15f, insdistance));
  
  const float autofluo = insmask * autoyolk * (0.3f+0.7f*noiseval);

  return autofluo;
}



