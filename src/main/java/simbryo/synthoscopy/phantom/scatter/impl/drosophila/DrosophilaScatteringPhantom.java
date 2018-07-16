package simbryo.synthoscopy.phantom.scatter.impl.drosophila;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLProgram;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.dynamics.tissue.embryo.EmbryoDynamics;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.ScatteringPhantom;

/**
 * This renders Drosophila scattering phantom.
 *
 * @author royer
 */
public class DrosophilaScatteringPhantom extends ScatteringPhantom
{

  private float mLowEdge, mHighEdge;
  private float mScatteringYolk, mScatteringWhole;

  /**
   * Instanciates a Drosophila scattering phantom renderer.
   * 
   * @param pContext
   *          ClearCL context
   * @param pDrosophila
   *          drosophila embryo dynamics
   * @param pDrosophilaHistoneFluorescence
   *          drosophila histone florescence
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown if OpenCL kernels cannot be read.
   */
  public DrosophilaScatteringPhantom(ClearCLContext pContext,
                                     EmbryoDynamics pDrosophila,
                                     DrosophilaHistoneFluorescence pDrosophilaHistoneFluorescence,
                                     long... pStackDimensions) throws IOException
  {
    super(pContext, 16, pDrosophila, pStackDimensions);
    setNoiseOverSignalRatio(0.02f);
    setSignalIntensity(1f);

    float lNucleiRadius =
                        pDrosophilaHistoneFluorescence.getNucleiRadius();

    mLowEdge = 2 * lNucleiRadius;
    mHighEdge = 20 * lNucleiRadius;

    setScatteringWhole(1f);
    setScatteringYolk(4f);

    setupProgramAndKernel();
  }

  protected void setupProgramAndKernel() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(DrosophilaScatteringPhantom.class,
                       "kernel/Scattering.cl");

    TissueDynamicsInterface lTissue = getTissue();
    if (lTissue instanceof Drosophila)
    {
      Drosophila lDrosophila = (Drosophila) lTissue;
      lProgram.addDefine("ELLIPSOIDA", lDrosophila.getEllipsoidA());
      lProgram.addDefine("ELLIPSOIDB", lDrosophila.getEllipsoidB());
      lProgram.addDefine("ELLIPSOIDC", lDrosophila.getEllipsoidC());
      lProgram.addDefine("ELLIPSOIDR", lDrosophila.getEllipsoidR());
    }
    else
    {
      lProgram.addDefine("ELLIPSOIDA", new Float(0.5));
      lProgram.addDefine("ELLIPSOIDB", new Float(0.5));
      lProgram.addDefine("ELLIPSOIDC", new Float(0.5));
      lProgram.addDefine("ELLIPSOIDR", new Float(0.5));
    }
    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("scatterrender");

    mRenderKernel.setArgument("lowedge", mLowEdge);
    mRenderKernel.setArgument("highedge", mHighEdge);
    mRenderKernel.setArgument("noiseratio",
                              getNoiseOverSignalRatio());
    mRenderKernel.setArgument("scattering_whole",
                              getScatteringWhole());
    mRenderKernel.setArgument("scattering_yolk", getScatteringYolk());
    mRenderKernel.setArgument("noiseratio",
                              getNoiseOverSignalRatio());

    mRenderKernel.setArgument("image", mImage);
  }

  /**
   * Returns the scattering intensity for the yolk (region at the center of
   * embryo)
   * 
   * @return scattering intensity for Yolk
   */
  public float getScatteringYolk()
  {
    return mScatteringYolk;
  }

  /**
   * Returns the scattering intensity for the whole embryo (region at the center
   * of embryo)
   * 
   * @return scattering intensity for the whole embryo
   */
  public float getScatteringWhole()
  {
    return mScatteringWhole;
  }

  /**
   * Sets the scattering intensity for the Yolk.
   * 
   * @param pScatteringYolk
   *          scattering intensity for the Yolk.
   */
  public void setScatteringYolk(float pScatteringYolk)
  {
    mScatteringYolk = pScatteringYolk;
  }

  /**
   * Sets the scattering intensity for the whole embryo
   * 
   * @param pScatteringWhole
   *          scattering intensity for the whole embryo
   */
  public void setScatteringWhole(float pScatteringWhole)
  {
    mScatteringWhole = pScatteringWhole;
  }

}
