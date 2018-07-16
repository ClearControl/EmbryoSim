package simbryo;

/**
 * Simulation interface
 *
 * @author royer
 */
public interface SimulationInterface
{

  /**
   * Returns the current time step index.
   * 
   * @return time step
   */
  long getTimeStepIndex();

  /**
   * Runs a given number of simulation steps, each separated by a given time
   * interval
   * 
   * @param pNumberOfSteps
   *          number of simulation steps to run
   */
  void simulationSteps(int pNumberOfSteps);

}
