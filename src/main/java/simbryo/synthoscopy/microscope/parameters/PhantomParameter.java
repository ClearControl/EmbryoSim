package simbryo.synthoscopy.microscope.parameters;

/**
 * Phantom Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum PhantomParameter implements ParameterInterface<Void>
{
 Fluorescence(null),
 Scattering(null),
 Absorption(null),
 Refraction(null);

  Number mDefaultValue;

  private PhantomParameter(Number pDefaultValue)
  {
    mDefaultValue = pDefaultValue;
  }

  @Override
  public Void getDefaultValue()
  {
    throw new IllegalArgumentException("No default value for phantoms");
  }

  @Override
  public Void getMinValue()
  {
    throw new IllegalArgumentException("No default value for phantoms");
  }

  @Override
  public Void getMaxValue()
  {
    throw new IllegalArgumentException("No default value for phantoms");
  }
}
