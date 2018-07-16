package simbryo.particles;

import java.util.concurrent.ThreadLocalRandom;

import simbryo.particles.forcefield.ForceFieldInterface;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.util.DoubleBufferingFloatArray;

/**
 * N-dimensional Particle system implementation. Particles have a position and
 * 'radius-of-influence' which can be thought as the proper radius of the
 * particle or as a bounding sphere. This class implements various methods to
 * 'run' the dynamics and apply the forces to and between particles.
 *
 * @author royer
 */
public class ParticleSystem implements ParticleSystemInterface
{
  private static final long serialVersionUID = 1L;

  private final int mDimension;
  private final int mMaxNumberOfParticles;
  private final int mMaxNumberOfParticlesPerGridCell;
  private int mNumberOfParticles;

  protected final DoubleBufferingFloatArray mPositions;
  protected final DoubleBufferingFloatArray mVelocities;
  protected final DoubleBufferingFloatArray mRadii;

  private final NeighborhoodGrid mNeighborhood;

  /**
   * Creates a particle system with a give number of dimensions, number of
   * particles, minimal radius, and typical radius.
   * 
   * @param pDimension
   *          dimension
   * @param pMaxNumberOfParticles
   *          max number of particles
   * @param pMinRradius
   *          minimal radius
   * @param pTypicalRadius
   *          typicall radius
   */
  public ParticleSystem(int pDimension,
                        int pMaxNumberOfParticles,
                        float pMinRradius,
                        float pTypicalRadius)
  {
    this(32
         + getOptimalMaxNumberOfParticlesPerGridCell(pDimension,
                                                     pMaxNumberOfParticles,
                                                     pMinRradius,
                                                     pTypicalRadius),
         pMaxNumberOfParticles,
         getOptimalGridDimensions(pDimension,
                                  pMaxNumberOfParticles,
                                  pTypicalRadius));
  }

  private static int[] getOptimalGridDimensions(int pDimension,
                                                int pMaxNumberOfParticles,
                                                float pTypicalRadius)
  {
    int lOptimalGridSize =
                         (int) Math.max(4,
                                        1 / (2 * 2 * pTypicalRadius));

    int[] lGridDimensions = new int[pDimension];

    for (int d = 0; d < pDimension; d++)
      lGridDimensions[d] = lOptimalGridSize;

    return lGridDimensions;
  }

  private static int getOptimalMaxNumberOfParticlesPerGridCell(int pDimension,
                                                               int pMaxNumberOfParticles,
                                                               float pMinRradius,
                                                               float pTypicalRadius)
  {
    int[] lOptimalGridDimensions =
                                 getOptimalGridDimensions(pDimension,
                                                          pMaxNumberOfParticles,
                                                          pTypicalRadius);

    int lVolume = 1;
    for (int d = 0; d < pDimension; d++)
      lVolume *= lOptimalGridDimensions[d];

    float lCellVolume = 1.0f / lVolume;
    float lTypicalParticleVolume = (float) Math.pow(2 * pMinRradius,
                                                    pDimension);

    int lMaxNumberOfParticlesPerCell =
                                     (int) Math.ceil(lCellVolume
                                                     / lTypicalParticleVolume);

    return lMaxNumberOfParticlesPerCell;
  }

  /**
   * Constructs a particle system with a given grid size, max number of
   * particles and particles per cell.
   * 
   * @param pMaxNumberOfParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pMaxNumberOfParticles
   *          max number of particles
   * @param pGridDimensions
   *          grid dimensions
   */
  public ParticleSystem(int pMaxNumberOfParticlesPerGridCell,
                        int pMaxNumberOfParticles,
                        int... pGridDimensions)
  {
    super();

    mMaxNumberOfParticles = pMaxNumberOfParticles;
    mMaxNumberOfParticlesPerGridCell =
                                     pMaxNumberOfParticlesPerGridCell;
    mDimension = pGridDimensions.length;
    mPositions = new DoubleBufferingFloatArray(pMaxNumberOfParticles
                                               * mDimension);
    mVelocities = new DoubleBufferingFloatArray(pMaxNumberOfParticles
                                                * mDimension);
    mRadii = new DoubleBufferingFloatArray(pMaxNumberOfParticles);
    mNeighborhood =
                  new NeighborhoodGrid(pMaxNumberOfParticlesPerGridCell,
                                       pGridDimensions);
  }

  @Override
  public DoubleBufferingFloatArray getPositions()
  {
    return mPositions;
  }

  @Override
  public DoubleBufferingFloatArray getVelocities()
  {
    return mVelocities;
  }

  @Override
  public DoubleBufferingFloatArray getRadii()
  {
    return mRadii;
  }

  @Override
  public NeighborhoodGrid getNeighborhoodGrid()
  {
    return mNeighborhood;
  }

  @Override
  public int getNumberOfParticles()
  {
    return mNumberOfParticles;
  }

  @Override
  public int getMaxNumberOfParticles()
  {
    return mMaxNumberOfParticles;
  }

  @Override
  public int getMaxNumberOfParticlesPerGridCell()
  {
    return mMaxNumberOfParticlesPerGridCell;
  }

  @Override
  public int getDimension()
  {
    return mDimension;
  }

  @Override
  public int[] getGridDimensions()
  {
    return getNeighborhoodGrid().getGridDimensions();
  }

  @Override
  public float getRadius(int pParticleId)
  {
    return mRadii.getCurrentArray()[pParticleId];
  }

  @Override
  public int addParticle(float... pPosition)
  {
    if (mNumberOfParticles >= mMaxNumberOfParticles)
      return -1;

    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final int lParticleId = mNumberOfParticles;
    final int i = lParticleId * lDimension;

    for (int d = 0; d < Math.min(mDimension, pPosition.length); d++)
    {
      lPositionsRead[i + d] = pPosition[d];
      lPositionsWrite[i + d] = pPosition[d];
    }

    mNumberOfParticles++;

    return lParticleId;
  }

  @Override
  public void removeParticle(int pParticleId)
  {
    final int lLastParticleId = mNumberOfParticles - 1;
    copyParticle(lLastParticleId, pParticleId);
    mNumberOfParticles--;
  }

  @Override
  public void copyParticle(int pSourceParticleId,
                           int pDestinationParticleId)
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final float[] lRadiiRead = mRadii.getReadArray();
    final float[] lRadiiWrite = mRadii.getWriteArray();

    for (int d = 0; d < lDimension; d++)
    {

      lPositionsRead[pDestinationParticleId * lDimension
                     + d] =
                          lPositionsRead[pSourceParticleId
                                         * lDimension + d];

      lPositionsWrite[pDestinationParticleId * lDimension
                      + d] =
                           lPositionsWrite[pSourceParticleId
                                           * lDimension + d];

      lVelocitiesRead[pDestinationParticleId * lDimension
                      + d] =
                           lVelocitiesRead[pSourceParticleId
                                           * lDimension + d];
      lVelocitiesWrite[pDestinationParticleId * lDimension
                       + d] =
                            lVelocitiesWrite[pSourceParticleId
                                             * lDimension + d];
    }

    lRadiiRead[pDestinationParticleId] =
                                       lRadiiRead[pSourceParticleId];
    lRadiiWrite[pDestinationParticleId] =
                                        lRadiiWrite[pSourceParticleId];
  }

  @Override
  public int cloneParticle(int pSourceParticleId, float pNoiseFactor)
  {
    int lNewParticleId = addParticle();
    if (lNewParticleId < 0)
      return lNewParticleId;
    copyParticle(pSourceParticleId, lNewParticleId);
    addNoiseToParticle(lNewParticleId, pNoiseFactor, 0, 0);
    return lNewParticleId;
  }

  @Override
  public void addNoiseToParticle(int pParticleId,
                                 float pPositionNoise,
                                 float pVelocityNoise,
                                 float pRadiusNoise)
  {
    final int lDimension = mDimension;
    final float[] lPositions = mPositions.getCurrentArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final float[] lRadii = mRadii.getCurrentArray();

    ThreadLocalRandom lRandom = ThreadLocalRandom.current();

    for (int d = 0; d < lDimension; d++)
    {
      float lPositionNoiseValue =
                                (float) ((lRandom.nextDouble() - 0.5)
                                         * 2 * pPositionNoise);
      float lVelocityNoiseValue =
                                (float) ((lRandom.nextDouble() - 0.5)
                                         * 2 * pVelocityNoise);
      lPositions[pParticleId * lDimension + d] += lPositionNoiseValue;

      lVelocities[pParticleId * lDimension + d] +=
                                                lVelocityNoiseValue;
    }
    float lRadiusNoiseValue = (float) ((lRandom.nextDouble() - 0.5)
                                       * 2 * pVelocityNoise);
    lRadii[pParticleId] += lRadiusNoiseValue;
  }

  @Override
  public void setPosition(int pParticleId, float... pParticlePosition)
  {
    final float[] lPositions = mPositions.getCurrentArray();
    final int i = mDimension * pParticleId;
    for (int d = 0; d < mDimension; d++)
    {
      lPositions[i + d] = pParticlePosition[d];
    }
  }

  @Override
  public void setVelocity(int pParticleId, float... pVelocity)
  {
    final float[] lVelocities = mVelocities.getCurrentArray();
    final int i = mDimension * pParticleId;
    for (int d = 0; d < mDimension; d++)
    {
      lVelocities[i + d] = pVelocity[d];
    }
  }

  @Override
  public void setRadius(int pParticleId, float pRadius)
  {
    float[] lRadii = mRadii.getCurrentArray();
    lRadii[pParticleId] = pRadius;
  }

  @Override
  public void updateNeighborhoodGrid()
  {
    updateNeighborhoodGrid(getNeighborhoodGrid());
  }

  @Override
  public void updateNeighborhoodGrid(NeighborhoodGrid pNeighborhoodGrid)
  {
    pNeighborhoodGrid.clear();
    float[] lPositions = mPositions.getCurrentArray();
    float[] lRadii = mRadii.getCurrentArray();
    pNeighborhoodGrid.update(lPositions, lRadii, mNumberOfParticles);
  }

  @Override
  public void repelAround(float pFactor,
                          float pCenterX,
                          float pCenterY)
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
    {

      float x = lPositionsRead[i + 0];
      float y = lPositionsRead[i + 1];
      float ux = x - pCenterX;
      float uy = y - pCenterY;
      float l = (float) Math.sqrt(ux * ux + uy * uy);
      float n = pFactor / l;
      float fx = n * ux;
      float fy = n * uy;

      lVelocitiesWrite[i + 0] = 0.99f * lVelocitiesRead[i + 0] + fx;
      lVelocitiesWrite[i + 1] = 0.99f * lVelocitiesRead[i + 1] + fy;
    }

    mVelocities.swap();

  }

  @Override
  public void enforceBounds(float pDampening)
  {
    enforceBounds(pDampening, 1e-6f);
  }

  @Override
  public void enforceBounds(float pDampening, float pNoise)
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final float[] lRadiiRead = mRadii.getReadArray();

    ThreadLocalRandom lRandom = ThreadLocalRandom.current();

    for (int id = 0; id < mNumberOfParticles; id++)
    {
      for (int d = 0; d < lDimension; d++)
      {
        int i = id * lDimension + d;
        float lRadius = lRadiiRead[id];

        if (lPositionsRead[i] < lRadius)
        {
          lPositionsWrite[i] = (float) (lRadius
                                        + lRandom.nextDouble(-pNoise,
                                                             pNoise));
          lVelocitiesWrite[i] = -pDampening * lVelocitiesRead[i];
        }
        else if (lPositionsRead[i] > 1 - lRadius)
        {
          lPositionsWrite[i] = (float) (1 - lRadius
                                        + lRandom.nextDouble(-pNoise,
                                                             pNoise));
          lVelocitiesWrite[i] = -pDampening * lVelocitiesRead[i];
        }
        else
        {
          lPositionsWrite[i] = lPositionsRead[i];
          lVelocitiesWrite[i] = lVelocitiesRead[i];
        }
      }
    }

    mPositions.swap();
    mVelocities.swap();
  }

  @Override
  public void addBrownianMotion(float pAmount)
  {
    final int lDimension = mDimension;
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();

    ThreadLocalRandom lRandom = ThreadLocalRandom.current();

    for (int id = 0; id < mNumberOfParticles; id++)
    {
      for (int d = 0; d < lDimension; d++)
      {
        int i = id * lDimension + d;

        lVelocitiesWrite[i] = (float) (lVelocitiesRead[i]
                                       + pAmount
                                         * lRandom.nextDouble(-1, 1));
      }
    }

    mVelocities.swap();
  }

  @Override
  public void applyForce(float... pForce)
  {
    applyForce(0, getNumberOfParticles(), pForce);
  }

  @Override
  public void applyForce(int pBeginId, int pEndId, float... pForce)
  {
    final int lDimension = mDimension;
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    for (int i = lIndexStart; i < lIndexEnd; i += lDimension)
      for (int d = 0; d < lDimension; d++)
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d] + pForce[d];

    mVelocities.swap();
  }

  @Override
  public void applyForceField(ForceFieldInterface pForceField)
  {
    applyForceField(pForceField, 0, getNumberOfParticles(), null);
  }

  @Override
  public void applyForceField(ForceFieldInterface pForceField,
                              int pBeginId,
                              int pEndId,
                              float[] pForceFactor)
  {
    pForceField.applyForceField(pBeginId, pEndId, pForceFactor, this);
  }

  @Override
  public void intergrateEuler()
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
    {
      for (int d = 0; d < lDimension; d++)
        lPositionsWrite[i + d] = lPositionsRead[i + d]
                                 + lVelocities[i + d];
    }

    mPositions.swap();

  }

  @Override
  public void intergrateTrapezoidal()
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocitiesCurrent = mVelocities.getCurrentArray();
    final float[] lVelocitiesPrevious =
                                      mVelocities.getPreviousArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
    {
      for (int d = 0; d < lDimension; d++)
        lPositionsWrite[i + d] = lPositionsRead[i + d]
                                 + 0.5f
                                   * (lVelocitiesCurrent[i + d]
                                      + lVelocitiesPrevious[i + d]);
    }

    mPositions.swap();

  }

  @Override
  public int copyPositions(float[] pPositionsCopy)
  {
    mPositions.copyCurrentArrayTo(pPositionsCopy,
                                  mNumberOfParticles * mDimension);
    return mNumberOfParticles;
  }

  @Override
  public int copyVelocities(float[] pVelocitiesCopy)
  {
    mVelocities.copyCurrentArrayTo(pVelocitiesCopy,
                                   mNumberOfParticles * mDimension);
    return mNumberOfParticles;
  }

  @Override
  public int copyRadii(float[] pRadiiCopy)
  {
    mRadii.copyCurrentArrayTo(pRadiiCopy, mNumberOfParticles);
    return mNumberOfParticles;
  }

}
