package simbryo.synthoscopy;

import coremem.ContiguousMemoryInterface;

/**
 * Synthoscopy interface. Each synthoscopy module produces a 2D or 3D image that
 * is then potentially consumed by a subsequent module
 *
 * @param <I>
 *          type of images used to store and process illumination-side images
 * @author royer
 */
public interface SynthoscopyInterface<I>
{

  /**
   * Returns the internal representation of the image that this modules produces
   * (2D or 3D). The type depends on the actual implementation.
   * 
   * @return image
   */
  I getImage();

  /**
   * Copies rendered image into memory region.
   * 
   * @param pMemory
   *          memory
   * @param pBlocking
   *          true blocks call until copy done, false for asynch copy. Note:
   *          Some implementations might not be capable of asynch copy.
   */
  void copyTo(ContiguousMemoryInterface pMemory, boolean pBlocking);

  /**
   * Renders the image given the current parameters and input images.
   * 
   * @param pWaitToFinish
   *          true -> wait to finish
   */
  public void render(boolean pWaitToFinish);

  /**
   * Clears image
   * 
   * @param pWaitToFinish
   *          true -> blocking call
   */
  public void clear(boolean pWaitToFinish);

  /**
   * Image width.
   * 
   * @return image width
   */
  long getWidth();

  /**
   * Image height.
   * 
   * @return image height
   */
  long getHeight();

  /**
   * Image depth.
   * 
   * @return image depth
   */
  long getDepth();

  /**
   * Returns a copy of the array holding the image dimensions
   * 
   * @return dimensions array
   */
  long[] getImageDimensions();

}
