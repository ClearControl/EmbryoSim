package simbryo.synthoscopy.phantom;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.toIntExact;

import clearcl.ClearCLDevice;

/**
 * Utilities class containing usefull static methods for phantom rendering.
 *
 * @author royer
 */
public abstract class PhantomRendererUtils
{

  /**
   * Makes sure that the given image dimensions are compatible to the device in
   * terms of work group sizes.
   * 
   * @param pDevice
   *          device to adapt to
   * @param pImageDimensions
   *          image dimensions
   * @return possibly modified image dimensions
   */
  public static long[] adaptImageDimensionsToDevice(ClearCLDevice pDevice,
                                                    long[] pImageDimensions)
  {
    int[] lOptimalGridDimensions =
                                 getOptimalGridDimensions(pDevice,
                                                          pImageDimensions);

    long[] lNewDimensions = new long[pImageDimensions.length];

    for (int i = 0; i < pImageDimensions.length; i++)
      lNewDimensions[i] = (pImageDimensions[i]
                           / lOptimalGridDimensions[i])
                          * lOptimalGridDimensions[i];

    return lNewDimensions;
  }

  /**
   * Returns optimal grid dimensions for a given OpenCL device and stack
   * dimensions.
   *
   * 
   * @param pDevice
   *          ClearCL device
   * @param pStackDimensionsLong
   *          stack dimensions
   * @return array of grid dimensions
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               long... pStackDimensionsLong)
  {
    int[] lStackDimensionsInteger =
                                  new int[pStackDimensionsLong.length];
    for (int i = 0; i < lStackDimensionsInteger.length; i++)
      lStackDimensionsInteger[i] =
                                 toIntExact(pStackDimensionsLong[i]);
    return getOptimalGridDimensions(pDevice, lStackDimensionsInteger);
  }

  /**
   * Returns optimal grid dimensions for a given OpenCL device and stack
   * dimensions.
   *
   * 
   * @param pDevice
   *          ClearCL device
   * @param pStackDimensions
   *          stack dimensions
   * @return array of grid dimensions
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               int... pStackDimensions)
  {
    int lDimension = pStackDimensions.length;
    int[] lGridDimensions = new int[lDimension];

    long lMaxWorkGroupSize =
                           pDevice.getType()
                                  .isCPU() ? 1
                                           : pDevice.getMaxWorkGroupSize();

    int lMaxGridDim =
                    excludeNonOneOdd((int) floor(max(1,
                                                     min(8,
                                                         Math.pow(lMaxWorkGroupSize,
                                                                  1.0 / 3)))));

    for (int d = 0; d < lDimension; d++)
    {
      lGridDimensions[d] = pStackDimensions[d] / lMaxGridDim;
    }

    return lGridDimensions;
  }

  private static int excludeNonOneOdd(int x)
  {
    if (x == 1)
      return 1;
    if (x % 2 == 1)
      return x - 1;
    else
      return x;
  }

}
