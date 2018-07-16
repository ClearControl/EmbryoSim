package simbryo.dynamics.tissue.embryo.zoo;

import simbryo.dynamics.tissue.embryo.EmbryoDynamics;
import simbryo.particles.forcefield.external.impl.IsoSurfaceForceField;
import simbryo.particles.isosurf.impl.Sphere;

/**
 * Cells divide 14 times and remain on a sphere.`
 *
 * @author royer
 */
public class Spheroid extends EmbryoDynamics
{
  private static final long serialVersionUID = 1L;

  protected static final float Fc = 0.0001f;
  protected static final float D = 0.9f;

  private static final float Fpetal = 0.0001f;
  private static final float Ri = 0.35f;
  private static final float Fradius = 0.20f;

  private static final float cRadiusShrinkageFactor =
                                                    (float) Math.pow(0.5f,
                                                                     1.0f / 2);

  private IsoSurfaceForceField mForceField;

  private volatile int mCellDivCount;

  /**
   * Creates a 'Spheroid'.
   *
   * @param pGridDimensions
   *          grid dimensions
   */
  public Spheroid(int... pGridDimensions)
  {
    super(Fc, D, 32, pGridDimensions);

    setSurface(new Sphere(Fradius, 0.5f, 0.5f, 0.5f));

    for (int i = 0; i < 1; i++)
    {
      float x = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float y = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float z = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    updateNeighborhoodGrid();

    mForceField = new IsoSurfaceForceField(Fpetal, getSurface());
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      if (mCellDivCount <= 14 && getTimeStepIndex() % 500 == 499)
        triggerCellDivision();

      applyForceField(mForceField);
      super.simulationSteps(1);
    }
  }

  private void triggerCellDivision()
  {

    int lNumberOfParticles = getNumberOfParticles();

    for (int i = 0; i < lNumberOfParticles; i++)
    {
      int lNewParticleId = cloneParticle(i, 0.001f);
      setTargetRadius(i, getRadius(i) * cRadiusShrinkageFactor);
      setTargetRadius(lNewParticleId,
                      getRadius(lNewParticleId)
                                      * cRadiusShrinkageFactor);
    }

    mCellDivCount++;
  }/**/

}
