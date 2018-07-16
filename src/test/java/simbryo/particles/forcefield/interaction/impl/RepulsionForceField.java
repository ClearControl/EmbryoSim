package simbryo.particles.forcefield.interaction.impl;

import java.util.SplittableRandom;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.InteractionForceFieldBase;
import simbryo.particles.forcefield.interaction.InteractionForceFieldInterface;
import simbryo.util.geom.GeometryUtils;

/**
 * This interaction force field applies a repulsion force from all-against-all
 * particles. To make this computationally tractable, a random subset is picked
 * at every computation step.
 * 
 * 
 */
public class RepulsionForceField extends InteractionForceFieldBase
                                 implements
                                 InteractionForceFieldInterface
{
  private static final long serialVersionUID = 1L;

  private int mNumberOfInteractionPartners;

  private transient SplittableRandom mRandom;

  /**
   * Constructs a collision force field given a force intensity and a percentage
   * of interaction partners.
   * 
   * @param pForceIntensity
   *          constant force applied during collision.
   * @param pNumberOfInteractionPartners
   *          number of interaction partners
   */
  public RepulsionForceField(float pForceIntensity,
                             int pNumberOfInteractionPartners)
  {
    super(pForceIntensity);
    mNumberOfInteractionPartners = pNumberOfInteractionPartners;
  }

  @SuppressWarnings("unused")
  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              float[] pForceFactor,
                              ParticleSystem pParticleSystem)
  {
    if (mRandom == null)
      mRandom = new SplittableRandom();

    final int lDimension = pParticleSystem.getDimension();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();

    pParticleSystem.getVelocities().copyDefault(pBeginId * lDimension,
                                                pEndId * lDimension);

    final int lNumberOfInteractionPartners =
                                           mNumberOfInteractionPartners;
    final float lForceIntensity = mForceIntensity
                                  / lNumberOfInteractionPartners;

    for (int idu =
                 pBeginId, i = idu
                               * lDimension; idu < pEndId; idu++, i +=
                                                                    lDimension)
    {

      for (int k = 0; k < lNumberOfInteractionPartners; k++)
      {
        final int idv = mRandom.nextInt(pBeginId, pEndId);

        if (idv == idu)
          continue;

        int j = idv * lDimension;
        float lDistance = GeometryUtils.computeDistance(lDimension,
                                                        lPositionsRead,
                                                        idu,
                                                        idv);

        float lInvDistanceWithForce = lForceIntensity
                                      * (pForceFactor != null ? pForceFactor[idu]
                                                                * pForceFactor[idv]
                                                              : 1)
                                      / lDistance;

        for (int d = 0; d < lDimension; d++)
        {
          float lDelta =
                       lPositionsRead[i + d] - lPositionsRead[j + d];

          float lAxisVector = lInvDistanceWithForce * lDelta;

          lVelocitiesWrite[i + d] += lAxisVector;
          lVelocitiesWrite[j + d] += -lAxisVector;

        }

      }

    }

    pParticleSystem.getVelocities().swap();

  }

}
