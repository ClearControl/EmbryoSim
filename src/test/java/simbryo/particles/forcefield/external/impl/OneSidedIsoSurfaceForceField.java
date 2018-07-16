package simbryo.particles.forcefield.external.impl;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.external.ExternalForceFieldBase;
import simbryo.particles.forcefield.external.ExternalForceFieldInterface;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * This force field applies a ellipsoi(petal+/fugal-) force to the particles.
 * 
 *
 * @author royer
 */
public class OneSidedIsoSurfaceForceField extends
                                          ExternalForceFieldBase
                                          implements
                                          ExternalForceFieldInterface
{
  private static final long serialVersionUID = 1L;

  private boolean mZeroInside;
  private boolean mConstraintWithRadius;

  private IsoSurfaceInterface mIsoSurfaceInterface;

  /**
   * Constructs a one-sided iso-surface force field. The force intensity sign
   * decides whether the force points towards the surface (+) or away from the
   * surface (-). A boolean flag decides whether the force is zero on the inside
   * (negative side) or outside (positive side) of the iso-surface. This force
   * field can be used to constrain the position of particles inside or outside
   * of a given iso-surface.
   * 
   * 
   * @param pZeroInside
   *          if true the force field is zero inside of the ellipsoid, otherwise
   *          it is zero outside.
   * @param pConstraintWithRadius
   *          if true, the radius of the particles is taken into account.
   * @param pForceIntensity
   *          force intensity
   * @param pIsoSurfaceInterface
   *          iso-surface
   */
  public OneSidedIsoSurfaceForceField(boolean pZeroInside,
                                      boolean pConstraintWithRadius,
                                      float pForceIntensity,
                                      IsoSurfaceInterface pIsoSurfaceInterface)
  {
    super(pForceIntensity);
    mZeroInside = pZeroInside;
    mConstraintWithRadius = pConstraintWithRadius;
    mIsoSurfaceInterface = pIsoSurfaceInterface;
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

    final float[] lRadiiRead = pParticleSystem.getRadii()
                                              .getWriteArray();

    float lForceIntensity = mForceIntensity;
    boolean lConstraintInside = mZeroInside;
    boolean lConstraintWithRadius = mConstraintWithRadius;

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    for (int i =
               lIndexStart, id =
                               pBeginId; i < lIndexEnd; i +=
                                                          lDimension, id++)
    {
      mIsoSurfaceInterface.clear();

      for (int d = 0; d < lDimension; d++)
      {
        float px = lPositionsRead[i + d];
        mIsoSurfaceInterface.addCoordinate(px);
      }

      float lDistance = mIsoSurfaceInterface.getDistance();

      float lRadius = lRadiiRead[id];

      float lValue = (lConstraintInside ? -1 : 1)
                     * (lConstraintWithRadius ? lRadius : 0);

      float lSignedDistanceToEllipoid = lDistance - lValue;

      float lForceIntensityPerParticle = lForceIntensity
                                         * (pForceFactor != null ? pForceFactor[id]
                                                                 : 1);

      if (lConstraintInside && lSignedDistanceToEllipoid >= 0)
        for (int d = 0; d < lDimension; d++)
        {
          float dx = mIsoSurfaceInterface.getNormalizedGardient(d);
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                    + dx * lForceIntensityPerParticle;
        }
      else if (!lConstraintInside && lSignedDistanceToEllipoid < 0)
        for (int d = 0; d < lDimension; d++)
        {
          float dx = mIsoSurfaceInterface.getNormalizedGardient(d);
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                    - dx * lForceIntensityPerParticle;
        }
      else
        for (int d = 0; d < lDimension; d++)
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d];

    }

    pParticleSystem.getVelocities().swap();
  }

}
