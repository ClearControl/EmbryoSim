package simbryo.dynamics.tissue.cellprop.operators;

import simbryo.dynamics.tissue.cellprop.CellProperty;

/**
 * This base class implements common fields and methods required by all cell
 * property operators.
 *
 * @author royer
 * @param <CP>
 *          cell property type
 */
public abstract class OperatorBase<CP extends CellProperty> implements
                                  CellPropertyOperatorInterface<CP>
{
  private static final long serialVersionUID = 1L;
  // nothing yet
}
