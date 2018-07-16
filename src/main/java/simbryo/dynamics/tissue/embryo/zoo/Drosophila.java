package simbryo.dynamics.tissue.embryo.zoo;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import simbryo.dynamics.tissue.cellprop.CellProperty;
import simbryo.dynamics.tissue.cellprop.HasPolarity;
import simbryo.dynamics.tissue.cellprop.VectorCellProperty;
import simbryo.dynamics.tissue.cellprop.operators.impl.StrogatzWaveOperator;
import simbryo.dynamics.tissue.cellprop.operators.impl.SurfaceGradientOperator;
import simbryo.dynamics.tissue.embryo.EmbryoDynamics;
import simbryo.particles.forcefield.ForceFieldInterface;
import simbryo.particles.forcefield.external.impl.OneSidedIsoSurfaceForceField;
import simbryo.particles.isosurf.impl.Ellipsoid;
import simbryo.util.serialization.SerializationUtilities;

/**
 * Drosophila melanogster embryo (First 14 divisions).
 *
 * @author royer
 */
public class Drosophila extends EmbryoDynamics
                        implements HasPolarity, Serializable
{
  private static final long serialVersionUID = 1L;

  private static final float cCellDivisionRadiusShrinkage =
                                                          (float) Math.pow(0.5f,
                                                                           1.0f / 2);
  protected static final float Fc = 0.00008f;
  protected static final float D = 0.9f;

  private static final float Finside = 0.0002f;
  private static final float Fafc = 0.00005f;

  private static final float Ri = 0.08f;

  private ForceFieldInterface mOutsideEllipseForceField;
  private ForceFieldInterface mInsideEllipseForceField;

  private CellProperty mCellCycleProperty;
  private StrogatzWaveOperator mStrogatzOscillator;

  private VectorCellProperty mPolarityProperty;
  private SurfaceGradientOperator mSurfaceGradientOperator;

  private volatile int mCellDivCount;

  private final float mEllipsoidA = 0.43f, mEllipsoidB = 1.00f,
      mEllipsoidC = 0.43f, mEllipsoidR = 0.47f;

  /**
   * Creates a Drosophila embryo.
   *
   * @param pMaxNumberOfParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pGridDimensions
   *          grid dimensions
   */
  public Drosophila(int pMaxNumberOfParticlesPerGridCell,
                    int... pGridDimensions)
  {
    super(Fc, D, pMaxNumberOfParticlesPerGridCell, pGridDimensions);

    setSurface(new Ellipsoid(getEllipsoidR(),
                             0.5f,
                             0.5f,
                             0.5f,
                             getEllipsoidA(),
                             getEllipsoidB(),
                             getEllipsoidC()));

    for (int i = 0; i < 1; i++)
    {
      float x = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float y = (float) (Ri - 0.02 * Math.random());
      float z = (float) (0.5f + Ri * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    updateNeighborhoodGrid();

    mOutsideEllipseForceField =
                              new OneSidedIsoSurfaceForceField(true,
                                                               true,
                                                               Finside,
                                                               getSurface());
    mInsideEllipseForceField =
                             new OneSidedIsoSurfaceForceField(false,
                                                              false,
                                                              Fafc,
                                                              getSurface());

    mCellCycleProperty = addCellProperty();
    mStrogatzOscillator =
                        new StrogatzWaveOperator(0.001f, 0.01f, 0.1f)
                        {
                          private static final long serialVersionUID =
                                                                     1L;

                          @Override
                          public float eventHook(boolean pEvent,
                                                 int pId,
                                                 float[] pPositions,
                                                 float[] pVelocities,
                                                 float[] pRadii,
                                                 float pNewMorphogenValue)
                          {
                            return cellDivisionHook(pEvent,
                                                    pId,
                                                    pNewMorphogenValue);
                          }

                        };

    mPolarityProperty = addVectorCellProperty(3);
    mPolarityProperty.initializeRandom();
    mPolarityProperty.normalize();
    mSurfaceGradientOperator = new SurfaceGradientOperator();

  }

  /**
   * Returns the Drosophilas ellipsoid X axis A parameter
   * 
   * @return A parameter
   */
  public float getEllipsoidA()
  {
    return mEllipsoidA;
  }

  /**
   * Returns the Drosophilas ellipsoid X axis B parameter
   * 
   * @return B parameter
   */
  public float getEllipsoidB()
  {
    return mEllipsoidB;
  }

  /**
   * Returns the Drosophilas ellipsoid X axis C parameter
   * 
   * @return C parameter
   */
  public float getEllipsoidC()
  {
    return mEllipsoidC;
  }

  /**
   * Returns the Drosophilas ellipsoid R parameter (radius)
   * 
   * @return R parameter
   */
  public float getEllipsoidR()
  {
    return mEllipsoidR;
  }

  @Override
  public VectorCellProperty getPolarityProperty()
  {
    return mPolarityProperty;
  }

  private float cellDivisionHook(boolean pEvent,
                                 int pId,
                                 float pNewMorphogenValue)
  {
    int lDimension = getDimension();

    if (pEvent && pNewMorphogenValue < 14)
    {

      int lNewParticleId = cloneParticle(pId, 0.001f);

      mCellCycleProperty.getArray()
                        .getWriteArray()[lNewParticleId] =
                                                         pNewMorphogenValue;

      if (pNewMorphogenValue >= 6)
      {
        setTargetRadius(pId,
                        getRadius(pId)
                             * cCellDivisionRadiusShrinkage);
        setTargetRadius(lNewParticleId,
                        getRadius(lNewParticleId)
                                        * cCellDivisionRadiusShrinkage);
      }

      if (pNewMorphogenValue >= 5)
      {
        float[] lPositions = mPositions.getReadArray();
        float y = lPositions[pId * lDimension + 1];
        if (y > 0.9f)
          return (float) (pNewMorphogenValue + 0.1f * Math.pow(y, 4));
      }

    }

    if ((int) pNewMorphogenValue > mCellDivCount)
      System.out.println("Division: " + mCellDivCount);

    mCellDivCount = Math.max(mCellDivCount, (int) pNewMorphogenValue);

    return pNewMorphogenValue;
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      applyOperator(mStrogatzOscillator, mCellCycleProperty);
      applyOperator(mSurfaceGradientOperator, mPolarityProperty);

      adjustForceFieldInsideEmbryo();

      applyForceField(mOutsideEllipseForceField);
      applyForceField(mInsideEllipseForceField);

      super.simulationSteps(1);
    }
  }

  private void adjustForceFieldInsideEmbryo()
  {
    final float lForce;

    switch (mCellDivCount)
    {
    case 0:
      lForce = -1f * Fafc;
      break;
    case 1:
      lForce = -1f * Fafc;
      break;
    case 2:
      lForce = -1f * Fafc;
      break;
    case 3:
      lForce = -1f * Fafc;
      break;
    case 4:
      lForce = +0.1f * Fafc;
      break;
    case 5:
      lForce = +0.5f * Fafc;
      break;

    default:
      lForce = Fafc;

    }

    mInsideEllipseForceField.setForceIntensity(lForce);
  }

  /**
   * Returns a cached embryo dynamics state. Phantom width, height and depth are
   * provided to optimize the grid size.
   * 
   * @param pDivisionTime
   *          time in cell-division time
   * @return Drosohila dynamics at given state.
   * @throws IOException
   *           exception if problem savin/loading saved dynamics state
   */
  public static Drosophila getDeveloppedEmbryo(float pDivisionTime) throws IOException
  {
    File lTempDirectory =
                        new File(System.getProperty("java.io.tmpdir"));
    File lCachedEmbryoDynamicsFile =
                                   new File(lTempDirectory,
                                            Drosophila.class.getSimpleName()
                                                            + pDivisionTime);
    Drosophila lDrosophila =
                           SerializationUtilities.loadFromFile(Drosophila.class,
                                                               lCachedEmbryoDynamicsFile);

    if (lDrosophila == null)
    {

      lDrosophila = new Drosophila(64, 16, 16, 16);
      lDrosophila.simulationSteps((int) (pDivisionTime * 1000));
      SerializationUtilities.saveToFile(lDrosophila,
                                        lCachedEmbryoDynamicsFile);
    }
    return lDrosophila;
  }

}
