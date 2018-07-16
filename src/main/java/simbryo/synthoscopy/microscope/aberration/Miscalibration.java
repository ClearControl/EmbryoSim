package simbryo.synthoscopy.microscope.aberration;

import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;
import simbryo.synthoscopy.microscope.parameters.UnitConversion;

/**
 * Miscalibration
 *
 * @author royer
 */
public class Miscalibration extends AberrationBase
                            implements AberrationInterface
{

  /**
   * Instanciates a miscalibration
   */
  public Miscalibration()
  {
    super();
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {

  }

  @Override
  public Number transform(ParameterInterface<Number> pParameter,
                          int pIndex,
                          Number pNumber)
  {

    if (pParameter instanceof IlluminationParameter)
    {
      double lValue = pNumber.doubleValue();
      switch ((IlluminationParameter) pParameter)
      {
      case Y:
      case Z:
        lValue = lValue * 0.9;
        break;
      case Height:
        lValue = lValue
                 * UnitConversion.Length.getMaxValue().doubleValue();
        break;
      default:
        break;
      }
      return new Double(lValue);
    }

    if (pParameter instanceof DetectionParameter)
    {
      double lValue = pNumber.doubleValue();
      switch ((DetectionParameter) pParameter)
      {
      case Z:
        lValue = (lValue - 0.05f) * 1.07;
        break;
      default:
        break;
      }
      return new Double(lValue);
    }

    return pNumber;
  }

}
