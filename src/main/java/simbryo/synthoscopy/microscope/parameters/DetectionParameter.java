package simbryo.synthoscopy.microscope.parameters;

/**
 * Detection Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum DetectionParameter implements ParameterInterface<Number>
{
 Wavelength(0.0005f, 0.0001f, 0.01f),
 Intensity(0.95f, 0.0f, 1.0f),
 Z(0.0f, -1, 1);

  Number mDefaultValue, mMinValue, mMaxValue;

  private DetectionParameter(Number pDefaultValue,
                             Number pMinValue,
                             Number pMaxValue)
  {
    mDefaultValue = pDefaultValue;
    mMinValue = pMinValue;
    mMaxValue = pMaxValue;
  }

  @Override
  public Number getDefaultValue()
  {
    return mDefaultValue;
  }

  @Override
  public Number getMinValue()
  {
    return mMinValue;
  }

  @Override
  public Number getMaxValue()
  {
    return mMaxValue;
  }
}
