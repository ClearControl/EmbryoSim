package simbryo.dynamics.tissue.cellprop.operators.impl;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.CellProperty;
import simbryo.dynamics.tissue.cellprop.operators.OperatorBase;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.util.geom.GeometryUtils;

/**
 * Strogatz wave operator.
 * 
 * Inspired by the pulse-coupled oscillators from Mirollo and Strogatz (1990).
 * The morphogen integral part defines the current oscillator 'cycle index' the
 * fractional part is the phase within each cycle. All cells morphogens are
 * incremented all the time by a fixed value. Cells that transition from one
 * cycle to the next 'pull' their neighbors by an amount defined by the coupling
 * constant. This scheme can be used to implement mitotic waves.
 * 
 * 
 * http://epubs.siam.org/doi/abs/10.1137/0150098
 * 
 */
public class StrogatzWaveOperator extends OperatorBase<CellProperty>
{
  private static final long serialVersionUID = 1L;

  private float mCouplingConstant;

  private int[] mNeighboorsArray;

  private float mNeighborhoodRadiusDilationFactor;

  private float mIncrement;

  /**
   * Constructs a Strogatz wave operator with given increment, coupling constant
   * and neighborhood radius factor. The neighborhood radius dilation factor
   * defines a percentage to dilate the size of the neighborhood around each
   * cell. A good value is for example 10% (hence 0.1).
   * 
   * @param pIncrement
   *          increment
   * @param pCouplingConstant
   *          coupling constant
   * @param pNeighboorhoodRadiusDilationFactor
   *          neighborhood dilation factor
   */
  public StrogatzWaveOperator(float pIncrement,
                              float pCouplingConstant,
                              float pNeighboorhoodRadiusDilationFactor)
  {
    super();
    mIncrement = pIncrement;
    mCouplingConstant = pCouplingConstant;
    mNeighborhoodRadiusDilationFactor =
                                      pNeighboorhoodRadiusDilationFactor;
  }

  @Override
  public void apply(int pBeginId,
                    int pEndId,
                    TissueDynamics pEmbryo,
                    CellProperty... pCellProperty)
  {
    final int lDimension = pEmbryo.getDimension();
    final CellProperty lCellProperty = pCellProperty[0];

    final NeighborhoodGrid lNeighborhood =
                                         pEmbryo.getNeighborhoodGrid();
    final int lMaxNumberOfParticlesPerGridCell =
                                               lNeighborhood.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = lNeighborhood.getVolume();
    final float lIncrement = mIncrement;
    final float lCouplingConstant = mCouplingConstant;
    final float lNeighborhoodRadiusFactor =
                                          mNeighborhoodRadiusDilationFactor;

    final float[] lPositions =
                             pEmbryo.getPositions().getCurrentArray();
    final float[] lVelocities = pEmbryo.getVelocities()
                                       .getCurrentArray();
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

    final float[] lCellPropertyArrayRead =
                                         lCellProperty.getArray()
                                                      .getReadArray();
    final float[] lCellPropertyArrayWrite =
                                          lCellProperty.getArray()
                                                       .getWriteArray();

    for (int idu = pBeginId; idu < pEndId; idu++)
    {
      final float ru = lRadii[idu];

      float lOldValue = lCellPropertyArrayRead[idu];

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

      float lNewValue = lOldValue + lIncrement
                        + detectNeighboringEvent(lDimension,
                                                 lNeighborhood,
                                                 lCouplingConstant,
                                                 lNeighborhoodRadiusFactor,
                                                 lPositions,
                                                 lVelocities,
                                                 lRadii,
                                                 lCellPropertyArrayRead,
                                                 lNeighboors,
                                                 lNumberOfNeighboors,
                                                 idu);

      boolean lEvent = (int) lNewValue > (int) lOldValue;

      lNewValue = (lEvent ? (int) lNewValue : lNewValue);

      lCellPropertyArrayWrite[idu] = eventHook(lEvent,
                                               idu,
                                               lPositions,
                                               lVelocities,
                                               lRadii,
                                               lNewValue);

    }

    lCellProperty.getArray().swap();

  }

  /*
   * 
   */
  private float detectNeighboringEvent(int pDimension,
                                       NeighborhoodGrid pNeighborhood,
                                       final float pCouplingConstant,
                                       final float pNeighborhoodRadiusFactor,
                                       final float[] pPositions,
                                       final float[] pVelocities,
                                       final float[] pRadii,
                                       final float[] pMorphogenArrayRead,
                                       final int[] pNeighboors,
                                       final int pNumberOfNeighboors,
                                       int idu)
  {
    final float ru = pRadii[idu];

    boolean lAtLeastOneNeighboorDivided = false;

    for (int k = 0; k < pNumberOfNeighboors; k++)
    {
      final int idv = pNeighboors[k];

      final float rv = pRadii[idv];

      if (idu != idv) //
      {
        float lDistance = GeometryUtils.computeDistance(pDimension,
                                                        pPositions,
                                                        idu,
                                                        idv);
        float lGap = lDistance - (1 + pNeighborhoodRadiusFactor) * ru
                     - (1 + pNeighborhoodRadiusFactor) * rv;

        boolean lBoundaryPassed =
                                ((int) pMorphogenArrayRead[idv]) > ((int) pMorphogenArrayRead[idu]);

        lAtLeastOneNeighboorDivided |= (lGap < 0 && lBoundaryPassed);
      }
    }

    if (lAtLeastOneNeighboorDivided)
    {
      return pCouplingConstant;
    }
    else
      return 0;
  }

  protected float eventHook(boolean pEvent,
                            int pId,
                            float[] pPositions,
                            float[] pVelocities,
                            float[] pRadii,
                            float pNewMorphogenValue)
  {
    return pEvent ? (int) pNewMorphogenValue : pNewMorphogenValue;
  }

}
