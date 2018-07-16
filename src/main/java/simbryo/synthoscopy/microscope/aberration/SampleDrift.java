package simbryo.synthoscopy.microscope.aberration;

import simbryo.synthoscopy.microscope.parameters.ParameterInterface;
import simbryo.synthoscopy.microscope.parameters.StageParameter;

/**
 * Sample drift
 *
 * @author royer
 */
public class SampleDrift extends AberrationBase
                         implements AberrationInterface
{
  private int mStageIndex;

  private volatile float mDriftConstant;
  private volatile float x, y, z;

  /**
   * Instanciates a sample drift
   */
  public SampleDrift()
  {
    this(0, 0.02f);
  }

  /**
   * Instanciates a sample drift given a drift constant
   * 
   * @param pDriftConstant
   *          drift constant
   */
  public SampleDrift(float pDriftConstant)
  {
    this(0, pDriftConstant);
  }

  /**
   * Instanciates a sample drift for a given stage and drift constant
   * 
   * @param pStageIndex
   *          stage index
   * @param pDriftConstant
   *          drift constant
   */
  public SampleDrift(int pStageIndex, float pDriftConstant)
  {
    super();
    mStageIndex = pStageIndex;
    mDriftConstant = pDriftConstant;
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      x += mDriftConstant * rand(-1, 1);
      y += mDriftConstant * rand(-1, 1);
      z += mDriftConstant * rand(-1, 1);
    }
    // System.out.format("x=%g, y=%g, z=%g \n", x, y, z);
  }

  @Override
  public Number transform(ParameterInterface<Number> pParameter,
                          int pIndex,
                          Number pNumber)
  {
    if (pIndex != mStageIndex
        && !(pParameter instanceof StageParameter))
      return pNumber;

    Number lNumber = pNumber;
    if (pParameter == StageParameter.StageX)
    {
      lNumber = lNumber.floatValue() + x;
    }
    else if (pParameter == StageParameter.StageY)
    {
      lNumber = lNumber.floatValue() + y;
    }
    else if (pParameter == StageParameter.StageZ)
    {
      lNumber = lNumber.floatValue() + z;
    }

    return lNumber;
  }

}
