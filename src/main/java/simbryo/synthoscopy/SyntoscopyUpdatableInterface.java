package simbryo.synthoscopy;

/**
 * Interface for updatable syntoscopy modules
 *
 * @author royer
 */
public interface SyntoscopyUpdatableInterface
{

  /**
   * Requests update, will cause the image to b re-rendered at next call to
   * render()
   */
  void requestUpdate();

  /**
   * Clears update, update not needed anymore because the image as been
   * re-rendered
   */
  void clearUpdate();

  /**
   * Returns true if parameters have changed and an update is needed.
   * 
   * @return true if update needed
   */
  boolean isUpdateNeeded();

  /**
   * Adds an update listener. Update notifications will be forwarded to the
   * given listener
   * 
   * @param pUpdateListener
   *          update listener to forward notifications to
   */
  void addUpdateListener(SyntoscopyUpdatableInterface pUpdateListener);

}
