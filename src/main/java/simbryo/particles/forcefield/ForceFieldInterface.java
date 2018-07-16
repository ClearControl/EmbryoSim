package simbryo.particles.forcefield;

import java.io.Serializable;

import simbryo.particles.ParticleSystem;

/**
 * Force fields can be applied to particle systems to influence their movement.
 *
 * @author royer
 */
public interface ForceFieldInterface extends Serializable
{

  /**
   * Returns the intensity of the force field.
   * 
   * @return force intensity
   */
  float getForceIntensity();

  /**
   * Sets the force intensity. If zero the force field should have no influence
   * on the particle dynamics.
   * 
   * @param pForce
   *          force intensity
   */
  void setForceIntensity(float pForce);

  /**
   * Applies the nD force field to particles within a given range of ids (begin
   * inclusive, end exclusive). A float array can be provided to adjust the
   * force per particle. If the float array is null then it is ignored.
   * 
   * @param pBeginId
   *          particle id range beginning inclusive
   * @param pEndId
   *          particle id range end exclusive
   * @param pFactor
   *          applies a force factor per particle, ignored if null.
   * @param pParticleSystem
   *          particle system
   * 
   */
  void applyForceField(int pBeginId,
                       int pEndId,
                       float[] pFactor,
                       ParticleSystem pParticleSystem);

}
