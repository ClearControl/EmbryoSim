package simbryo.particles.isosurf;

/**
 * This base class provides common fields and methods for all iso-surface
 * implementations.
 *
 * @author royer
 */
public abstract class IsoSurfaceBase implements IsoSurfaceInterface
{
  private static final long serialVersionUID = 1L;

  protected final int mDimension;

  protected int mIndex;
  protected float mDistance;
  protected final float[] mGradient;

  /**
   * Prepares internal data structures given the dimension.
   * 
   * @param pDimension
   *          dimension
   */
  public IsoSurfaceBase(int pDimension)
  {
    super();
    mDimension = pDimension;
    mGradient = new float[pDimension];
  }

  @Override
  public int getDimension()
  {
    return mDimension;
  }

  @Override
  public void clear()
  {
    mIndex = 0;
  }

  @Override
  public float getDistance()
  {
    return mDistance;
  }

  @Override
  public float getNormalizedGardient(int pIndex)
  {
    return mGradient[pIndex];
  }

}
