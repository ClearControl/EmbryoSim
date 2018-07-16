package simbryo.particles.isosurf.impl;

import static java.lang.Math.pow;

import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * A rice grain :-) Works only in 3D.
 * 
 * 
 * 
 *
 * 
 * @author royer
 */
public class RiceGrain extends Ellipsoid
                       implements IsoSurfaceInterface
{
  private static final long serialVersionUID = 1L;

  private float x, y, z;
  private float a, b, c;
  private float s = 2;

  /**
   * Instanciates a 'Rice-Grain' with a given radius, center and axis
   * parameters. See Ellipsoid for more details.
   * 
   * @param pRadius
   *          radius
   * @param pCenterAndAxis
   *          center and axis parameters
   */
  public RiceGrain(float pRadius, float... pCenterAndAxis)
  {
    super(pRadius, pCenterAndAxis);

    if (pCenterAndAxis.length / 2 != 3)
      throw new IllegalArgumentException("Dimension must be 3");

  }

  /**
   * Derivation of the gradient:
   * https://www.wolframalpha.com/input/?i=partial+derivatives+of+(x%5E2)*(1%2F(a%5E2)%2B(1%2F(1%2Bx%5E2)))%2B(y%5E2)%2F(b%5E2)%2B(z%5E2)%2F(c%5E2)
   * 
   * 
   */
  @Override
  public void addCoordinate(float pValue)
  {
    float lCenteredValue = (mCenterAndAxis[mIndex] - pValue);

    if (mIndex == 0)
    {
      x = lCenteredValue;
      a = mCenterAndAxis[mDimension + mIndex];
    }
    else if (mIndex == 1)
    {
      y = lCenteredValue;
      b = mCenterAndAxis[mDimension + mIndex];
    }
    else if (mIndex == 2)
    {
      z = lCenteredValue;
      c = mCenterAndAxis[mDimension + mIndex];
    }

    if (mIndex == mDimension - 1)
    {
      float lFunction =
                      (float) (pow(x, 2) / pow(a, 2)
                               + pow(y, 2) * (1 / pow(b, 2)
                                              + s / (pow(x, 2) + 1))
                               + pow(z, 2) * (1 / pow(c, 2)
                                              + s / (pow(x, 2) + 1)));

      mDistance = (float) (Math.sqrt(lFunction) - mRadius);

      float dx = (float) ((2 * x
                           * (-pow(a, 2) * s * (pow(y, 2) + pow(z, 2))
                              + pow(x, 4) + 2 * pow(x, 2) + 1))
                          / pow(pow(a, 2) * (pow(x, 2) + 1), 2));
      float dy = (float) (2 * y
                          * (1 / pow(b, 2) + s / (pow(x, 2) + 1)));
      float dz = (float) (2 * z
                          * (1 / pow(c, 2) + s / (pow(x, 2) + 1)));

      float lGradientSquaredLength = dx * dx + dy * dy + dz * dz;

      float lGradientlength =
                            (float) Math.sqrt(lGradientSquaredLength);

      mGradient[0] = dx / lGradientlength;
      mGradient[1] = dy / lGradientlength;
      mGradient[2] = dz / lGradientlength;
    }

    mIndex++;
  }

}
