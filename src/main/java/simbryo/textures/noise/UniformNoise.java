package simbryo.textures.noise;

import java.util.SplittableRandom;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * Uniform noise
 * 
 * @author royer
 */
public class UniformNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{

  private SplittableRandom mRandom;

  private volatile float mMin = -1;
  private volatile float mMax = Math.nextUp(1);

  /**
   * Instanciates a uniform noise object.
   * 
   * @param pDimension
   *          dimension
   */
  public UniformNoise(int pDimension)
  {
    super(pDimension);
    mRandom = new SplittableRandom();
  }

  @Override
  public TextureGeneratorInterface clone()
  {
    UniformNoise lSimplexNoise = new UniformNoise(getDimension());
    return lSimplexNoise;
  }

  @Override
  public float sampleTexture(int... pCoordinate)
  {
    float lValue = (float) mRandom.nextDouble(getMin(), getMax());
    return lValue;
  }

  /**
   * Returns the minimal value of the noise
   * 
   * @return min
   */
  public float getMin()
  {
    return mMin;
  }

  /**
   * Sets the minimal value of the noise
   * 
   * @param pMin
   *          min
   */
  public void setMin(float pMin)
  {
    mMin = pMin;
  }

  /**
   * Returns the maximal value of the noise
   * 
   * @return max
   */
  public float getMax()
  {
    return mMax;
  }

  /**
   * Sets the maximal value of the noise
   * 
   * @param pMax
   *          max
   */
  public void setMax(float pMax)
  {
    mMax = pMax;
  }

}
