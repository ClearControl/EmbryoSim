package simbryo.synthoscopy.microscope.parameters;

/**
 * Illumination Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum IlluminationParameter implements
                                  ParameterInterface<Number>
{
 Wavelength(0.0005f, 0.0001, 0.01f),
 Intensity(10.0f, 0, 100),
 X(0.0f, -1, 1),
 Y(0.0f, -1, 1),
 Z(0.0f, -1, 1),
 Height(0.5f, 0, 1),
 Alpha(0.0f, -45, 45),
 Beta(0.0f, -45, 45),
 Gamma(0.0f, -45, 45),
 Theta(3.0f, 0, 20);

  Number mDefaultValue, mMinValue, mMaxValue;

  private IlluminationParameter(Number pDefaultValue,
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
