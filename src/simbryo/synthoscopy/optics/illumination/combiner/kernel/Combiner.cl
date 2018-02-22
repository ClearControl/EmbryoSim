
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
                           __read_write   image3d_t  imagedest
                     )
{
  add2(image0,image1,1.0f,1.0f,imagedest);
  add2(imagedest,image2,1.0f,1.0f,imagedest);
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
                           __read_write   image3d_t  imagedest
                     )
{
  add4(image0,image1,image2,image3,1.0f,1.0f,1.0f,1.0f,imagedest);
  add2(imagedest,image4,1.0f,1.0f,imagedest);
}


__kernel void combine6(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __read_only    image3d_t  image5,
                           __read_write   image3d_t  imagedest
                     )
{
  add4(image0,image1,image2,image3,1.0f,1.0f,1.0f,1.0f,imagedest);
  add2(imagedest,image4,1.0f,1.0f,imagedest);
  add2(imagedest,image5,1.0f,1.0f,imagedest);
}


__kernel void combine7(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __read_only    image3d_t  image4,
                           __read_only    image3d_t  image5,
                           __read_only    image3d_t  image6,
                           __read_write   image3d_t  imagedest
                     )
{
  add4(image0,image1,image2,image3,1.0f,1.0f,1.0f,1.0f,imagedest);
  add4(imagedest,image4,image5,image6,1.0f,1.0f,1.0f,1.0f,imagedest);
}