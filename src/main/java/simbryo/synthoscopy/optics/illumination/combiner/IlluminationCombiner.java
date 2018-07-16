package simbryo.synthoscopy.optics.illumination.combiner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.optics.OpticsBase;
import simbryo.synthoscopy.optics.illumination.IlluminationOpticsInterface;

/**
 * Ilumination combiner can add up several lightmap images into a single
 * lightmap
 *
 * @author royer
 * @param <I>
 *          illumination type
 */
public class IlluminationCombiner<I extends IlluminationOpticsInterface<ClearCLImage>>
                                 extends OpticsBase implements
                                 IlluminationOpticsInterface<ClearCLImage>,
                                 AutoCloseable
{

  private final ArrayList<I> mListOfIlluminationOptics =
                                                       new ArrayList<>();

  private ClearCLKernel mRenderKernel;

  /**
   * Instanciates a ClearCL powered illumination combiner ClearCL context,
   * andthe list of illumination optics to combine.
   * 
   * @param pContext
   *          ClearCL context
   * @param pIlluminationOptics
   *          list of illumination optics to combine
   * @throws IOException
   *           thrown if kernel source file cannot be read
   */
  public IlluminationCombiner(final ClearCLContext pContext,
                              List<I> pIlluminationOptics) throws IOException
  {
    super(pContext, pIlluminationOptics.get(0).getImageDimensions());

    mContext = pContext;

    for (I lIllumination : pIlluminationOptics)
      mListOfIlluminationOptics.add(lIllumination);

    int lNumberOfLightMapsToCombine = getNumberOfLightMapsToCombine();

    if (lNumberOfLightMapsToCombine == 1)
      mImage = mListOfIlluminationOptics.get(0).getImage();
    else
    {
      mImage =
             mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                               pIlluminationOptics.get(0)
                                                                  .getWidth(),
                                               pIlluminationOptics.get(0)
                                                                  .getHeight(),
                                               pIlluminationOptics.get(0)
                                                                  .getDepth());
      mImage.fillZero(true, false);
    }

    setupProgramAndKernel();
  }

  protected int getNumberOfLightMapsToCombine()
  {
    return mListOfIlluminationOptics.size();
  }

  protected void setupProgramAndKernel() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(IlluminationCombiner.class,
                       "kernel/Combiner.cl");
    lProgram.buildAndLog();

    int lNumberOfLightMapsToCombine = getNumberOfLightMapsToCombine();

    if (lNumberOfLightMapsToCombine == 1)
    {

    }
    else if (lNumberOfLightMapsToCombine == 2)
    {
      mRenderKernel = lProgram.createKernel("combine2");
      mRenderKernel.setArgument("image0",
                                mListOfIlluminationOptics.get(0)
                                                         .getImage());
      mRenderKernel.setArgument("image1",
                                mListOfIlluminationOptics.get(1)
                                                         .getImage());
    }
    else if (lNumberOfLightMapsToCombine == 4)
    {
      mRenderKernel = lProgram.createKernel("combine4");
      mRenderKernel.setArgument("image0",
                                mListOfIlluminationOptics.get(0)
                                                         .getImage());
      mRenderKernel.setArgument("image1",
                                mListOfIlluminationOptics.get(1)
                                                         .getImage());
      mRenderKernel.setArgument("image2",
                                mListOfIlluminationOptics.get(2)
                                                         .getImage());
      mRenderKernel.setArgument("image3",
                                mListOfIlluminationOptics.get(3)
                                                         .getImage());
    }
    else
    {
      mRenderKernel =
                    lProgram.createKernel("combine"
                                          + lNumberOfLightMapsToCombine);
      for (int i = 0; i < lNumberOfLightMapsToCombine; i++)
      {
        mRenderKernel.setArgument("image" + i,
                                  mListOfIlluminationOptics.get(i)
                                                           .getImage());
      }

    }

    if (lNumberOfLightMapsToCombine > 1 && mRenderKernel != null)
    {
      mRenderKernel.setArgument("imagedest", getImage());
    }
  }

  @Override
  public void clear(boolean pWaitToFinish)
  {
    if (getNumberOfLightMapsToCombine() > 1)
      mImage.fillZero(pWaitToFinish, false);
    super.clear(pWaitToFinish);
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (getNumberOfLightMapsToCombine() == 1 || mRenderKernel == null)
      return;

    mRenderKernel.setGlobalOffsets(0, 0, 0);
    mRenderKernel.setGlobalSizes(getWidth(), getHeight(), getDepth());
    mRenderKernel.run(pWaitToFinish);

    super.render(pWaitToFinish);
  }

}
