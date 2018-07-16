package simbryo.dynamics.tissue.epithelium;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.io.Serializable;
import java.util.ArrayList;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.dynamics.tissue.cellprop.CellProperty;
import simbryo.particles.forcefield.ForceFieldInterface;
import simbryo.particles.forcefield.external.impl.IsoSurfaceForceField;
import simbryo.particles.forcefield.interaction.impl.CohesionForceField;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Layered epithelium dynamics extend from a tissue dynamics and add the notion
 * of 'epithelium surface'
 *
 * @author royer
 */
public class LayeredEpitheliumDynamics extends TissueDynamics
                                       implements
                                       TissueDynamicsInterface,
                                       HasLayers,
                                       Serializable
{
  private static final long serialVersionUID = 1L;

  private ArrayList<IsoSurfaceInterface> mEpitheliumLayerIsoSurfaceList =
                                                                        new ArrayList<>();
  private ArrayList<ForceFieldInterface> mEpitheliumLayerForceFieldList =
                                                                        new ArrayList<>();
  private CellProperty mEpitheliumLayerCellForceProperty;

  private ForceFieldInterface mCohesionForceField;

  private float[] mMask;

  /**
   * Instantiates epithelium dynamics given a collision force, drag coefficient,
   * max number of particles per grid cell, and grid dimensions.
   * 
   * @param pCollisionForce
   *          collision force
   * @param pCohesionForce
   *          cohesion force
   * @param pDrag
   *          drag coefficient
   * @param pMaxNumberOfParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pGridDimensions
   *          grid dimensions
   */
  public LayeredEpitheliumDynamics(float pCollisionForce,
                                   float pCohesionForce,
                                   float pDrag,
                                   int pMaxNumberOfParticlesPerGridCell,
                                   int... pGridDimensions)
  {
    super(pCollisionForce,
          pDrag,
          pMaxNumberOfParticlesPerGridCell,
          pGridDimensions);

    mCohesionForceField =
                        new CohesionForceField(pCohesionForce, pDrag);

    mEpitheliumLayerCellForceProperty = addCellProperty();

    mEpitheliumLayerCellForceProperty.set(0,
                                          getMaxNumberOfParticles(),
                                          0f);

    mMask = new float[getMaxNumberOfParticles()];

  }

  @Override
  public void addLayer(IsoSurfaceInterface pLayerIsoSurface,
                       float pLayerForceIntensity)
  {
    mEpitheliumLayerIsoSurfaceList.add(pLayerIsoSurface);

    ForceFieldInterface lForceField =
                                    new IsoSurfaceForceField(pLayerForceIntensity,
                                                             pLayerIsoSurface);

    mEpitheliumLayerForceFieldList.add(lForceField);

  }

  @Override
  public int getNumberOfLayers()
  {
    return mEpitheliumLayerIsoSurfaceList.size();
  }

  @Override
  public IsoSurfaceInterface getLayerSurface(int pLayerIndex)
  {
    return mEpitheliumLayerIsoSurfaceList.get(pLayerIndex);
  }

  /**
   * Assigns a given cell to a given layer.
   * 
   * @param pCellId
   *          cell id
   * @param pLayerId
   *          layer id
   */
  public void assignCellToLayer(int pCellId, float pLayerId)
  {
    float[] lCurrentArray =
                          mEpitheliumLayerCellForceProperty.getArray()
                                                           .getCurrentArray();
    lCurrentArray[pCellId] = pLayerId;
  }

  /**
   * Returns the layer to which a given cell belongs
   * 
   * @param pCellId
   *          cell id
   * @return layer id
   */
  public float getCellLayer(int pCellId)
  {
    float[] lCurrentArray =
                          mEpitheliumLayerCellForceProperty.getArray()
                                                           .getCurrentArray();

    return lCurrentArray[pCellId];
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      for (int layer = 0; layer < getNumberOfLayers(); layer++)
      {
        for (int id = 0; id < getNumberOfParticles(); id++)
        {
          float lLayer = getCellLayer(id);
          float lMask = layer == round(lLayer) ? 1 : 0;
          float lStrength = 1f - 2 * abs(lLayer - round(lLayer));
          mMask[id] = lMask * lStrength;
        }

        applyForceField(mEpitheliumLayerForceFieldList.get(layer),
                        0,
                        getNumberOfParticles(),
                        mMask);
      }

      applyForceField(mCohesionForceField);

      super.simulationSteps(1);
    }
  }

}
