package simbryo.synthoscopy.optics.illumination.impl.lightsheet;

import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import clearcl.util.MatrixUtils;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.optics.illumination.IlluminationOpticsBase;
import simbryo.synthoscopy.optics.illumination.IlluminationOpticsInterface;
import simbryo.util.geom.GeometryUtils;

/**
 * Lightsheet illumination.
 *
 * @author royer
 */
public class LightSheetIllumination extends IlluminationOpticsBase
                                    implements
                                    IlluminationOpticsInterface<ClearCLImage>,
                                    AutoCloseable
{

  protected ClearCLImage mBallisticLightImageA, mBallisticLightImageB,
      mScatteredLightImageA, mScatteredLightImageB;
  protected ClearCLKernel mPropagateLightSheetKernel;
  private ClearCLBuffer mCombinedTransformMatrixBuffer;

  protected Vector4f mLightSheetPosition, mLightSheetAxisVector,
      mLightSheetNormalVector, mLightSheetEffectivePosition,
      mLightSheetEffectiveAxisVector,
      mLightSheetEffectiveNormalVector;

  private volatile float mLightSheetAlphaInRad, mLightSheetBetaInRad,
      mLightSheetGammaInRad, mLightSheetThetaInRad, mLightSheetHeigth,
      mScatterConstant, mScatterLoss, mSigmaMin, mSigmaMax;

  private Matrix4f mDetectionTransformMatrix;

  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, the wavelength of light, the light intensity, and light map image
   * dimensions
   * 
   * @param pContext
   *          OpenCL context
   * @param pLightMapDimensions
   *          light map dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public LightSheetIllumination(ClearCLContext pContext,
                                long... pLightMapDimensions) throws IOException
  {
    super(pContext, pLightMapDimensions);

    setLightSheetThetaInDeg(IlluminationParameter.Theta.getDefaultValue()
                                                       .floatValue());
    setLightSheetHeigth(IlluminationParameter.Height.getDefaultValue()
                                                    .floatValue());
    setScatterConstant(100.0f);
    setScatterLoss(0.02f);
    setSigmaMin(0.5f);
    setSigmaMax(1.0f);

    setupProgramAndKernels();

    mScatteredLightImageA =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mScatteredLightImageB =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mBallisticLightImageA =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mBallisticLightImageB =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mLightSheetPosition = new Vector4f(0f, 0f, 0.5f, 1.0f);
    mLightSheetAxisVector = new Vector4f(1.0f, 0, 0, 1.0f);
    mLightSheetNormalVector = new Vector4f(0, 0, 1.0f, 1.0f);
    mLightSheetEffectivePosition =
                                 new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
    mLightSheetEffectiveAxisVector = new Vector4f(1.0f, 0, 0, 1.0f);
    mLightSheetEffectiveNormalVector = new Vector4f(0, 0, 1.0f, 1.0f);

    mDetectionTransformMatrix = new Matrix4f();
    mDetectionTransformMatrix.setIdentity();
  }

  /**
   * Returns scattering constant. The bigger the constant the more light gets
   * transfered from ballistic to scattering.
   * 
   * @return scattering constant
   */
  public float getScatterConstant()
  {
    return mScatterConstant;
  }

  /**
   * Setseturns scattering constant. The bigger the constant the more light gets
   * transfered from ballistic to scattering.
   * 
   * @param pScatterConstant
   *          new scattering constant
   */
  public void setScatterConstant(float pScatterConstant)
  {
    if (mScatterConstant != pScatterConstant)
    {
      mScatterConstant = pScatterConstant;
      requestUpdate();
    }
  }

  /**
   * Returns the scattering loss. This is the proportion of scattered light
   * (value within [0,1]) that gets lost per voxel.
   * 
   * @return scattering loss
   */
  public float getScatterLoss()
  {
    return mScatterLoss;
  }

  /**
   * Sets the scattering loss. This is the proportion of scattered light (value
   * within [0,1]) that gets lost per voxel.
   * 
   * @param pScatterLoss
   *          scattering loss
   */
  public void setScatterLoss(float pScatterLoss)
  {
    if (mScatterLoss != pScatterLoss)
    {
      mScatterLoss = pScatterLoss;
      requestUpdate();
    }
  }

  /**
   * Returns the minimal sigma value. The min sigma value represents the
   * dispersion of already scattered light as it propagates _even_ in the
   * absence of scattering material.
   * 
   * @return current min sigma value
   */
  public float getSigmaMin()
  {
    return mSigmaMin;
  }

  /**
   * Sets the minimal sigma value. The min sigma value represents the dispersion
   * of already scattered light as it propagates _even_ in the absence of
   * scattering material.
   * 
   * @param pSigmaMin
   *          new min sigma value
   */
  public void setSigmaMin(float pSigmaMin)
  {
    if (mSigmaMin != pSigmaMin)
    {
      mSigmaMin = pSigmaMin;
      requestUpdate();
    }
  }

  /**
   * Returns the maximal sigma value. The actual sigma value per voxel will be
   * modulated by (maxsigma-minsigm) basd on the actual scattering phantom value
   * (which are values within [0,1], 0-> min scattering, 1 -> max scattering)
   * 
   * @return current max sigma value
   */
  public float getSigmaMax()
  {
    return mSigmaMax;
  }

  /**
   * Sets the maximal sigma value. The actual sigma value per vocel will be
   * modulated by (maxsigma-minsigm) basd on the actual scattering phantom value
   * (which are values within [0,1], 0-> min scattering, 1 -> max scattering)
   * 
   * @param pSigmaMax
   *          new max sigma value
   */
  public void setSigmaMax(float pSigmaMax)
  {
    if (mSigmaMax != pSigmaMax)
    {
      mSigmaMax = pSigmaMax;
      requestUpdate();
    }
  }

  /**
   * Returns lightsheet height in normalized units.
   * 
   * @return light sheet height in normalized units.
   */
  public float getLightSheetHeigth()
  {
    return mLightSheetHeigth;
  }

  /**
   * Sets the lightsheet height in normalized units.
   * 
   * @param pLightSheetHeigth
   *          light sheet height in normalized units.
   */
  public void setLightSheetHeigth(float pLightSheetHeigth)
  {
    if (mLightSheetHeigth != pLightSheetHeigth)
    {
      mLightSheetHeigth = pLightSheetHeigth;
      requestUpdate();
    }
  }

  /**
   * Returns the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @return divergence in degrees
   */
  public float getLightSheetThetaInDeg()
  {
    return (float) Math.toDegrees(mLightSheetThetaInRad);
  }

  /**
   * Returns the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @return divergence in radians
   */
  public float getLightSheetThetaInRad()
  {
    return mLightSheetThetaInRad;
  }

  /**
   * Sets the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @param pLightSheetThetaInDeg
   *          divergence in degrees
   */
  public void setLightSheetThetaInDeg(float pLightSheetThetaInDeg)
  {
    float lNewLightSheetThetaInDeg =
                                   (float) Math.toRadians(pLightSheetThetaInDeg);
    if (mLightSheetThetaInRad != lNewLightSheetThetaInDeg)
    {
      mLightSheetThetaInRad = lNewLightSheetThetaInDeg;
      requestUpdate();
    }
  }

  private float getSpotSizeAtNeck()
  {
    return (float) (getLightWavelength()
                    / (Math.PI * getLightSheetThetaInRad()));
  }

  /**
   * Sets lightsheet position in normalized units. N
   * 
   * @param pX
   *          x coordinate within [0,1]
   * @param pY
   *          y coordinate within [0,1]
   * @param pZ
   *          z coordinate within [0,1]
   */
  public void setLightSheetPosition(float pX, float pY, float pZ)
  {
    if (mLightSheetPosition.x != pX || mLightSheetPosition.y != pY
        || mLightSheetPosition.z != pZ)
    {
      mLightSheetPosition.x = pX;
      mLightSheetPosition.y = pY;
      mLightSheetPosition.z = pZ;
      requestUpdate();
    }
  }

  /**
   * Returns lightsheet center position vector.
   * 
   * @return axis vector
   */
  public Vector4f getLightSheetPositionVector()
  {
    return mLightSheetPosition;
  }

  /**
   * Sets lightsheet axis vector (light propagation direction). Inputs are
   * automatically normalized.
   * 
   * @param pAxisVector
   *          axis vector
   * 
   */
  public void setLightSheetAxisVector(Vector3f pAxisVector)
  {
    setLightSheetAxisVector(pAxisVector.x,
                            pAxisVector.y,
                            pAxisVector.z);
  }

  /**
   * Sets lightsheet axis vector (light propagation direction). Inputs are
   * automatically normalized.
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setLightSheetAxisVector(float pX, float pY, float pZ)
  {
    if (mLightSheetAxisVector.x != pX || mLightSheetAxisVector.y != pY
        || mLightSheetAxisVector.z != pZ)
    {

      mLightSheetAxisVector.x = pX;
      mLightSheetAxisVector.y = pY;
      mLightSheetAxisVector.z = pZ;

      GeometryUtils.homogenousNormalize(mLightSheetAxisVector);
      float lProjection =
                        GeometryUtils.homogenousDot(mLightSheetAxisVector,
                                                    mLightSheetNormalVector);

      mLightSheetNormalVector.scaleAdd(-lProjection,
                                       mLightSheetAxisVector,
                                       mLightSheetNormalVector);
      GeometryUtils.homogenousNormalize(mLightSheetNormalVector);
      requestUpdate();
    }
  }

  /**
   * Returns lightsheet axis vector (light propagation direction).
   * 
   * @return axis vector
   */
  public Vector4f getLightSheetAxisVector()
  {
    return mLightSheetAxisVector;
  }

  /**
   * Sets lightsheet normal vector (perpendicular to the lightsheet plane).
   * Inputs are automatically normalized.
   * 
   * @param pNormalVector
   *          normal vector
   * 
   */
  public void setLightSheetNormalVector(Vector3f pNormalVector)
  {
    setLightSheetNormalVector(pNormalVector.x,
                              pNormalVector.y,
                              pNormalVector.z);
  }

  /**
   * Sets lightsheet normal vector (perpendicular to the lightsheet plane).
   * Inputs are automatically normalized.
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setLightSheetNormalVector(float pX, float pY, float pZ)
  {
    if (mLightSheetNormalVector.x != pX
        || mLightSheetNormalVector.y != pY
        || mLightSheetNormalVector.z != pZ)
    {
      mLightSheetNormalVector.x = pX;
      mLightSheetNormalVector.y = pY;
      mLightSheetNormalVector.z = pZ;

      GeometryUtils.homogenousNormalize(mLightSheetNormalVector);

      float lProjection =
                        GeometryUtils.homogenousDot(mLightSheetNormalVector,
                                                    mLightSheetAxisVector);

      mLightSheetAxisVector.scaleAdd(-lProjection,
                                     mLightSheetNormalVector,
                                     mLightSheetAxisVector);

      GeometryUtils.homogenousNormalize(mLightSheetAxisVector);
      requestUpdate();
    }
  }

  /**
   * Returns lightsheet normal vector.
   * 
   * @return normal vector
   */
  public Vector4f getLightSheetNormalVector()
  {
    return mLightSheetNormalVector;
  }

  /**
   * Sets the axis and normal lightsheet vectors from the three angles (alpha,
   * beta, gamma) in degrees. the alpha angle rotates along x, the beta angle
   * along y, and the gamma angle around z.
   * 
   * @param pAlpha
   *          alpha in degrees
   * @param pBeta
   *          beta in degrees
   * @param pGamma
   *          gamma in degrees
   */
  public void setOrientationWithAnglesInDegrees(float pAlpha,
                                                float pBeta,
                                                float pGamma)
  {
    setOrientationWithAnglesInRadians((float) toRadians(pAlpha),
                                      (float) toRadians(pBeta),
                                      (float) toRadians(pGamma));
  }

  /**
   * Rotates the axis and normal lightsheet vectors along the three directions
   * (x,y,z) by the angles (alpha, beta, gamma) in radians. the alpha angle
   * rotates along x, the beta angle along y, and the gamma angle around z.
   * 
   * @param pAlpha
   *          alpha in radians
   * @param pBeta
   *          beta in radians
   * @param pGamma
   *          gamma in radians
   */
  public void setOrientationWithAnglesInRadians(float pAlpha,
                                                float pBeta,
                                                float pGamma)
  {

    if (mLightSheetAlphaInRad != pAlpha
        || mLightSheetBetaInRad != pBeta
        || mLightSheetGammaInRad != pGamma)
    {
      mLightSheetAlphaInRad = pAlpha;
      mLightSheetBetaInRad = pBeta;
      mLightSheetGammaInRad = pGamma;

      requestUpdate();
    }

  }

  /**
   * Sets the alpha angle in radians
   * 
   * @param pAlphaInRadians
   *          alpha in radians
   */
  public void setAlphaInRadians(float pAlphaInRadians)
  {
    if (mLightSheetAlphaInRad != pAlphaInRadians)
    {
      mLightSheetAlphaInRad = pAlphaInRadians;
      requestUpdate();
    }
  }

  /**
   * Sets the beta angle in radians
   * 
   * @param pBetaInRadians
   *          beta in radians
   */
  public void setBetaInRadians(float pBetaInRadians)
  {
    if (mLightSheetBetaInRad != pBetaInRadians)
    {
      mLightSheetBetaInRad = pBetaInRadians;
      requestUpdate();
    }
  }

  /**
   * Sets the alpha angle in radians
   * 
   * @param pGammaInRadians
   *          gamma in radians
   */
  public void setGammaInRadians(float pGammaInRadians)
  {
    if (mLightSheetGammaInRad != pGammaInRadians)
    {
      mLightSheetGammaInRad = pGammaInRadians;
      requestUpdate();
    }
  }

  @Override
  public void setPhantomTransformMatrix(Matrix4f pTransformMatrix)
  {
    super.setPhantomTransformMatrix(pTransformMatrix);
  }

  /**
   * Sets teh detection transform matrix.
   * 
   * @param pDetectionTransformMatrix
   *          detection transform matrix
   */
  public void setDetectionTransformMatrix(Matrix4f pDetectionTransformMatrix)
  {
    if (mDetectionTransformMatrix == null
        || !mDetectionTransformMatrix.equals(pDetectionTransformMatrix))
    {
      mDetectionTransformMatrix = pDetectionTransformMatrix;
      requestUpdate();
    }
  }

  /**
   * Returns the detection transform matrix
   * 
   * @return detection transform matrix
   */
  public Matrix4f getDetectionTransformMatrix()
  {
    return new Matrix4f(mDetectionTransformMatrix);
  }

  /**
   * Returns the inverse detection transform matrix
   * 
   * @return inverse detection transform matrix
   */
  public Matrix4f getInverseDetectionTransformMatrix()
  {
    Matrix4f lInverseDetectionTransformMatrix =
                                              getDetectionTransformMatrix();
    lInverseDetectionTransformMatrix.invert();
    return lInverseDetectionTransformMatrix;
  }

  /**
   * Returns the product of the phantom and detection transform matrices
   * 
   * @return product of the phantom and detection transform matrices
   */
  public Matrix4f getPhantomAndDetectionTransformMatrix()
  {
    Matrix4f lPhantomAndDetectionTransformMatrix =
                                                 GeometryUtils.multiply(getPhantomTransformMatrix(),
                                                                        getDetectionTransformMatrix());
    return lPhantomAndDetectionTransformMatrix;
  }

  protected ClearCLBuffer getCombinedTransformMatrixBuffer()
  {
    mCombinedTransformMatrixBuffer =
                                   MatrixUtils.matrixToBuffer(mContext,
                                                              mCombinedTransformMatrixBuffer,
                                                              getPhantomAndDetectionTransformMatrix());
    return mCombinedTransformMatrixBuffer;
  }

  /**
   * Returns the light sheet's effective position after all transformations
   * applied.
   * 
   * @return effective lightsheet position
   */
  public Vector4f getLightSheetEffectivePosition()
  {
    return mLightSheetEffectivePosition;
  }

  /**
   * Returns the light sheet's effective axis vector after rotation by (alpha,
   * beta, gamma)
   * 
   * @return effective axis vector
   */
  public Vector4f getLightSheetEffectiveAxisVector()
  {
    return mLightSheetEffectiveAxisVector;
  }

  /**
   * Returns the light sheet's effective normal vector after rotation by (alpha,
   * beta, gamma)
   * 
   * @return normal axis vector
   */
  public Vector4f getLightSheetEffectiveNormalVector()
  {
    return mLightSheetEffectiveNormalVector;
  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(LightSheetIllumination.class,
                       "kernel/LightSheetIllumination.cl");

    lProgram.addBuildOptionAllMathOpt();
    lProgram.buildAndLog();

    mPropagateLightSheetKernel = lProgram.createKernel("propagate");
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (!isUpdateNeeded())
      return;

    initializeLightSheet(mBallisticLightImageA,
                         mBallisticLightImageB,
                         mScatteredLightImageA,
                         mScatteredLightImageB,
                         false);

    setInvariantKernelParameters(mScatteringPhantomImage);

    for (int i = 0; i < getWidth(); i++)
    {
      int x;

      if (mLightSheetEffectiveAxisVector.x > 0)
        x = i;
      else
        x = (int) (getWidth() - 1 - i);

      propagate(x,
                mBallisticLightImageA,
                mScatteredLightImageA,
                mBallisticLightImageB,
                mScatteredLightImageB,
                (i == getWidth() - 1) && pWaitToFinish);

      /*diffuse(mScatteredLightImageB,
              mScatteredLightImageA,
              mScatteredLightImageB,
              5);/**/
      swapLightImages();
    }

    super.render(pWaitToFinish);
  }

  private void updateEffectiveVectors()
  {
    Matrix4f lInverseDetectionTransformMatrix =
                                              getInverseDetectionTransformMatrix();

    GeometryUtils.homogenousNormalize(getLightSheetAxisVector());
    GeometryUtils.homogenousNormalize(getLightSheetNormalVector());

    Matrix4f lRotX = new Matrix4f();
    Matrix4f lRotY = new Matrix4f();
    Matrix4f lRotZ = new Matrix4f();

    lRotX.rotX(mLightSheetAlphaInRad);
    lRotY.rotY(mLightSheetBetaInRad);
    lRotZ.rotZ(mLightSheetGammaInRad);

    Matrix4f lVectorTransformationMatrix =
                                         GeometryUtils.multiply(lInverseDetectionTransformMatrix,
                                                                lRotZ,
                                                                lRotX,
                                                                lRotY);

    mLightSheetEffectiveAxisVector =
                                   GeometryUtils.directionMultiplication(lVectorTransformationMatrix,
                                                                         mLightSheetAxisVector);

    mLightSheetEffectiveNormalVector =
                                     GeometryUtils.directionMultiplication(lVectorTransformationMatrix,
                                                                           mLightSheetNormalVector);

    // the following two lines make sure that the lighsheet alpha pivot also
    // affects the z position:
    mLightSheetEffectivePosition.set(mLightSheetPosition);
    mLightSheetEffectivePosition.z = (float) (mLightSheetPosition.z
                                              + sin(mLightSheetAlphaInRad)
                                                * (mLightSheetPosition.y
                                                   - 0.5f));

    mLightSheetEffectivePosition =
                                 GeometryUtils.pointMultiplication(lInverseDetectionTransformMatrix,
                                                                   mLightSheetEffectivePosition);

  }

  private void setInvariantKernelParameters(ClearCLImage pScatteringPhantomImage)
  {
    updateEffectiveVectors();

    mPropagateLightSheetKernel.setGlobalOffsets(0, 0);
    mPropagateLightSheetKernel.setGlobalSizes(getHeight(),
                                              getDepth());

    mPropagateLightSheetKernel.setArgument("scatterphantom",
                                           pScatteringPhantomImage);
    mPropagateLightSheetKernel.setArgument("lightmapout", getImage());

    mPropagateLightSheetKernel.setArgument("lspx",
                                           mLightSheetEffectivePosition.x);
    mPropagateLightSheetKernel.setArgument("lspy",
                                           mLightSheetEffectivePosition.y);
    mPropagateLightSheetKernel.setArgument("lspz",
                                           mLightSheetEffectivePosition.z);

    mPropagateLightSheetKernel.setArgument("lsax",
                                           mLightSheetEffectiveAxisVector.x);
    mPropagateLightSheetKernel.setArgument("lsay",
                                           mLightSheetEffectiveAxisVector.y);
    mPropagateLightSheetKernel.setArgument("lsaz",
                                           mLightSheetEffectiveAxisVector.z);

    mPropagateLightSheetKernel.setArgument("lsnx",
                                           mLightSheetEffectiveNormalVector.x);
    mPropagateLightSheetKernel.setArgument("lsny",
                                           mLightSheetEffectiveNormalVector.y);
    mPropagateLightSheetKernel.setArgument("lsnz",
                                           mLightSheetEffectiveNormalVector.z);

    mPropagateLightSheetKernel.setArgument("lambda",
                                           getLightWavelength());

    float lEffectiveIntensity = getIntensity()
                                * (1.0f / max(1.0f / getHeight(),
                                              getLightSheetHeigth()));

    mPropagateLightSheetKernel.setArgument("intensity",
                                           lEffectiveIntensity);

    mPropagateLightSheetKernel.setArgument("scatterconstant",
                                           getScatterConstant());

    mPropagateLightSheetKernel.setArgument("scatterloss",
                                           1.0f - getScatterLoss());

    mPropagateLightSheetKernel.setArgument("sigmamin", getSigmaMin());

    mPropagateLightSheetKernel.setArgument("sigmamax", getSigmaMax());

    mPropagateLightSheetKernel.setArgument("w0", getSpotSizeAtNeck());

    mPropagateLightSheetKernel.setArgument("lsheight",
                                           getLightSheetHeigth());

    mPropagateLightSheetKernel.setOptionalArgument("matrix",
                                                   getCombinedTransformMatrixBuffer());
  }

  private void propagate(int pXPosition,
                         ClearCLImage pBallisticLightImageA,
                         ClearCLImage pScatteredLightImageA,
                         ClearCLImage pBallisticLightImageB,
                         ClearCLImage pScatteredLightImageB,
                         boolean pWaitToFinish)
  {

    mPropagateLightSheetKernel.setArgument("binput",
                                           pBallisticLightImageA);
    mPropagateLightSheetKernel.setArgument("boutput",
                                           pBallisticLightImageB);
    mPropagateLightSheetKernel.setArgument("sinput",
                                           pScatteredLightImageA);
    mPropagateLightSheetKernel.setArgument("soutput",
                                           pScatteredLightImageB);

    mPropagateLightSheetKernel.setArgument("x", pXPosition);

    mPropagateLightSheetKernel.run(pWaitToFinish);

    // pBallisticLightImageB.notifyListenersOfChange(mContext.getDefaultQueue());
    // pScatteredLightImageB.notifyListenersOfChange(mContext.getDefaultQueue());

  }

  private void initializeLightSheet(ClearCLImage pBallisticLightImageA,
                                    ClearCLImage pBallisticLightImageB,
                                    ClearCLImage pScatteredLightImageA,
                                    ClearCLImage pScatteredLightImageB,
                                    boolean pBlockingCall)
  {

    pBallisticLightImageA.fill(1.0f, false, false);
    pBallisticLightImageB.fill(1.0f, false, false);
    pScatteredLightImageA.fill(0.0f, false, false);
    pScatteredLightImageB.fill(0.0f, pBlockingCall, false);
  }

  private void swapLightImages()
  {
    ClearCLImage lTemp;

    lTemp = mScatteredLightImageA;
    mScatteredLightImageA = mScatteredLightImageB;
    mScatteredLightImageB = lTemp;

    lTemp = mBallisticLightImageA;
    mBallisticLightImageA = mBallisticLightImageB;
    mBallisticLightImageB = lTemp;
  }

  @Override
  public void close()
  {
    mBallisticLightImageA.close();
    mBallisticLightImageB.close();
    mScatteredLightImageA.close();
    mScatteredLightImageB.close();

    mPropagateLightSheetKernel.close();

    super.close();
  }

}
