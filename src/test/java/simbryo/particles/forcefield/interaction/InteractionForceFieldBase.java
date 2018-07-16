package simbryo.particles.forcefield.interaction;

import simbryo.particles.forcefield.ForceFieldBase;

/**
 * Base class implementing common fields and methods of all interaction force
 * fields.
 *
 * @author royer
 */
public abstract class InteractionForceFieldBase extends ForceFieldBase
                                                implements
                                                InteractionForceFieldInterface
{

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an interaction force field of given force intensity.
   * 
   * @param pForceIntensity
   *          force intensity
   */
  public InteractionForceFieldBase(float pForceIntensity)
  {
    super(pForceIntensity);
  }

}
