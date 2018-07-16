package simbryo.particles.isosurf.impl;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import simbryo.particles.isosurf.IsoSurfaceBase;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * A plane surface. The plane is defined as passing through a given point and
 * perpendicular to a given normal vector.
 *
 * @author royer
 */
public class Plane extends IsoSurfaceBase
                   implements IsoSurfaceInterface
{
  private static final long serialVersionUID = 1L;

  private float[] mNormal;
  private float[] mPoint;

  private float[] mCoordinates;

  /**
   * Instantiates a plane.
   * 
   * @param pNormal
   *          normal vector
   */
  public Plane(float... pNormal)
  {
    super(pNormal.length);
    mNormal = pNormal;
    mPoint = new float[getDimension()];
    for (int i = 0; i < getDimension(); i++)
      mPoint[i] = 0.5f;
    mCoordinates = new float[getDimension()];
  }

  /**
   * Sets a point through which the planes passes
   * 
   * @param pPoint
   *          point coordinates
   */
  public void setPoint(float... pPoint)
  {
    mPoint = pPoint;
  }

  @Override
  public void addCoordinate(float pValue)
  {
    mCoordinates[mIndex++] = pValue;

    if (mIndex == mCoordinates.length)
    {
      float num = 0;
      float denom = 0;
      float dsum = 0;

      for (int i = 0; i < getDimension(); i++)
      {
        num += mNormal[i] * mCoordinates[i];
        denom += pow(mNormal[i], 2);
        dsum += -mNormal[i] * mPoint[i];

        mGradient[i] = mNormal[i];
      }

      float lGradientlength = 0;

      for (int i = 0; i < mDimension; i++)
        lGradientlength += mGradient[i] * mGradient[i];
      lGradientlength = (float) sqrt(lGradientlength);

      for (int i = 0; i < mDimension; i++)
        mGradient[i] = mGradient[i] / lGradientlength;

      mDistance = (float) ((num + dsum) / sqrt(denom));

    }
  }

}
