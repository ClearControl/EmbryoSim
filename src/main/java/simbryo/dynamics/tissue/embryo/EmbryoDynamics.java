package simbryo.dynamics.tissue.embryo;

import java.io.Serializable;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Embryos extend from a tissue dynamics and add the notion of 'embryo surface'
 *
 * @author royer
 */
public class EmbryoDynamics extends TissueDynamics implements
                            TissueDynamicsInterface,
                            HasSurface,
                            Serializable
{
  private static final long serialVersionUID = 1L;

  private IsoSurfaceInterface mEmbryoSurface;

  /**
   * Instanciates embryo dynamics given a collision force, drag coefficient, max
   * number of particles per grid cell, and grid dimensions.
   * 
   * @param pCollisionForce
   *          collision force
   * @param pDrag
   *          drag coefficient
   * @param pMaxNumberOfParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pGridDimensions
   *          grid dimensions
   */
  public EmbryoDynamics(float pCollisionForce,
                        float pDrag,
                        int pMaxNumberOfParticlesPerGridCell,
                        int[] pGridDimensions)
  {

    super(pCollisionForce,
          pDrag,
          pMaxNumberOfParticlesPerGridCell,
          pGridDimensions);

  }

  @Override
  public IsoSurfaceInterface getSurface()
  {
    return mEmbryoSurface;
  }

  @Override
  public void setSurface(IsoSurfaceInterface pEmbryoSurface)
  {
    mEmbryoSurface = pEmbryoSurface;
  }

}
