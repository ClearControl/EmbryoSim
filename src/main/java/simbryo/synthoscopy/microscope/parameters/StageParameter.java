package simbryo.synthoscopy.microscope.parameters;

/**
 * Microscope parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum StageParameter implements ParameterInterface<Number>
{
 StageX(0.0f, -2, 2),
 StageY(0.0f, -2, 2),
 StageZ(0.0f, -2, 2),
 StageRX(0.0f, -180, 180),
 StageRY(0.0f, -180, 180),
 StageRZ(0.0f, -180, 180);

  Number mDefaultValue, mMinValue, mMaxValue;

  private StageParameter(Number pDefaultValue,
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
