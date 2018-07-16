package simbryo.synthoscopy.microscope.aberration;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;

import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 * Illumination misalignment
 *
 * @author royer
 */
public class IlluminationMisalignment extends AberrationBase
                                      implements AberrationInterface
{
  private volatile float mOffsetConstant = 1f;

  private HashMap<MutablePair<ParameterInterface<Number>, Integer>, Number> mOffsetMap =
                                                                                       new HashMap<>();

  private float mAlpha, mBeta, mGamma, mHeight, mIntensity, mTheta,
      mX, mY, mZ;

  /**
   * Instantiates an illumination misalignment for a given offset in X, Y and Z
   * 
   * @param pX
   *          X misalignment
   * @param pY
   *          Y misalignment
   * @param pZ
   *          Z misalignment
   * @return illumination misalignment
   */
  public static IlluminationMisalignment buildXYZ(float pX,
                                                  float pY,
                                                  float pZ)
  {
    IlluminationMisalignment lIlluminationMisalignment =
                                                       new IlluminationMisalignment();

    lIlluminationMisalignment.setX(pX);
    lIlluminationMisalignment.setY(pY);
    lIlluminationMisalignment.setZ(pZ);

    return lIlluminationMisalignment;
  }

  /**
   * Instantiates an illumination misalignment
   */
  public IlluminationMisalignment()
  {
    super();
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {

  }

  @Override
  public Number transform(ParameterInterface<Number> pParameter,
                          int pIndex,
                          Number pNumber)
  {
    if (!(pParameter instanceof IlluminationParameter))
      return pNumber;

    Number lNumber = pNumber;
    MutablePair<ParameterInterface<Number>, Integer> lKey =
                                                          MutablePair.of(pParameter,
                                                                         pIndex);

    Number lOffset = mOffsetMap.get(lKey);

    if (lOffset == null)
    {
      float lOffsetConstant = adjustOffsetAmount(pParameter);

      lOffset = mOffsetConstant * lOffsetConstant; // * rand(-1, 1);

      mOffsetMap.put(lKey, lOffset);
    }

    lNumber = lNumber.floatValue() + lOffset.floatValue();

    // System.out.format("offset: %s[%d] -> %g
    // \n",pParameter,pIndex,lOffset.floatValue());

    return lNumber;
  }

  protected float adjustOffsetAmount(ParameterInterface<Number> pParameter)
  {
    float lOffsetConstant = 0;
    switch ((IlluminationParameter) pParameter)
    {
    case Alpha:
      lOffsetConstant = getAlpha();
      break;
    case Beta:
      lOffsetConstant = getBeta();
      break;
    case Gamma:
      lOffsetConstant = getGamma();
      break;
    case Height:
      lOffsetConstant = mHeight;
      break;
    case Intensity:
      lOffsetConstant = getIntensity();
      break;
    case Theta:
      lOffsetConstant = getTheta();
      break;
    case Wavelength:
      lOffsetConstant = 0;
      break;
    case X:
      lOffsetConstant = getX();
      break;
    case Y:
      lOffsetConstant = getY();
      break;
    case Z:
      lOffsetConstant = getZ();
      break;
    default:
      break;
    }
    return lOffsetConstant;
  }

  public float getAlpha()
  {
    return mAlpha;
  }

  public void setAlpha(float pAlpha)
  {
    mAlpha = pAlpha;
  }

  public float getBeta()
  {
    return mBeta;
  }

  public void setBeta(float pBeta)
  {
    mBeta = pBeta;
  }

  public float getGamma()
  {
    return mGamma;
  }

  public void setGamma(float pGamma)
  {
    mGamma = pGamma;
  }

  public float getIntensity()
  {
    return mIntensity;
  }

  public void setIntensity(float pIntensity)
  {
    mIntensity = pIntensity;
  }

  public float getTheta()
  {
    return mTheta;
  }

  public void setTheta(float pTheta)
  {
    mTheta = pTheta;
  }

  public float getX()
  {
    return mX;
  }

  public void setX(float pX)
  {
    mX = pX;
  }

  public float getY()
  {
    return mY;
  }

  public void setY(float pY)
  {
    mY = pY;
  }

  public float getZ()
  {
    return mZ;
  }

  public void setZ(float pZ)
  {
    mZ = pZ;
  }

}
