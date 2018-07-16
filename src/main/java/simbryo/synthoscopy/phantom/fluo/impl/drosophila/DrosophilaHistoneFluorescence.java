package simbryo.synthoscopy.phantom.fluo.impl.drosophila;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLProgram;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.dynamics.tissue.embryo.EmbryoDynamics;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.fluo.HistoneFluorescence;

/**
 * This renders Drosophila histone fluorescence (nuclei + yolk).
 *
 * @author royer
 */
public class DrosophilaHistoneFluorescence extends HistoneFluorescence
{

  /**
   * Instanciates a Drosophila embryo histone fluorescence renderer.
   * 
   * @param pContext
   *          ClearCL context
   * @param pDrosophila
   *          drosophila embryo dynamics
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown if OpenCL kernels cannot be read.
   */
  public DrosophilaHistoneFluorescence(ClearCLContext pContext,
                                       EmbryoDynamics pDrosophila,
                                       long... pStackDimensions) throws IOException
  {
    super(pContext, pDrosophila, pStackDimensions);
  }

  @Override
  public void addAutoFluoFunctionSourceCode(ClearCLProgram pClearCLProgram) throws IOException
  {
    pClearCLProgram.addSource(DrosophilaHistoneFluorescence.class,
                              "kernel/AutoFluo.cl");

    TissueDynamicsInterface lDynamics = getTissue();

    if (lDynamics instanceof Drosophila)
    {
      Drosophila lDrosophila = (Drosophila) getTissue();
      pClearCLProgram.addDefine("ELLIPSOIDA",
                                lDrosophila.getEllipsoidA());
      pClearCLProgram.addDefine("ELLIPSOIDB",
                                lDrosophila.getEllipsoidB());
      pClearCLProgram.addDefine("ELLIPSOIDC",
                                lDrosophila.getEllipsoidC());
      pClearCLProgram.addDefine("ELLIPSOIDR",
                                lDrosophila.getEllipsoidR());
    }
    else
    {
      pClearCLProgram.addDefine("ELLIPSOIDA", new Float(0.5));
      pClearCLProgram.addDefine("ELLIPSOIDB", new Float(0.5));
      pClearCLProgram.addDefine("ELLIPSOIDC", new Float(0.5));
      pClearCLProgram.addDefine("ELLIPSOIDR", new Float(0.5));

    }

  }

}
