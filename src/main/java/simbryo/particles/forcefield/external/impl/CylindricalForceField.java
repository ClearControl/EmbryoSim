package simbryo.particles.forcefield.external.impl;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.external.ExternalForceFieldBase;
import simbryo.particles.forcefield.external.ExternalForceFieldInterface;

/**
 * This force field applies a centri(petal+/fugal-) force to the particles in a
 * cylindrical geometry.
 * 
 * @author royer
 */
public class CylindricalForceField extends ExternalForceFieldBase
                                   implements
                                   ExternalForceFieldInterface
{
  private static final long serialVersionUID = 1L;

  private int mAxis;
  private float[] mCenter;

  /**
   * Constructs a centri(petal+/fugal-) cylindrical force field given a force
   * intensity and center. if the the force intensity is positive then it is a
   * centripetal force, otherwise it is a centrifugal force.
   * 
   * @param pAxis
   *          axis index (x=0, y=1, z=2, ...) along which there is no force
   *          (cylinder axis)
   * @param pForceIntensity
   *          force intensity
   * @param pCenter
   *          force field center
   */
  public CylindricalForceField(int pAxis,
                               float pForceIntensity,
                               float... pCenter)
  {
    super(pForceIntensity);
    mAxis = pAxis;
    mCenter = pCenter;
  }

  @SuppressWarnings("unused")
  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              float[] pForceFactor,
                              ParticleSystem pParticleSystem)
  {
    final int lDimension = pParticleSystem.getDimension();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    final float[] lVector = new float[lDimension];

    for (int i =
               lIndexStart, id =
                               pBeginId; i < lIndexEnd; i +=
                                                          lDimension, id++)
    {
      float lSquaredLength = 0;
      for (int d = 0; d < lDimension; d++)
      {
        float px = lPositionsRead[i + d];
        float cx = mCenter[d];
        float dx = d == mAxis ? 0 : cx - px;
        lVector[d] = dx;

        lSquaredLength += dx * dx;
      }

      float lInverseLengthTimesForce =
                                     (float) (mForceIntensity
                                              * (pForceFactor != null ? pForceFactor[id]
                                                                      : 1)
                                              / Math.sqrt(lSquaredLength));

      for (int d = 0; d < lDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lVector[d]
                                    * lInverseLengthTimesForce;
      }

    }

    pParticleSystem.getVelocities().swap();
  }

}
