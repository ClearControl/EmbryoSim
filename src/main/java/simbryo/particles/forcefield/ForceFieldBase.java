package simbryo.particles.forcefield;

/**
 * Base class implementing common fields and methods of all force fields.
 *
 * @author royer
 */
public abstract class ForceFieldBase implements ForceFieldInterface
{
  private static final long serialVersionUID = 1L;

  protected volatile float mForceIntensity;

  /**
   * Constructs a force field with a given force intensity.
   * 
   * @param pForceIntensity
   *          force intensity
   */
  public ForceFieldBase(float pForceIntensity)
  {
    mForceIntensity = pForceIntensity;
  }

  @Override
  public float getForceIntensity()
  {
    return mForceIntensity;
  }

  @Override
  public void setForceIntensity(float pForce)
  {
    mForceIntensity = pForce;
  }

}
