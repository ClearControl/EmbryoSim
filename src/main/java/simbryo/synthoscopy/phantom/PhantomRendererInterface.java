package simbryo.synthoscopy.phantom;

import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.synthoscopy.SynthoscopyInterface;

/**
 * Phantom Renderers implement this interface
 *
 * @param <I>
 *          type of image used to store phantom
 * @author royer
 */
public interface PhantomRendererInterface<I> extends
                                         SynthoscopyInterface<I>
{

  /**
   * Return phantom signal intensity
   * 
   * @return signal intensity
   */
  float getSignalIntensity();

  /**
   * Sets the phantom signal intensity
   * 
   * @param pSignalIntensity
   */
  void setSignalIntensity(float pSignalIntensity);

  /**
   * Return phantom noise over signal intensity ratio.
   * 
   * @return noise over signal ratio
   */
  float getNoiseOverSignalRatio();

  /**
   * Sets the phantom noise intensity
   * 
   * @param pNoiseIntensity
   */
  void setNoiseOverSignalRatio(float pNoiseIntensity);

  /**
   * Returns begin of z rendering range
   * 
   * @return zbegin
   */
  int getBeginZ();

  /**
   * Sets begin of z rendering range
   * 
   * @param pStartZ
   */
  void setBeginZ(int pStartZ);

  /**
   * Returns end of z rendering range
   * 
   * @return zend
   */
  int getEndZ();

  /**
   * Sets end of z rendering range
   * 
   * @param pEndZ
   */
  void setEndZ(int pEndZ);

  /**
   * Returns the tissue dynamics for this renderer.
   * 
   * @return tissue dynamics
   */
  TissueDynamicsInterface getTissue();

  /**
   * Renders a range of z plane indices. This method is cache-aware, it will not
   * re-render already rendered planes. A call to clear() is needed to force a
   * re-render.
   * 
   * @param pZPlaneIndexBegin
   *          begin of z plane index range
   * @param pZPlaneIndexEnd
   *          end of z plane index range
   * @param pWaitToFinish
   *          true -> wait to finish
   */
  void render(int pZPlaneIndexBegin,
              int pZPlaneIndexEnd,
              boolean pWaitToFinish);

}
