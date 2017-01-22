package simbryo.synthoscopy.illumination;

import static java.lang.Math.round;

import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.phantom.PhantomRendererInterface;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class ClearCLIlluminationOpticsBase extends
                                                    IlluminationOpticsBase<ClearCLImage>
                                                    implements
                                                    IlluminationOpticsInterface<ClearCLImage>,
                                                    AutoCloseable
{

  protected ClearCLContext mContext;
  protected ClearCLImage mLightMapImage;

  /**
   * Instanciates a ClearCL powered illumination optics base class given a
   * ClearCL context, phantom renderer, and light map image dimensions
   * 
   * @param pContext ClearCL context
   * @param pLightMapDimensions light map image dimensions
   */
  public ClearCLIlluminationOpticsBase(final ClearCLContext pContext,
                                       long... pLightMapDimensions)
  {
    super(pLightMapDimensions);

    mContext = pContext;

    mLightMapImage =
                   mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                     getWidth(),
                                                     getHeight(),
                                                     getDepth());

    mLightMapImage.fillZero(true);
  }

  @Override
  public ClearCLImage getLightMapImage()
  {
    return mLightMapImage;
  }

  @Override
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage, int pZCenterPlaneIndex)
  {
    // not doing anything here, derived classes must actually cmpute something into mLightMapImage
    return mLightMapImage;
  }
 

  @Override
  public void clear()
  {
    mLightMapImage.fillZero(true);
  }

  public void close()
  {
    mLightMapImage.close();
  }
  
  /**
   * Opens viewer for the internal image
   * 
   * @return viewer
   */
  public ClearCLImageViewer openViewer()
  {
    final ClearCLImageViewer lViewImage =
                                        ClearCLImageViewer.view(mLightMapImage);
    return lViewImage;
  }

}
