package simbryo.dynamics.tissue.cellprop;

/**
 * Tissues that implement this interface have a polarity cell property
 *
 * @author royer
 */
public interface HasPolarity
{

  /**
   * Returns the polarity cell property.
   * 
   * @return polarity property
   */
  VectorCellProperty getPolarityProperty();

}
