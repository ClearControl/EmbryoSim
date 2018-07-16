package simbryo.synthoscopy.camera;

import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.ClearCLSynthoscopyBase;

/**
 * Camera model base class for camera models computation based on CLearCL
 *
 * @author royer
 */
public abstract class ClearCLCameraRendererBase extends
                                                ClearCLSynthoscopyBase
                                                implements
                                                CameraRendererInterface<ClearCLImage>

{
  /**
   * normal camera exposure
   */
  public static final float cNormalExposure = 0.020f;

  protected ClearCLImage mDetectionImage;

  protected Vector3f mDetectionDownUpVector = new Vector3f();

  /**
   * Instanciates a ClearCL powered detection optics base class given a ClearCL
   * context, and idefield image dimensions (2D).
   * 
   * @param pContext
   *          ClearCL context
   * @param pMaxCameraImageDimensions
   *          max camera image dimensions
   * @param pDataType
   *          data type for rendered image
   */
  public ClearCLCameraRendererBase(final ClearCLContext pContext,
                                   ImageChannelDataType pDataType,
                                   long... pMaxCameraImageDimensions)
  {
    super(pContext, false, pDataType, pMaxCameraImageDimensions);

    mContext = pContext;

  }

  /**
   * Sets detection image
   * 
   * @param pDetectionImage
   *          detection image
   */
  public void setDetectionImage(ClearCLImage pDetectionImage)
  {
    if (mDetectionImage != pDetectionImage)
    {
      mDetectionImage = pDetectionImage;
      requestUpdate();
    }
  }

  /**
   * Sets detection down-to-up vector. Inputs are automatically normalized. The
   * down-to-up vector
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setDetectionDownUpVector(float pX, float pY, float pZ)
  {
    mDetectionDownUpVector.x = pX;
    mDetectionDownUpVector.y = pY;
    mDetectionDownUpVector.z = pZ;

    mDetectionDownUpVector.normalize();
  }

  /**
   * Sets detection down-to-up vector. Inputs are automatically normalized.
   * 
   * @param pAxisVector
   *          axis vector
   * 
   */
  public void setDetectionDownUpVector(Vector3f pAxisVector)
  {
    mDetectionDownUpVector.x = pAxisVector.x;
    mDetectionDownUpVector.y = pAxisVector.y;
    mDetectionDownUpVector.z = pAxisVector.z;

    mDetectionDownUpVector.normalize();
  }

  /**
   * Returns detection down-to-up vector.
   * 
   * @return normal vector
   */
  public Vector3f getDetectionDownUpVector()
  {
    return mDetectionDownUpVector;
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    // not doing anything here, derived classes must actually compute something
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    clearUpdate();
  }

}
