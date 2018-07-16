package simbryo.particles.isosurf.impl;

import simbryo.particles.isosurf.IsoSurfaceBase;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * An nD axis aligned cylinder.
 * 
 * Example for a 3D cylinder:
 * 
 * <pre>
 *  {@code example:}
 *   new Cylinder(R,    xc,   yc, a, b)
 *   new Cylinder(1.0f, 0.5f, 0.5f, 1, 2)
 * </pre>
 * 
 * Sets the center at (xc,yc) = (0.5, 0.5) and axis scaling at (a,b) = (1,2)
 * 
 * The equation is: ((x-xc)/a)^2+((y-yc)/b)^2 - R^2 =0
 *
 * 
 * @author royer
 */

public class Cylinder extends IsoSurfaceBase
                      implements IsoSurfaceInterface
{
  private static final long serialVersionUID = 1L;

  protected float mRadius;
  protected float[] mCenterAndAxis;

  protected float mAccumulatorDistance;
  protected float mAccumulatorGradientLength;

  /**
   * Instantiates an ellipsoid with a given radius, center and axis parameters
   * 
   * @param pRadius
   *          radius
   * @param pCenterAndAxis
   *          center and axis {cx, cy, cz, ..., a, b, c, ,...}
   */
  public Cylinder(float pRadius, float... pCenterAndAxis)
  {
    super(pCenterAndAxis.length / 2 + 1);
    mRadius = pRadius;
    mCenterAndAxis = pCenterAndAxis;
  }

  @Override
  public void clear()
  {
    super.clear();
    mAccumulatorGradientLength = 0;
    mAccumulatorDistance = 0;
  }

  @Override
  public void addCoordinate(float pValue)
  {
    if (mIndex < mDimension - 1)
    {
      float c = mCenterAndAxis[mIndex];
      float a = mCenterAndAxis[mDimension - 1 + mIndex];
      float x = (c - pValue) / a;
      float dx = x / a;

      mGradient[mIndex] = dx;

      mAccumulatorGradientLength += dx * dx;
      mAccumulatorDistance += x * x;

      if (mIndex == mDimension - 1)
      {
        mDistance =
                  (float) (Math.sqrt(mAccumulatorDistance) - mRadius);

        float lGradientlength =
                              (float) Math.sqrt(mAccumulatorGradientLength);

        for (int i = 0; i < mDimension; i++)
          mGradient[i] = mGradient[i] / lGradientlength;
      }
    }
    else
    {
      mGradient[mIndex] = 0;
    }

    mIndex++;
  }

}
