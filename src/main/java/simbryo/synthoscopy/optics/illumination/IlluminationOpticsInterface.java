package simbryo.synthoscopy.optics.illumination;

import simbryo.synthoscopy.SynthoscopyInterface;
import simbryo.synthoscopy.interfaces.LightIntensityInterface;
import simbryo.synthoscopy.interfaces.LightWavelengthInterface;

/**
 * Illumination optics interface
 *
 * @param <I>
 *          type of images used to store and process illumination-side images
 * @author royer
 */
public interface IlluminationOpticsInterface<I> extends
                                            SynthoscopyInterface<I>,
                                            LightIntensityInterface,
                                            LightWavelengthInterface
{

}
