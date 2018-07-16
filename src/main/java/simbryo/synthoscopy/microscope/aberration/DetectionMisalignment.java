package simbryo.synthoscopy.microscope.aberration;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;

import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 * Illumination misalignment
 *
 * @author royer
 */
public class DetectionMisalignment extends AberrationBase
                                   implements AberrationInterface
{
  private HashMap<MutablePair<ParameterInterface<Number>, Integer>, Number> mOffsetMap =
                                                                                       new HashMap<>();

  /**
   * Instanciates an illumination misalignment
   */
  public DetectionMisalignment()
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
    Number lNumber = pNumber;
    MutablePair<ParameterInterface<Number>, Integer> lKey =
                                                          MutablePair.of(pParameter,
                                                                         pIndex);
    Number lOffset = mOffsetMap.get(lKey);

    if (pParameter instanceof DetectionParameter)
    {
      if (lOffset == null)
      {
        float lOffsetConstant =
                              adjustOffsetAmountDetection(pParameter);

        lOffset = lOffsetConstant * rand(-1, 1);

        mOffsetMap.put(lKey, lOffset);
      }

      lNumber = lNumber.floatValue() + lOffset.floatValue();

      // System.out.format("offset: %s[%d] -> %g
      // \n",pParameter,pIndex,lOffset.floatValue());
    }
    else if (pParameter instanceof CameraParameter)
    {

      if (lOffset == null)
      {

        float lOffsetConstant = adjustOffsetAmountCamera(pParameter,
                                                         pIndex);

        lOffset = lOffsetConstant * rand(-1, 1);

        mOffsetMap.put(lKey, lOffset);
      }

      lNumber = lNumber.floatValue() + lOffset.floatValue();

      // System.out.format("offset: %s[%d] -> %g
      // \n",pParameter,pIndex,lOffset.floatValue());
    }

    return lNumber;
  }

  protected float adjustOffsetAmountDetection(ParameterInterface<Number> pParameter)
  {
    float lOffsetConstant = 0;
    switch ((DetectionParameter) pParameter)
    {
    case Intensity:
      break;
    case Wavelength:
      break;
    case Z:
      lOffsetConstant = 10f;
      break;
    default:
      break;

    }
    return lOffsetConstant;
  }

  protected float adjustOffsetAmountCamera(ParameterInterface<Number> pParameter,
                                           int pIndex)
  {
    float lPixelSize =
                     1.0f / getMicroscope().getCameraRenderer(pIndex)
                                           .getMaxWidth();
    float lOffsetConstant = 0;
    switch ((CameraParameter) pParameter)
    {
    case Exposure:
      break;
    case Magnification:
      break;
    case ROIHeight:
      break;
    case ROIOffsetX:
      break;
    case ROIOffsetY:
      break;
    case ROIWidth:
      break;
    case ROIXMax:
      break;
    case ROIXMin:
      break;
    case ShiftX:
      lOffsetConstant = 4 * lPixelSize;
      break;
    case ShiftY:
      lOffsetConstant = 4 * lPixelSize;
      break;
    default:
      break;
    }
    return lOffsetConstant;
  }

}
