package simbryo.synthoscopy.microscope.lightsheet.drosophila.demo;

import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;
import simbryo.synthoscopy.microscope.lightsheet.drosophila.LightSheetMicroscopeSimulatorDrosophila;

/**
 * Demo for lightsheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorDrosophilaDemo
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
  public void demo1D1I() throws IOException, InterruptedException
  {
    test(1, 1);
  }

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo1D2I() throws IOException, InterruptedException
  {
    test(1, 2);
  }

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo2D1I() throws IOException, InterruptedException
  {
    test(2, 1);
  }

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo2D2I() throws IOException, InterruptedException
  {
    test(2, 2);
  }

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo2D4I() throws IOException, InterruptedException
  {
    test(2, 4);
  }

  private void test(int pNumberOfDetectionArms,
                    int pNumberOfIlluminationArms)
  {
    try
    {

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {
        System.out.println(lFastestGPUDevice);

        LightSheetMicroscopeSimulatorDrosophila lSimulator =
                                                           new LightSheetMicroscopeSimulatorDrosophila(lContext,
                                                                                                       pNumberOfDetectionArms,
                                                                                                       pNumberOfIlluminationArms,
                                                                                                       1024,
                                                                                                       11f,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        // lSimulator.setNumberParameter(UnitConversion.Length, 0, 100f);
        lSimulator.openViewerForControls();

        lSimulator.openViewerForFluorescencePhantom();
        lSimulator.openViewerForScatteringPhantom();

        ClearCLImageViewer lCameraImageViewer =
                                              lSimulator.openViewerForCameraImage(0);
        for (int i = 1; i < pNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        for (int i = 0; i < pNumberOfIlluminationArms; i++)
          lSimulator.openViewerForLightMap(i);

        while (lCameraImageViewer.isShowing())
        {
          // lSimulator.simulationSteps(100, 1);
          lSimulator.render(true);
        }

        lSimulator.close();

      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

}
