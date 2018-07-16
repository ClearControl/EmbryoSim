package simbryo.particles.forcefield.interaction.impl;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.InteractionForceFieldBase;
import simbryo.particles.forcefield.interaction.InteractionForceFieldInterface;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.util.geom.GeometryUtils;

/**
 * This interaction force field applies a force to each particle that keeps
 * particles that are in close proximity (distance between influence spheres)
 * together This is a good way to create meshes of particles
 * 
 * 
 */
public class CohesionForceField extends InteractionForceFieldBase
                                implements
                                InteractionForceFieldInterface
{
  private static final long serialVersionUID = 1L;

  private float mDrag;

  private int[] mNeighboorsArray;

  /**
   * Constructs a cohesion force field given a force intensity and drag
   * coefficient. The drag coefficient is often necessary to prevent excessive
   * bouncing.
   * 
   * @param pForceIntensity
   *          constant force applied during collision.
   * @param pDrag
   *          drag applied to slow down particles.
   */
  public CohesionForceField(float pForceIntensity, float pDrag)
  {
    super(pForceIntensity);
    mDrag = pDrag;
  }

  @SuppressWarnings("unused")
  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              float[] pForceFactor,
                              ParticleSystem pParticleSystem)
  {
    final int lDimension = pParticleSystem.getDimension();

    NeighborhoodGrid lNeighborhoodGrid =
                                       pParticleSystem.getNeighborhoodGrid();
    final int lMaxNumberOfParticlesPerGridCell =
                                               lNeighborhoodGrid.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = lNeighborhoodGrid.getVolume();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();
    final float[] lRadii =
                         pParticleSystem.getRadii().getCurrentArray();

    pParticleSystem.getVelocities().copyAndMult(pBeginId * lDimension,
                                                pEndId * lDimension,
                                                mDrag);

    int lNeighboorhoodListMaxLength = lMaxNumberOfParticlesPerGridCell
                                      * lTotalNumberOfCells;
    if (mNeighboorsArray == null
        || mNeighboorsArray.length != lNeighboorhoodListMaxLength)
    {
      mNeighboorsArray = new int[lNeighboorhoodListMaxLength];
    }

    final int[] lNeighboors = mNeighboorsArray;
    final int[] lNeighboorsTemp = mNeighboorsArray;
    final float[] lCellCoord = new float[lDimension];
    final int[] lCellCoordMin = new int[lDimension];
    final int[] lCellCoordMax = new int[lDimension];
    final int[] lCellCoordCurrent = new int[lDimension];

    for (int idu =
                 pBeginId, i = idu
                               * lDimension; idu < pEndId; idu++, i +=
                                                                    lDimension)
    {

      final float ru = lRadii[idu];

      int lNumberOfNeighboors =
                              lNeighborhoodGrid.getAllNeighborsForParticle(lNeighboors,
                                                                           lNeighboorsTemp,
                                                                           lPositionsRead,
                                                                           idu,
                                                                           ru,
                                                                           lCellCoord,
                                                                           lCellCoordMin,
                                                                           lCellCoordMax,
                                                                           lCellCoordCurrent);

      for (int k = 0; k < lNumberOfNeighboors; k++)
      {
        final int idv = lNeighboors[k];

        final float rv = lRadii[idv];

        // testing bounding box collision:
        if (idu < idv) /*
                       && GeometryUtils.detectBoundingBoxCollision(lDimension,
                                                    lPositionsRead,
                                                    ru,
                                                    rv,
                                                    idu,
                                                    idv)/**/
        {
          int j = idv * lDimension;
          /// System.out.println("BB collision");
          float lDistance = GeometryUtils.computeDistance(lDimension,
                                                          lPositionsRead,
                                                          idu,
                                                          idv);
          float lGap = lDistance - ru - rv;

          // testing sphere proximity:
          if (lGap < 0.5f * (ru + rv) && lGap > 0f && lDistance != 0)
          {

            // Cohesion -> apply force.
            // System.out.println("Cohesion -> apply force.");
            float lInvDistance = 1.0f / lDistance;
            float lInvDistanceWithForce = mForceIntensity
                                          * (pForceFactor != null ? pForceFactor[idu]
                                                                    * pForceFactor[idv]
                                                                  : 1)
                                          * lInvDistance;

            for (int d = 0; d < lDimension; d++)
            {
              float lDelta = lPositionsRead[i + d]
                             - lPositionsRead[j + d];

              float lAxisVector = lInvDistanceWithForce * lDelta;

              lVelocitiesWrite[i + d] += -lAxisVector;
              lVelocitiesWrite[j + d] += +lAxisVector;

            }

          }
        }

      }

    }

    pParticleSystem.getVelocities().swap();

  }

}
