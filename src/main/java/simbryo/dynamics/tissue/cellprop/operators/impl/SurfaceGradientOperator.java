package simbryo.dynamics.tissue.cellprop.operators.impl;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.VectorCellProperty;
import simbryo.dynamics.tissue.cellprop.operators.OperatorBase;
import simbryo.dynamics.tissue.embryo.HasSurface;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Surface gradient operator.
 * 
 * This operator sets a vector property to the normalized gradient of an
 * iso-surface.
 * 
 */
public class SurfaceGradientOperator extends
                                     OperatorBase<VectorCellProperty>
{

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a Surface gradient operator.
   * 
   */
  public SurfaceGradientOperator()
  {
    super();

  }

  @Override
  public void apply(int pBeginId,
                    int pEndId,
                    TissueDynamics pTissueDynamics,
                    VectorCellProperty... pVectorCellProperty)
  {
    if (!(pTissueDynamics instanceof HasSurface))
      throw new IllegalArgumentException("tissue dynamics should implement "
                                         + HasSurface.class.getSimpleName()
                                         + " interface");

    HasSurface lHasSurface = (HasSurface) pTissueDynamics;
    IsoSurfaceInterface lSurface = lHasSurface.getSurface();

    final int lDimension = pTissueDynamics.getDimension();
    final VectorCellProperty lVectorCellProperty =
                                                 pVectorCellProperty[0];
    float[] lPropertyArray = lVectorCellProperty.getArray()
                                                .getCurrentArray();

    final float[] lPositions = pTissueDynamics.getPositions()
                                              .getCurrentArray();

    for (int idu = pBeginId; idu < pEndId; idu++)
    {
      int lIndex = lDimension * idu;
      lSurface.clear();
      for (int d = 0; d < lDimension; d++)
      {
        float lValue = lPositions[lIndex + d];
        lSurface.addCoordinate(lValue);
      }

      for (int d = 0; d < lDimension; d++)
      {
        float lValue = lSurface.getNormalizedGardient(d);
        lPropertyArray[lIndex + d] = lValue;
      }
    }
  }

}
