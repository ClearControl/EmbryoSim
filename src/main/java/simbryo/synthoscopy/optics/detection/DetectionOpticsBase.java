package simbryo.synthoscopy.optics.detection;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.optics.OpticsBase;

/**
 * Detection optics base class for detection optics computation based on CLearCL
 *
 * @author royer
 */
public abstract class DetectionOpticsBase extends OpticsBase
                                          implements
                                          DetectionOpticsInterface<ClearCLImage>,
                                          AutoCloseable
{

  protected ClearCLImage mFluorescencePhantomImage;
  protected ClearCLImage mScatteringPhantomImage;
  protected ClearCLImage mLightMapImage;

  /**
   * Instanciates a ClearCL powered detection optics base class given a ClearCL
   * context, and widefield image dimensions (2D).
   * 
   * @param pContext
   *          ClearCL context
   * @param pWideFieldImageDimensions
   *          widefield map image dimensions
   */
  public DetectionOpticsBase(final ClearCLContext pContext,
                             long... pWideFieldImageDimensions)
  {
    super(pContext, pWideFieldImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             getWidth(),
                                             getHeight());

    mImage.fillZero(true, false);
  }

  /**
   * Sets fluorescence phantom image.
   * 
   * @param pFluorescencePhantomImage
   *          fluorescence phantom image
   */
  public void setFluorescencePhantomImage(ClearCLImage pFluorescencePhantomImage)
  {
    if (mFluorescencePhantomImage != pFluorescencePhantomImage)
    {
      mFluorescencePhantomImage = pFluorescencePhantomImage;
      mFluorescencePhantomImage.addListener((q,
                                             m) -> requestUpdate());
      requestUpdate();
    }
  }

  /**
   * Sets scattering phantom image
   * 
   * @param pScatteringPhantomImage
   *          scattering phantom image
   */
  public void setScatteringPhantomImage(ClearCLImage pScatteringPhantomImage)
  {
    if (mScatteringPhantomImage != pScatteringPhantomImage)
    {
      mScatteringPhantomImage = pScatteringPhantomImage;
      mScatteringPhantomImage.addListener((q, m) -> requestUpdate());
      requestUpdate();
    }
  }

  /**
   * Sets lightmap image
   * 
   * @param pLightMapImage
   *          lightmap
   */
  public void setLightMapImage(ClearCLImage pLightMapImage)
  {
    if (mLightMapImage != pLightMapImage)
    {
      mLightMapImage = pLightMapImage;
      mLightMapImage.addListener((q, m) -> requestUpdate());
      requestUpdate();
    }
  }

}
