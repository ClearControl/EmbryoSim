package simbryo.synthoscopy.optics.illumination;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.optics.OpticsBase;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class IlluminationOpticsBase extends OpticsBase
                                             implements
                                             IlluminationOpticsInterface<ClearCLImage>,
                                             AutoCloseable
{

  protected ClearCLImage mScatteringPhantomImage;

  /**
   * Instanciates a ClearCL powered illumination optics base class given a
   * ClearCL context, and the light map image dimensions.
   * 
   * @param pContext
   *          ClearCL context
   * @param pLightMapDimensions
   *          light map image dimensions
   */
  public IlluminationOpticsBase(final ClearCLContext pContext,
                                long... pLightMapDimensions)
  {
    super(pContext, pLightMapDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             getWidth(),
                                             getHeight(),
                                             getDepth());

    mImage.fillZero(true, false);
  }

  /**
   * Sets scattering phantom image.
   * 
   * @param pScatteringPhantomImage
   *          scattering phantom
   */
  public void setScatteringPhantom(ClearCLImage pScatteringPhantomImage)
  {
    if (mScatteringPhantomImage != pScatteringPhantomImage)
    {
      mScatteringPhantomImage = pScatteringPhantomImage;
      mScatteringPhantomImage.addListener((q, m) -> requestUpdate());
      requestUpdate();
    }
  }

}
