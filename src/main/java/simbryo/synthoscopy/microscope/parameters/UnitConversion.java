package simbryo.synthoscopy.microscope.parameters;

/**
 * Unit conversion factors parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum UnitConversion implements ParameterInterface<Number>
{
 Length(1f, 0, 1000f), LaserIntensity(1f, 0, 100f);

  Number mDefaultValue, mMinValue, mMaxValue;

  private UnitConversion(Number pDefaultValue,
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
