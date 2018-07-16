package simbryo.dynamics.tissue.cellprop.operators.impl;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.VectorCellProperty;
import simbryo.dynamics.tissue.cellprop.operators.OperatorBase;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.util.geom.GeometryUtils;

/**
 * Nematic order operator.
 * 
 * This operator causes the polarity vectors of neighboring particles to
 * self-align.
 * 
 */
public class NematicOrderOperator extends
                                  OperatorBase<VectorCellProperty>
{
  private static final long serialVersionUID = 1L;

  private float mCouplingConstant;
  private int[] mNeighboorsArray;
  private float mNeighborhoodRadiusDilationFactor;

  /**
   * Constructs a Nematic order operator with coupling constant and neighborhood
   * radius factor. The neighborhood radius dilation factor defines a percentage
   * to dilate the size of the neighborhood around each cell. A good value is
   * for example 10% (hence 0.1).
   * 
   * @param pCouplingConstant
   *          coupling constant
   * @param pNeighboorhoodRadiusDilationFactor
   *          neighborhood radius dilation factor.
   */
  public NematicOrderOperator(float pCouplingConstant,
                              float pNeighboorhoodRadiusDilationFactor)
  {
    super();

    mCouplingConstant = pCouplingConstant;
    mNeighborhoodRadiusDilationFactor =
                                      pNeighboorhoodRadiusDilationFactor;
  }

  @Override
  public void apply(int pBeginId,
                    int pEndId,
                    TissueDynamics pEmbryo,
                    VectorCellProperty... pVectorCellProperty)
  {
    final int lDimension = pEmbryo.getDimension();
    final VectorCellProperty lVectorCellProperty =
                                                 pVectorCellProperty[0];

    final NeighborhoodGrid lNeighborhood =
                                         pEmbryo.getNeighborhoodGrid();
    final int lMaxNumberOfParticlesPerGridCell =
                                               lNeighborhood.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = lNeighborhood.getVolume();
    final float lCouplingConstant = mCouplingConstant;
    final float lNeighborhoodRadiusFactor =
                                          mNeighborhoodRadiusDilationFactor;

    final float[] lPositions =
                             pEmbryo.getPositions().getCurrentArray();
    final float[] lRadii = pEmbryo.getRadii().getCurrentArray();

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

    lVectorCellProperty.copyDefault(pBeginId, pEndId);

    for (int idu = pBeginId; idu < pEndId; idu++)
    {
      final float ru = lRadii[idu];

      int lNumberOfNeighboors =
                              lNeighborhood.getAllNeighborsForParticle(lNeighboors,
                                                                       lNeighboorsTemp,
                                                                       lPositions,
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

        float lDistance = GeometryUtils.computeDistance(lDimension,
                                                        lPositions,
                                                        idu,
                                                        idv);
        float lGap = lDistance - (1 + lNeighborhoodRadiusFactor) * ru
                     - (1 + lNeighborhoodRadiusFactor) * rv;

        if (lGap < 0)
        {
          lVectorCellProperty.addVector(idu, idv, lCouplingConstant);
        }
      }
    }

    lVectorCellProperty.normalize(pBeginId, pEndId);
    lVectorCellProperty.getArray().swap();

  }

}
