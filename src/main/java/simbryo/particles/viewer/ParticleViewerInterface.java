package simbryo.particles.viewer;

/**
 * Particle viewers implement the following common methods.
 *
 * @author royer
 */
public interface ParticleViewerInterface
{

  /**
   * Triggers an update of the view. Must be called after the particle system
   * has been updated.
   * 
   * @param pBlocking
   *          if true, the viewer waits for the previous rendering to finish.
   */
  void updateDisplay(boolean pBlocking);

  /**
   * Waits (blocking call) while window is showing.
   */
  void waitWhileShowing();

  /**
   * Returns true if the viewer is showing, false otherwise
   * 
   * @return true if showing.
   */
  boolean isShowing();

  /**
   * Sets whether the radii of the particles should be displayed.
   * 
   * @param pDisplayRadius
   *          true to display radii, false otherwise
   */
  void setDisplayRadius(boolean pDisplayRadius);

}
