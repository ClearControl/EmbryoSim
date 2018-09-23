package simbryo.synthoscopy.camera.impl;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.*;
import simbryo.synthoscopy.camera.CameraRendererInterface;
import simbryo.synthoscopy.camera.ClearCLCameraRendererBase;

/**
 * Generic camera model for sCMOS cameras
 *
 * @author royer
 */
public class SCMOSCameraRenderer extends ClearCLCameraRendererBase
                                 implements
                                 CameraRendererInterface<ClearCLImage>
{

  protected ClearCLKernel mUpscaleKernel, mNoiseKernel;
  protected ClearCLImage mImageTemp;

  private volatile int mTimeIndex = 0;

  private volatile int mXMin, mXMax, mYMin, mYMax;

  private volatile float mExposureInSeconds, mShotNoise, mOffset,
      mGain, mOffsetNoise, mGainNoise, mOffsetBias, mGainBias;

  private volatile float mShiftX, mShiftY, mMagnification;

  /**
   * Instantiates a light sheet illumination optics class given a ClearCL
   * context, and light map image dimensions
   * 
   * @param pContext
   *          OpenCL context
   *
   * @param pMaxCameraImageDimensions
   *          max camera image dimensions in voxels
   * 
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public SCMOSCameraRenderer(ClearCLContext pContext,
                             long... pMaxCameraImageDimensions) throws IOException
  {
    super(pContext,
          ImageChannelDataType.UnsignedInt16,
          pMaxCameraImageDimensions);

    setExposure(cNormalExposure);
    setMagnification(1);
    setShiftX(0);
    setShiftY(0);

    setShotNoise(0.02f);
    setOffset(97.0f);
    setGain(200.0f);
    setOffsetBias(1.0f);
    setGainBias(0.05f);
    setOffsetNoise(1.0f);
    setGainNoise(0.04f);

    mXMin = 0;
    mXMax = (int) pMaxCameraImageDimensions[0];
    mYMin = 0;
    mYMax = (int) pMaxCameraImageDimensions[1];

    ensureImagesAllocated();
    setupProgramAndKernels();
    clearImages(true);
  }

  /**
   * Returns the exposure in seconds
   * 
   * @return exposure in seconds
   */
  public float getExposure()
  {
    return mExposureInSeconds;
  }

  /**
   * Sets the exposure parameter.
   * 
   * @param pExposureInSeconds
   *          exposure in seconds
   */
  public void setExposure(float pExposureInSeconds)
  {
    if (mExposureInSeconds != pExposureInSeconds)
    {
      mExposureInSeconds = pExposureInSeconds;
      requestUpdate();
    }
  }

  /**
   * Returns the magnification
   * 
   * @return magnification
   */
  public float getMagnification()
  {
    return mMagnification;
  }

  /**
   * Sets the magnification.
   * 
   * @param pMagnification
   *          magnification
   */
  public void setMagnification(float pMagnification)
  {
    if (mMagnification != pMagnification)
    {
      mMagnification = pMagnification;
      requestUpdate();
    }
  }

  /**
   * Returns the camera image shift along x
   * 
   * @return exposure in seconds
   */
  public float getShiftX()
  {
    return mShiftX;
  }

  /**
   * Sets the camera image shift along x
   * 
   * @param pShiftX
   *          shift x
   */
  public void setShiftX(float pShiftX)
  {
    if (mShiftX != pShiftX)
    {
      mShiftX = pShiftX;
      requestUpdate();
    }
  }

  /**
   * Returns the camera image shift along y
   * 
   * @return exposure in seconds
   */
  public float getShiftY()
  {
    return mShiftY;
  }

  /**
   * Sets the camera image shift along y
   * 
   * @param pShiftY
   *          shift y
   */
  public void setShiftY(float pShiftY)
  {
    if (mShiftY != pShiftY)
    {
      mShiftY = pShiftY;
      requestUpdate();
    }
  }

  /**
   * Returns shot noise parameter.
   * 
   * @return shot noise parameter
   */
  public float getShotNoise()
  {
    return mShotNoise;
  }

  /**
   * Sets shot noise parameter. This parameter should be a floating point number
   * within [0,1].
   * 
   * @param pShotNoise
   *          shot noise parameter within [0,1]
   */
  public void setShotNoise(float pShotNoise)
  {
    if (mShotNoise != pShotNoise)
    {
      mShotNoise = pShotNoise;
      requestUpdate();
    }
  }

  /**
   * Returns signal amplification offset.
   * 
   * @return signal amplification offset.
   */
  public float getOffset()
  {
    return mOffset;
  }

  /**
   * Sets signal amplification offset. This is a in y=ax+b where x is the
   * fluorescence signal and y is the resulting electronic-amplified signal.
   * 
   * @param pOffset
   *          signal amplification offset.
   */
  public void setOffset(float pOffset)
  {
    if (mOffset != pOffset)
    {
      mOffset = pOffset;
      requestUpdate();
    }
  }

  /**
   * Returns signal amplification gain.
   * 
   * @return signal amplification gain.
   */
  public float getGain()
  {
    return mGain;
  }

  /**
   * Sets signal amplification gain. This is a in y=ax+b where x is the
   * fluorescence signal and y is the resulting electronic-amplified signal.
   * 
   * @param pGain
   *          signal amplification gain.
   */
  public void setGain(float pGain)
  {
    if (mGain != pGain)
    {
      mGain = pGain;
      requestUpdate();
    }
  }

  /**
   * Returns signal amplification offset noise amplitude.
   * 
   * @return signal amplification offset noise amplitude.
   */
  public float getOffsetNoise()
  {
    return mOffsetNoise;
  }

  /**
   * Sets signal amplification offset noise amplitude. Offset noise is modeled
   * as a χ^2(k=1) (Chi-square distribution with 1 degree of freedom)
   * 
   * @param pOffsetNoise
   *          signal amplification offset noise amplitude
   */
  public void setOffsetNoise(float pOffsetNoise)
  {
    if (mOffsetNoise != pOffsetNoise)
    {
      mOffsetNoise = pOffsetNoise;
      requestUpdate();
    }
  }

  /**
   * Returns signal amplification gain noise amplitude.
   * 
   * @return signal amplification gain noise amplitude.
   */
  public float getGainNoise()
  {
    return mGainNoise;
  }

  /**
   * Sets signal amplification gain noise amplitude. Offset gain is as a
   * zero-mean Gaussian distribution of given amplitude. The gain is modulated
   * additively by the noise.
   * 
   * @param pGainNoise
   *          signal amplification offset noise amplitude
   */
  public void setGainNoise(float pGainNoise)
  {
    if (mGainNoise != pGainNoise)
    {
      mGainNoise = pGainNoise;
      requestUpdate();
    }
  }

  /**
   * Returns the offset bias amplitude
   * 
   * @return offset bias amplitude
   */
  public float getOffsetBias()
  {
    return mOffsetBias;
  }

  /**
   * Sets the offset bias amplitude. Each pixel of the detector has a
   * non-time-varying noise component for the electronic offset which only
   * varies across pixels. This noise is modeled as a zero-mean Gaussian of
   * given amplitude.
   * 
   * @param pOffsetBias
   *          offset bias amplitude
   */
  public void setOffsetBias(float pOffsetBias)
  {
    if (mOffsetBias != pOffsetBias)
    {
      mOffsetBias = pOffsetBias;
      requestUpdate();
    }
  }

  /**
   * Returns the offset bias amplitude. Each pixel of the detector has a
   * non-time-varying noise component for the electronic gain which only varies
   * across pixels. This noise is modeled as a zero-mean Gaussian of given
   * amplitude.
   * 
   * @return offset bias amplitude
   */
  public float getGainBias()
  {
    return mGainBias;
  }

  /**
   * Sets the gain bias amplitude
   * 
   * @param pGainBias
   *          gain bias amplitude
   */
  public void setGainBias(float pGainBias)
  {
    if (mGainBias != pGainBias)
    {
      mGainBias = pGainBias;
    }
  }

  /**
   * Increment time index.
   */
  public void incrementTimeIndex()
  {
    mTimeIndex++;
  }

  /**
   * Sets Region-Of-Interest [xmin,xmax]x[ymin,ymax]
   * 
   * @param pXMin
   *          x min
   * @param pXMax
   *          x max
   * @param pYMin
   *          y min
   * @param pYMax
   *          y max
   */
  public void setROI(int pXMin, int pXMax, int pYMin, int pYMax)
  {
    if (mXMin != pXMin || mXMax != pXMax
        || mYMin != pYMin
        || mYMax != pYMax)
    {

      mXMin = pXMin;
      mXMax = pXMax;
      mYMin = pYMin;
      mYMax = pYMax;

      requestUpdate();
    }
  }

  /**
   * Sets a centered Region-Of-Interest (center of ROI coincides with center of
   * detector)
   * 
   * @param pWidth
   *          width of ROI
   * @param pHeight
   *          height of ROI
   */

  public void setCenteredROI(int pWidth, int pHeight)
  {
    int lMarginX = (int) ((getMaxWidth() - pWidth) / 2);
    int lMarginY = (int) ((getMaxHeight() - pHeight) / 2);
    setROI(lMarginX,
           (int) (getMaxWidth() - lMarginX),
           lMarginY,
           (int) (getMaxHeight() - lMarginY));
  }

  /**
   * Sets a centered Region-Of-Interest with XY offset (center of ROI coincides
   * with center of detector)
   * 
   * @param pXOffset
   *          x offset
   * @param pYOffset
   *          y offset
   * @param pWidth
   *          width of ROI
   * @param pHeight
   *          height of ROI
   */

  public void setCenteredROI(int pXOffset,
                             int pYOffset,
                             int pWidth,
                             int pHeight)
  {
    int lMarginX = (int) ((getMaxWidth() - pWidth) / 2);
    int lMarginY = (int) ((getMaxHeight() - pHeight) / 2);

    int xmin = clamp(lMarginX + pXOffset, 0, getMaxWidth());
    int xmax = clamp((int) (getMaxWidth() - lMarginX) + pXOffset,
                     0,
                     getMaxWidth());
    int ymin = clamp(lMarginY + pYOffset, 0, getMaxHeight());
    int ymax = clamp((int) (getMaxHeight() - lMarginY) + pYOffset,
                     0,
                     getMaxHeight());

    setROI(xmin, xmax, ymin, ymax);
  }

  private int clamp(int pValue, int pMin, long pMax)
  {
    return (int) Math.max(Math.min(pValue, pMax), pMin);
  }

  @Override
  public long getWidth()
  {
    return mXMax - mXMin;
  }

  @Override
  public long getHeight()
  {
    return mYMax - mYMin;
  }

  /**
   * Returns the camera max pixel width
   * 
   * @return camera max pixel width
   */
  public long getMaxWidth()
  {
    return super.getWidth();
  }

  /**
   * Returns the camera max pixel height
   * 
   * @return camera max pixel height
   */
  public long getMaxHeight()
  {
    return super.getHeight();
  }

  private void ensureImagesAllocated()
  {
    if (mImage != null && mImageTemp != null
        && mImage.getWidth() == getWidth()
        && mImage.getHeight() == getHeight())
      return;

    ClearCLImage lImage = mImage;
    ClearCLImage lImageTemp = mImageTemp;

    mImage = mContext.createImage(MemAllocMode.Best,
                                  HostAccessType.ReadWrite,
                                  KernelAccessType.ReadWrite,
                                  ImageChannelOrder.R,
                                  mImage.getChannelDataType(),
                                  new long[]
                                  { getWidth(), getHeight() });
    // mContext.createSingleChannelImage(mImage.getChannelDataType(),
    // getWidth(),
    // getHeight());

    mImageTemp = mContext.createImage(mImage,
                                      ImageChannelDataType.Float);

    if (mViewImage != null)
      mViewImage.setImage(mImage);

    if (lImage != null)
      lImage.close();
    if (lImageTemp != null)
      lImageTemp.close();

  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(SCMOSCameraRenderer.class,
                       "kernel/CameraImage.cl");

    lProgram.addBuildOptionAllMathOpt();
    lProgram.addDefineForDataType(mImage.getChannelDataType());
    lProgram.buildAndLog();

    mUpscaleKernel = lProgram.createKernel("upscale");
    mNoiseKernel = lProgram.createKernel("camnoise");
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (!isUpdateNeeded())
      return;

    ensureImagesAllocated();
    clearImages(false);
    setInvariantKernelParameters(mDetectionImage);
    upscale(mDetectionImage, mImageTemp, false);
    noise(mImageTemp, mImage, pWaitToFinish);
    incrementTimeIndex();

    super.render(pWaitToFinish);
  }

  private void clearImages(boolean pWaitToFinish)
  {
    mImageTemp.fillZero(pWaitToFinish, false);
    super.clear(false);
  }

  private void setInvariantKernelParameters(ClearCLImage pDetectionImage)
  {
    mUpscaleKernel.setGlobalOffsets(0, 0);
    mUpscaleKernel.setGlobalSizes(getWidth(), getHeight());

    mNoiseKernel.setGlobalOffsets(0, 0);
    mNoiseKernel.setGlobalSizes(getWidth(), getHeight());

  }

  private void upscale(ClearCLImage pImageInput,
                       ClearCLImage pImageOutput,
                       boolean pWaitToFinish)
  {
    mUpscaleKernel.setArgument("imagein", pImageInput);
    mUpscaleKernel.setArgument("imageout", pImageOutput);

    float lNormalizedXMin = (float) mXMin / getMaxWidth();
    float lNormalizedXScale = (float) getWidth() / getMaxWidth();
    float lNormalizedYMin = (float) mYMin / getMaxHeight();
    float lNormalizedYScale = (float) getHeight() / getMaxHeight();

    lNormalizedXMin += mShiftX;
    lNormalizedYMin += mShiftY;

    if (mMagnification != 1.0f)
    {
      lNormalizedXMin +=
                      0.5f * lNormalizedXScale * (1 - mMagnification);
      lNormalizedYMin +=
                      0.5f * lNormalizedYScale * (1 - mMagnification);

      lNormalizedXScale *= mMagnification;
      lNormalizedYScale *= mMagnification;
    } /**/

    mUpscaleKernel.setArgument("nxmin", lNormalizedXMin);
    mUpscaleKernel.setArgument("nxscale", lNormalizedXScale);
    mUpscaleKernel.setArgument("nymin", lNormalizedYMin);
    mUpscaleKernel.setArgument("nyscale", lNormalizedYScale);

    mUpscaleKernel.run(pWaitToFinish);
  }

  private void noise(ClearCLImage pImageInput,
                     ClearCLImage pImageOutput,
                     boolean pWaitToFinish)
  {
    mNoiseKernel.setArgument("imagein", pImageInput);
    mNoiseKernel.setArgument("imageout", pImageOutput);
    mNoiseKernel.setArgument("timeindex", mTimeIndex);
    mNoiseKernel.setArgument("exposure",
                             getExposure() / cNormalExposure);
    mNoiseKernel.setArgument("shotnoise", getShotNoise());
    mNoiseKernel.setArgument("offset", getOffset());
    mNoiseKernel.setArgument("gain", getGain());
    mNoiseKernel.setArgument("offsetbias", getOffsetBias());
    mNoiseKernel.setArgument("gainbias", getGainBias());
    mNoiseKernel.setArgument("offsetnoise", getOffsetNoise());
    mNoiseKernel.setArgument("gainnoise", getGainNoise());

    mNoiseKernel.run(pWaitToFinish);
  }

  @Override
  public void close()
  {
    mImageTemp.close();
    super.close();
  }

}
