package simbryo.particles.viewer.three;

import javafx.scene.paint.Color;

/**
 * Functional interface that returns a color a for a given particle id.
 *
 * @author royer
 */
public interface ColorClosureInterface
{
  /**
   * Returns a color a for a given particle id.
   * 
   * @param pParticleId
   *          particle id
   * @return color
   */
  Color getColor(int pParticleId);
}
