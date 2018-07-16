package simbryo.synthoscopy.interfaces;

/**
 * Light wavelength interface
 *
 * @author royer
 */
public interface LightWavelengthInterface
{

  /**
   * Returns wavelength used for calculations. Normalized units (within [0,1])
   * are used.
   * 
   * @return returns wavelength in normalizd coordinates.
   */
  float getLightWavelength();

  /**
   * Sets wavelength in normalized coordinates.
   * 
   * @param pLambda
   *          normalized coordinates
   */
  void setLightWavelength(float pLambda);

}
