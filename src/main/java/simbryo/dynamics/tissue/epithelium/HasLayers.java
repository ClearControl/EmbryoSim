package simbryo.dynamics.tissue.epithelium;

import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Tissues implementing this interface have multiple layers represented as an
 * iso-surface.
 *
 * @author royer
 */
public interface HasLayers
{

  /**
   * Adds an iso-surface layer.
   * 
   * @param pLayerIsoSurface
   *          layer iso-surface
   * @param pLayerForceIntensity
   *          layer force intensity
   */
  public void addLayer(IsoSurfaceInterface pLayerIsoSurface,
                       float pLayerForceIntensity);

  /**
   * Returns the number of layers
   * 
   * @return number of layers
   */
  int getNumberOfLayers();

  /**
   * Returns the iso-surface for a given layer index.
   * 
   * @param pLayerIndex
   *          layer index
   * @return iso-surface for layer
   */
  public IsoSurfaceInterface getLayerSurface(int pLayerIndex);

}
