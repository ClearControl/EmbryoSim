package simbryo.dynamics.tissue.embryo;

import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Tissues implementing this interface have a 'surface' represented as an
 * iso-surface.
 *
 * @author royer
 */
public interface HasSurface
{

  /**
   * Returns the iso-surface
   * 
   * @return iso-surface
   */
  IsoSurfaceInterface getSurface();

  /**
   * Sets the surface represented as an iso-surface.
   * 
   * @param pSurface
   *          iso-surface
   */
  void setSurface(IsoSurfaceInterface pSurface);

}
