package simbryo.dynamics.tissue.cellprop;

import static java.lang.Math.sqrt;

import simbryo.dynamics.tissue.TissueDynamics;

/**
 * Vector cell property. Multi-dimensional cell property with vector operations.
 *
 * @author royer
 */
public class VectorCellProperty extends CellProperty
{
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a vector cell property for given tissue dynamics and
   * dimension.
   * 
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pDimension
   *          dimension
   */
  public VectorCellProperty(TissueDynamics pTissueDynamics,
                            int pDimension)
  {
    super(pTissueDynamics, pDimension);
  }

  /**
   * Initializes with random vectors.
   */
  public void initializeRandom()
  {
    float[] lMorphogenArrayRead = mPropertyArray.getReadArray();
    float[] lMorphogenArrayWrite = mPropertyArray.getWriteArray();

    for (int i = 0; i < lMorphogenArrayRead.length; i++)
    {
      lMorphogenArrayRead[i] = (float) (Math.random() * 2 - 1);
      lMorphogenArrayWrite[i] = (float) (Math.random() * 2 - 1);
    }
  }

  /**
   * Adds the vector from a given particle to the vector of another particle
   * after multiplying with a factor.
   * 
   * @param pDestId
   *          destination particle vector id
   * @param pSourceId
   *          source particle vector id
   * @param pFactor
   *          factor to multiply vector with
   */
  public void addVector(int pDestId, int pSourceId, float pFactor)
  {
    int lDimension = getDimension();

    float[] lMorphogenArrayRead = mPropertyArray.getReadArray();
    float[] lMorphogenArrayWrite = mPropertyArray.getWriteArray();

    for (int i = 0; i < lDimension; i++)
      lMorphogenArrayWrite[pDestId * lDimension
                           + i] =
                                pFactor
                                  * lMorphogenArrayRead[pSourceId
                                                        * lDimension
                                                        + i];

  }

  /**
   * Normalizes all vectors
   */
  public void normalize()
  {
    normalize(0, getMaxNumberOfParticles());
  }

  /**
   * Normalizes vectors for particles within a given range.
   * 
   * @param pBeginId
   *          begin id
   * @param pEndId
   *          end id
   */
  public void normalize(int pBeginId, int pEndId)
  {
    int lDimension = getDimension();
    float[] lMorphogenArrayWrite = mPropertyArray.getWriteArray();

    for (int id = pBeginId; id < pEndId; id++)
    {
      int j = id * lDimension;
      float lNorm = 0;
      for (int i = 0; i < lDimension; i++)
      {
        float lValue = lMorphogenArrayWrite[j + i];
        lNorm += lValue * lValue;
      }
      lNorm = (float) sqrt(lNorm);

      if (lNorm > 0.0f || lNorm < 0.0f)
      {
        float lInvNorm = 1.0f / lNorm;

        for (int i = 0; i < lDimension; i++)
          lMorphogenArrayWrite[j + i] *= lInvNorm;
      }
    }

  }

}
