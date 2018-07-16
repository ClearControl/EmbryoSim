package simbryo.synthoscopy.optics.illumination.impl.lightsheet.demo;

import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.synthoscopy.phantom.PhantomRendererUtils;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class LightSheetIlluminationDemo
{

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo() throws IOException, InterruptedException
  {

    try
    {
      int lPhantomWidth = 512;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        int[] lGridDimensions =
                              PhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                            lPhantomWidth,
                                                                            lPhantomHeight,
                                                                            lPhantomDepth);

        Drosophila lDrosophila = new Drosophila(16, lGridDimensions);
        // lDrosophila.open3DViewer();
        lDrosophila.simulationSteps(1000);

        DrosophilaHistoneFluorescence lDrosoFluo =
                                                 new DrosophilaHistoneFluorescence(lContext,
                                                                                   lDrosophila,
                                                                                   lPhantomWidth,
                                                                                   lPhantomHeight,
                                                                                   lPhantomDepth);
        lDrosoFluo.render(true);

        LightSheetIllumination lLightSheetIllumination =
                                                       new LightSheetIllumination(lContext,
                                                                                  lPhantomWidth
                                                                                            / 4,
                                                                                  lPhantomHeight / 4,
                                                                                  31L);

        lLightSheetIllumination.setScatteringPhantom(lDrosoFluo.getImage());
        lLightSheetIllumination.setLightSheetHeigth(0.5f);
        lLightSheetIllumination.setLightSheetPosition(0.5f,
                                                      0.5f,
                                                      0.5f);
        lLightSheetIllumination.setOrientationWithAnglesInDegrees(0,
                                                                  0,
                                                                  0);
        lLightSheetIllumination.setLightSheetThetaInDeg(3.0f);

        // lDrosoFluo.getPhantomImage().fillZero(true);

        ElapsedTime.measure("renderlightsheet",
                            () -> lLightSheetIllumination.render(true));

        ClearCLImageViewer lOpenViewer =
                                       lLightSheetIllumination.openViewer();
        Thread.sleep(500);

        while (lOpenViewer.isShowing())
        {
          Thread.sleep(10);
        }

        lLightSheetIllumination.close();
        lDrosoFluo.close();

      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

}
