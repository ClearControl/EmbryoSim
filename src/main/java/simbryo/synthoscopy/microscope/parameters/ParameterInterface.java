package simbryo.synthoscopy.microscope.parameters;

/**
 * Interface for all micorscope simulation parameters
 *
 * @author royer
 * @param <T>
 *          parameter value type
 */
public interface ParameterInterface<T>
{

  /**
   * Returns the parameter default value
   * 
   * @return default value
   */
  T getDefaultValue();

  /**
   * Returns the parameter default value
   * 
   * @return default value
   */
  T getMinValue();

  /**
   * Returns the parameter default value
   * 
   * @return default value
   */
  T getMaxValue();

}
