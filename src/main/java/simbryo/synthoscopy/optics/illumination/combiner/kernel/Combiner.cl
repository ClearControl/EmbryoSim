
#include [OCLlib] "imageops/imageops.cl" 


__kernel void combine2(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __write_only   image3d_t  imagedest
                     )
{
  add2(image0,image1,1.0f,1.0f,imagedest);
}

__kernel void combine3(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int4 pos = (int4){x,y,z,0};

  const float a = read_imagef(image0, pos).x;
  const float b = read_imagef(image1, pos).x;
  const float c = read_imagef(image2, pos).x;

  const float value = a + b + c;

  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}

__kernel void combine4(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __write_only   image3d_t  imagedest
                     )
{
  add4(image0,image1,image2,image3,1.0f,1.0f,1.0f,1.0f,imagedest);
}


__kernel void combine5(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int4 pos = (int4){x,y,z,0};

  const float a = read_imagef(image0, pos).x;
  const float b = read_imagef(image1, pos).x;
  const float c = read_imagef(image2, pos).x;
  const float d = read_imagef(image3, pos).x;
  const float e = read_imagef(image4, pos).x;

  const float value = a + b + c + d + e;

  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}


__kernel void combine6(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __read_only    image3d_t  image5,
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int4 pos = (int4){x,y,z,0};

  const float a = read_imagef(image0, pos).x;
  const float b = read_imagef(image1, pos).x;
  const float c = read_imagef(image2, pos).x;
  const float d = read_imagef(image3, pos).x;
  const float e = read_imagef(image4, pos).x;
  const float f = read_imagef(image5, pos).x;

  const float value = a + b + c + d + e + f;

  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}


__kernel void combine7(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __read_only    image3d_t  image5,
                           __read_only    image3d_t  image6,
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int4 pos = (int4){x,y,z,0};

  const float a = read_imagef(image0, pos).x;
  const float b = read_imagef(image1, pos).x;
  const float c = read_imagef(image2, pos).x;
  const float d = read_imagef(image3, pos).x;
  const float e = read_imagef(image4, pos).x;
  const float f = read_imagef(image5, pos).x;
  const float g = read_imagef(image6, pos).x;

  const float value = a + b + c + d + e + f + g;

  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}


__kernel void combine8(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __read_only    image3d_t  image5,
                           __read_only    image3d_t  image6,
                           __read_only    image3d_t  image7,
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int4 pos = (int4){x,y,z,0};

  const float a = read_imagef(image0, pos).x;
  const float b = read_imagef(image1, pos).x;
  const float c = read_imagef(image2, pos).x;
  const float d = read_imagef(image3, pos).x;
  const float e = read_imagef(image4, pos).x;
  const float f = read_imagef(image5, pos).x;
  const float g = read_imagef(image6, pos).x;
  const float h = read_imagef(image6, pos).x;

  const float value = a + b + c + d + e + f + g + h;

  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}