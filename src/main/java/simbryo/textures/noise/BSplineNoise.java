package simbryo.textures.noise;

import java.util.SplittableRandom;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * Quadratic and Cubic 2d Basis Spline noise.
 * <p/>
 * Hashing coordinates to a random number is a combination of
 * http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf and
 * https://code.google.com/archive/p/fast-hash/
 * <p/>
 * The resulting uniform distribution seems pretty good. Normalization is done
 * once at the end to avoid unnecessary multiplications.
 * <p/>
 * The BITS flag indicates how many bits to use. Interestingly you get good
 * results with only 3 bits. This could possibly unlock some low level
 * optimizations (getting all pseudo random numbers at once)?
 * <p>
 * Repo: https://bitbucket.org/jonkagstrom/b-spline-noise By @jonkagstrom 2016
 * playchilla.com
 */
public class BSplineNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{
  private static final float mCorrectionScale = 3f;
  private static final long BITS = 62;
  private static final long BIT_MASK = (1L << BITS) - 1L;
  private static final double NORMALIZER = 1. / (1L << (BITS - 1));

  private long mSeed;

  /**
   * Instanciates a B-Spline nosie object
   * 
   * @param pDimension
   *          dimension
   */
  public BSplineNoise(int pDimension)
  {
    super(pDimension);
    SplittableRandom lSplittableRandom = new SplittableRandom();
    mSeed = lSplittableRandom.nextLong();
  }

  @Override
  public TextureGeneratorInterface clone()
  {
    return new BSplineNoise(getDimension());
  }

  @Override
  public float sampleTexture(int... pCoordinate)
  {
    if (getDimension() == 1)
    {
      float lValue = (float) cubic(pCoordinate[0] * mCorrectionScale
                                   * getScale(0),
                                   0);
      return lValue;
    }
    else if (getDimension() == 2)
    {
      float lValue = (float) cubic(pCoordinate[0] * mCorrectionScale
                                   * getScale(0),
                                   pCoordinate[1] * mCorrectionScale
                                                  * getScale(1));
      return lValue;
    }
    else if (getDimension() == 3)
    {
      float lValue =
                   (float) (cubic(pCoordinate[0]
                                  * mCorrectionScale
                                  * getScale(0),
                                  pCoordinate[1] * mCorrectionScale
                                                 * getScale(1))
                            + cubic(pCoordinate[1] * mCorrectionScale
                                    * getScale(1),
                                    pCoordinate[2] * mCorrectionScale
                                                   * getScale(2))
                            + cubic(pCoordinate[2] * mCorrectionScale
                                    * getScale(0),
                                    pCoordinate[0] * mCorrectionScale
                                                   * getScale(3)));
      return lValue;
    }

    return 0;
  }

  @SuppressWarnings("unused")
  private double quadratic(final double x, final double y)
  {
    long cx0 = _floorToLong(x);
    long cy0 = _floorToLong(y);
    final double u = x - cx0;
    final double v = y - cy0;
    final double u2 = u * u;
    final double bu0 = .5 * u2;
    final double bu1 = .5 + u - u2;
    final double bu2 = .5 - u + bu0;
    final double v2 = v * v;
    final double bv0 = .5 * v2;
    final double bv1 = .5 + v - v2;
    final double bv2 = .5 - v + bv0;
    cx0 *= 73856093L;
    cy0 *= 83492791L;
    final long cx1 = cx0 + 73856093L;
    final long cx2 = cx1 + 73856093L;
    final long cy1 = cy0 + 83492791L;
    final long cy2 = cy1 + 83492791L;
    final double a = bv2 * (bu2 * _n(cx0, cy0) + bu1 * _n(cx1, cy0)
                            + bu0 * _n(cx2, cy0));
    final double b = bv1 * (bu2 * _n(cx0, cy1) + bu1 * _n(cx1, cy1)
                            + bu0 * _n(cx2, cy1));
    final double c = bv0 * (bu2 * _n(cx0, cy2) + bu1 * _n(cx1, cy2)
                            + bu0 * _n(cx2, cy2));
    return NORMALIZER * (a + b + c) - 1.;
  }

  private double cubic(final double x, final double y)
  {
    long cx0 = _floorToLong(x);
    long cy0 = _floorToLong(y);
    final double u = x - cx0;
    final double v = y - cy0;
    final double u2 = u * u;
    final double v2 = v * v;
    final double u3 = u2 * u;
    final double v3 = v2 * v;
    final double bu0 = 1. / 6. * u3;
    final double bv0 = 1. / 6. * v3;
    final double bu1 = 1. / 6. + .5 * (u + u2 - u3);
    final double bv1 = 1. / 6. + .5 * (v + v2 - v3);
    final double bu2 = 2. / 3. + 3. * bu0 - u2;
    final double bv2 = 2. / 3. + 3. * bv0 - v2;
    final double bu3 = 1. / 6. + .5 * (u2 - u) - bu0;
    final double bv3 = 1. / 6. + .5 * (v2 - v) - bv0;
    cx0 *= 73856093L;
    cy0 *= 83492791L;
    final long cx1 = cx0 + 73856093L;
    final long cx2 = cx1 + 73856093L;
    final long cx3 = cx2 + 73856093L;
    final long cy1 = cy0 + 83492791L;
    final long cy2 = cy1 + 83492791L;
    final long cy3 = cy2 + 83492791L;
    final double a =
                   bv3 * (bu3 * _n(cx0, cy0) + bu2 * _n(cx1, cy0)
                          + bu1 * _n(cx2, cy0)
                          + bu0 * _n(cx3, cy0));
    final double b =
                   bv2 * (bu3 * _n(cx0, cy1) + bu2 * _n(cx1, cy1)
                          + bu1 * _n(cx2, cy1)
                          + bu0 * _n(cx3, cy1));
    final double c =
                   bv1 * (bu3 * _n(cx0, cy2) + bu2 * _n(cx1, cy2)
                          + bu1 * _n(cx2, cy2)
                          + bu0 * _n(cx3, cy2));
    final double d =
                   bv0 * (bu3 * _n(cx0, cy3) + bu2 * _n(cx1, cy3)
                          + bu1 * _n(cx2, cy3)
                          + bu0 * _n(cx3, cy3));
    return NORMALIZER * (a + b + c + d) - 1.;
  }

  private double _n(final long x, final long y)
  {
    long c = mSeed ^ x ^ y;
    c ^= (c >>> 23);
    c *= 0x2127599bf4325c37L;
    c ^= (c >>> 47);
    return c & BIT_MASK;
  }

  private static long _floorToLong(final double x)
  {
    final long xi = (long) x;
    return x < xi ? xi - 1 : xi;
  }

}
