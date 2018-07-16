package simbryo.synthoscopy.microscope.aberration;

import simbryo.SimulationInterface;
import simbryo.synthoscopy.microscope.MicroscopeSimulatorInterface;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 * Interface imlemented by all kinds of abberations such as sample drift,
 * refraction and other misalignements and miscalibrations.
 *
 * @author royer
 */
public interface AberrationInterface extends SimulationInterface
{

  /**
   * Sets the microscope parent
   * 
   * @param pMicroscope
   */
  void setMicroscope(MicroscopeSimulatorInterface pMicroscope);

  /**
   * Returns the microscope parent to whom this abberation 'belongs'
   * 
   * @return microscope
   */
  MicroscopeSimulatorInterface getMicroscope();

  /**
   * @param pParameter
   * @param pIndex
   * @param pNumber
   * @return transformed number
   */
  Number transform(ParameterInterface<Number> pParameter,
                   int pIndex,
                   Number pNumber);

}
