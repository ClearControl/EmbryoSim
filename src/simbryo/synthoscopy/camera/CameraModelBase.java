package simbryo.synthoscopy.camera;

import simbryo.synthoscopy.OpticsBase;

/**
 * Camera model base class providing common fields and methods required by all
 * implementors of the camera model interface
 *
 * @param <I> type of image used to store and process camera images
 * @author royer
 */
public abstract class CameraModelBase<I> extends OpticsBase<I>
                                     implements
                                     CameraModelInterface<I>
{

  private long mWidth, mHeight;

  @Override
  public long getWidth()
  {
    return mWidth;
  }

  @Override
  public long getHeight()
  {
    return mHeight;
  }

}
