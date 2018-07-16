package simbryo.synthoscopy.microscope.aberration;

import java.util.Random;

import simbryo.synthoscopy.microscope.MicroscopeSimulatorInterface;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 *
 *
 * @author royer
 */
public abstract class AberrationBase implements AberrationInterface
{

  private MicroscopeSimulatorInterface mMicroscope;

  protected Random mRandom = new Random();

  @Override
  public void setMicroscope(MicroscopeSimulatorInterface pMicroscope)
  {
    mMicroscope = pMicroscope;
  }

  @Override
  public MicroscopeSimulatorInterface getMicroscope()
  {
    return mMicroscope;
  }

  @Override
  public long getTimeStepIndex()
  {
    return getMicroscope().getTimeStepIndex();
  }

  @Override
  public abstract void simulationSteps(int pNumberOfSteps);

  @Override
  public abstract Number transform(ParameterInterface<Number> pParameter,
                                   int pIndex,
                                   Number pNumber);

  /**
   * Returns a pseudo-random number within [min,max]
   * 
   * @param pMin
   *          min
   * @param pMax
   *          max
   * @return random numner
   */
  public float rand(float pMin, float pMax)
  {
    return pMin + mRandom.nextFloat() * (pMax - pMin);

  }

}
