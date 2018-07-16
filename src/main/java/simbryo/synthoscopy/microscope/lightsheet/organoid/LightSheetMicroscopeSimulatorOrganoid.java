package simbryo.synthoscopy.microscope.lightsheet.organoid;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Organoid;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

public class LightSheetMicroscopeSimulatorOrganoid extends
                                                   LightSheetMicroscopeSimulatorOrtho
{

  private Organoid mOrganoid;
  private DrosophilaHistoneFluorescence mDrosophilaFluorescencePhantom;
  private DrosophilaScatteringPhantom mDrosophilaScatteringPhantom;

  private volatile boolean mFreezedEmbryo = false;

  /**
   * Instanciates a light sheet microscope simulator with a simulated drosophila
   * embryo as sample
   *
   * @param pContext
   *          ClearCL context to use
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfIlluminationArms
   *          number of illumination arms
   * @param pMaxCameraResolution
   *          max width and height of camera images
   * @param pInitialDivisionTime
   *          initial 'division time' for embryo
   * @param pMainPhantomDimensions
   *          main phantom dimensions
   */
  public LightSheetMicroscopeSimulatorOrganoid(ClearCLContext pContext,
                                               int pNumberOfDetectionArms,
                                               int pNumberOfIlluminationArms,
                                               int pMaxCameraResolution,
                                               float pInitialDivisionTime,
                                               long... pMainPhantomDimensions)
  {
    super(pContext,
          pNumberOfDetectionArms,
          pNumberOfIlluminationArms,
          pMaxCameraResolution,
          pMainPhantomDimensions);

    int[] dimensions = new int[pMainPhantomDimensions.length];
    for (int i = 0; i < dimensions.length; i++)
    {
      dimensions[i] = (int) pMainPhantomDimensions[i];
    }

    try
    {
      mOrganoid = new Organoid(dimensions);
      mOrganoid.simulationSteps((int) pInitialDivisionTime);
      // Drosophila.getDeveloppedEmbryo(pInitialDivisionTime);

      mDrosophilaFluorescencePhantom =
                                     new DrosophilaHistoneFluorescence(pContext,
                                                                       mOrganoid,
                                                                       getWidth(),
                                                                       getHeight(),
                                                                       getDepth());
      mDrosophilaFluorescencePhantom.render(true);

      mDrosophilaScatteringPhantom =
                                   new DrosophilaScatteringPhantom(pContext,
                                                                   mOrganoid,
                                                                   mDrosophilaFluorescencePhantom,
                                                                   getWidth() / 2,
                                                                   getHeight() / 2,
                                                                   getDepth() / 2);

      mDrosophilaScatteringPhantom.render(true);

      setPhantomParameter(PhantomParameter.Fluorescence,
                          mDrosophilaFluorescencePhantom.getImage());
      setPhantomParameter(PhantomParameter.Scattering,
                          mDrosophilaScatteringPhantom.getImage());
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new RuntimeException("Problem while initializing phantoms",
                                 e);
    }

  }

  /**
   * Opens viewer for fluorescence phantom image.
   *
   * @return viewer
   */
  public ClearCLImageViewer openViewerForFluorescencePhantom()
  {
    return mDrosophilaFluorescencePhantom.openViewer();
  }

  /**
   * Opens viewer for scattering phantom image.
   *
   * @return viewer
   */
  public ClearCLImageViewer openViewerForScatteringPhantom()
  {
    return mDrosophilaScatteringPhantom.openViewer();
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    if (!isFreezedEmbryo())
      mOrganoid.simulationSteps(pNumberOfSteps);
    super.simulationSteps(pNumberOfSteps);
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    mDrosophilaFluorescencePhantom.render(false);
    mDrosophilaScatteringPhantom.render(false);
    super.render(pWaitToFinish);
  }

  @Override
  public void close() throws Exception
  {
    super.close();
    mDrosophilaScatteringPhantom.close();
    mDrosophilaFluorescencePhantom.close();
  }

  /**
   * Returns wether this embryo is 'frozen' - meaning that it is prevented from
   * developping.
   *
   * @return true -> frozen, false -> otherwise
   */
  public boolean isFreezedEmbryo()
  {
    return mFreezedEmbryo;
  }

  /**
   * Sets whther this embryo should be 'frozen' - prevented from developping.
   *
   * @param pFreezeEmbryo
   *          true -> frozen, false -> otherwise
   */
  public void setFreezedEmbryo(boolean pFreezeEmbryo)
  {
    mFreezedEmbryo = pFreezeEmbryo;
  }

}
