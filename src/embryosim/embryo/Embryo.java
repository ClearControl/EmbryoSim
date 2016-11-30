package embryosim.embryo;

import java.util.ArrayList;

import embryosim.morphogen.Morphogen;
import embryosim.morphogen.operators.MorphogenOperatorInterface;
import embryosim.psystem.ParticleSystem;
import embryosim.psystem.forcefield.interaction.impl.CollisionForceField;
import embryosim.sequence.Sequence;
import embryosim.util.DoubleBufferingFloatArray;
import embryosim.viewer.ParticleViewerInterface;
import embryosim.viewer.three.ParticleViewer3D;

/**
 * Embryos extend from a particle system with standard dynamics appropriate for
 * simulating the motion of cells, as well as features such as morphogens and
 * target radii.
 *
 * @author royer
 */
public class Embryo extends ParticleSystem
{

  protected static final int cMaximumNumberOfCells = 100000;
  protected static final float V = 0.0001f;
  protected static final float Rt = 0.01f;
  protected static final float Rm = 0.005f;

  protected static final float Db = 0.9f;

  protected static final float Fg = 0.000001f;

  protected static final float Ar = 0.05f;
  protected static final float Fb = 0.0001f;

  private final DoubleBufferingFloatArray mTargetRadii;

  protected final CollisionForceField mCollisionForceField;

  protected ArrayList<Morphogen> mMorphogenList = new ArrayList<>();

  protected volatile long mTimeStepIndex = 0;
  protected final Sequence mSequence = new Sequence();

  private ParticleViewer3D mParticleViewer3D;

  /**
   * Constructs an embryo of given dimensions (2D or 3D), collision force
   * between particles, and drag.
   * 
   * @param pDimension
   *          dimension
   * @param pCollisionForce
   *          collision force
   * @param pDrag
   *          drag.
   */
  public Embryo(int pDimension, float pCollisionForce, float pDrag)
  {
    super(pDimension, cMaximumNumberOfCells, Rm, Rt);

    mTargetRadii =
                 new DoubleBufferingFloatArray(cMaximumNumberOfCells);

    mCollisionForceField = new CollisionForceField(pCollisionForce,
                                                   pDrag,
                                                   false);
  }

  /**
   * Returns the current time step index.
   * 
   * @return current time step index.
   */
  public long getTimeStepIndex()
  {
    return mTimeStepIndex;
  }

  /**
   * Sets the target radius for a given particle id.
   * 
   * @param pParticleId
   *          particle id
   * @param pTargetRadius
   *          target radius
   */
  public void setTargetRadius(int pParticleId, float pTargetRadius)
  {
    mTargetRadii.getCurrentArray()[pParticleId] = pTargetRadius;
  }

  @Override
  public void copyParticle(int pSourceParticleId,
                           int pDestinationParticleId)
  {
    super.copyParticle(pSourceParticleId, pDestinationParticleId);
    mTargetRadii.getCurrentArray()[pDestinationParticleId] =
                                                           mTargetRadii.getCurrentArray()[pDestinationParticleId];
  }

  @Override
  public int cloneParticle(int pSourceParticleId, float pNoiseFactor)
  {
    int lNewParticleId = super.cloneParticle(pSourceParticleId,
                                             pNoiseFactor);
    mTargetRadii.getCurrentArray()[lNewParticleId] =
                                                   mTargetRadii.getCurrentArray()[pSourceParticleId];

    for (Morphogen lMorphogen : mMorphogenList)
    {
      lMorphogen.copyValue(pSourceParticleId, lNewParticleId);
    }

    return lNewParticleId;
  }

  /**
   * Adds a new morphogen to this embryo.
   * 
   * @return the new morphogen.
   */
  protected Morphogen addMorphogen()
  {
    Morphogen lMorphogen = new Morphogen(this);
    mMorphogenList.add(lMorphogen);
    return lMorphogen;
  }

  /**
   * Applies a number of simulationsteps to the embryo.
   * 
   * @param pNumberOfSteps
   *          number of simulation steps.
   * @param pDeltaTime
   *          delta time for each step.
   */
  public void simulationSteps(int pNumberOfSteps, float pDeltaTime)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      addBrownianMotion(Fb);
      smoothToTargetRadius(Ar);
      applyForceField(mCollisionForceField);
      intergrateEuler();
      enforceBounds(Db);
      updateNeighborhoodCells();
      mTimeStepIndex++;
      mSequence.step(pDeltaTime);
    }

    if (mParticleViewer3D != null)
      mParticleViewer3D.updateDisplay(true);

  }

  /**
   * Smoothly converges current particle radii to the target radii.
   * 
   * @param pAlpha
   *          exponential coefficient.
   */
  private void smoothToTargetRadius(float pAlpha)
  {
    final float[] lRadiiReadArray = mRadii.getReadArray();
    final float[] lRadiiWriteArray = mRadii.getWriteArray();
    final float[] lTargetRadiiArray = mTargetRadii.getCurrentArray();
    final int lNumberOfParticles = getNumberOfParticles();

    for (int id = 0; id < lNumberOfParticles; id++)
    {
      lRadiiWriteArray[id] = (1 - pAlpha) * lRadiiReadArray[id]
                             + pAlpha * lTargetRadiiArray[id];
    }

    mRadii.swap();
  }

  /**
   * opens the 3D viewer for this embryo.
   * 
   * @return 3D viewer.
   */
  public ParticleViewer3D open3DViewer()
  {
    if (mParticleViewer3D == null)
      mParticleViewer3D = ParticleViewer3D.view(this,
                                                "Viewing: "
                                                      + getClass().getSimpleName(),
                                                768,
                                                768);
    return mParticleViewer3D;
  }

  /**
   * returns the current 3D viewer for this embryo.
   * 
   * @return 3D viewer.
   */
  public ParticleViewerInterface getViewer()
  {
    return mParticleViewer3D;
  }

  /**
   * Applies a single simulation step for an operator and a set of morphogens.
   * 
   * @param pOperator
   *          operator
   * @param pMorphogens
   *          a list of morphogens
   */
  public void applyOperator(MorphogenOperatorInterface pOperator,
                    Morphogen... pMorphogens)
  {
    apply(0, getNumberOfParticles(), pOperator, pMorphogens);
  }

  /**
   * Applies a single simulation step for an operator and a set of morphogens
   * for a given range of cell ids.
   * 
   * @param pBeginId
   *          begin id
   * @param pEndId
   *          end id
   * @param pOperator
   *          operator
   * @param pMorphogens
   *          a list of morphogens
   */
  public void apply(int pBeginId,
                    int pEndId,
                    MorphogenOperatorInterface pOperator,
                    Morphogen... pMorphogens)
  {
    pOperator.apply(pBeginId, pEndId, this, pMorphogens);
  }

}
