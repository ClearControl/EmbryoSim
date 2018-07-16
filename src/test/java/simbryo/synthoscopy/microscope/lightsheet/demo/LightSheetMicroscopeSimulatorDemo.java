package simbryo.synthoscopy.microscope.lightsheet.demo;

import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Demo for lightsheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorDemo
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
  public void demo1D4I() throws IOException, InterruptedException
  {
    test(1, 4);
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

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getDeviceByName("HD");
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(11);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        lDrosophilaFluorescencePhantom.render(true);

        // @SuppressWarnings("unused")

        /*ClearCLImageViewer lFluoPhantomViewer = lDrosophilaFluorescencePhantom.openViewer();/**/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*ClearCLImageViewer lScatterPhantomViewer =
                                                 lDrosophilaScatteringPhantom.openViewer();/**/

        LightSheetMicroscopeSimulatorOrtho lSimulator =
                                                      new LightSheetMicroscopeSimulatorOrtho(lContext,
                                                                                             pNumberOfDetectionArms,
                                                                                             pNumberOfIlluminationArms,
                                                                                             lMaxCameraResolution,
                                                                                             lPhantomWidth,
                                                                                             lPhantomHeight,
                                                                                             lPhantomDepth);

        lSimulator.setPhantomParameter(PhantomParameter.Fluorescence,
                                       lDrosophilaFluorescencePhantom.getImage());
        lSimulator.setPhantomParameter(PhantomParameter.Scattering,
                                       lDrosophilaScatteringPhantom.getImage());

        lSimulator.openViewerForControls();

        ClearCLImageViewer lCameraImageViewer =
                                              lCameraImageViewer =
                                                                 lSimulator.openViewerForCameraImage(0);
        // ClearCLImage lImage = lSimulator.getCameraImage(0);

        // System.out.println("Sum intensity was: " + sumImage(lImage));
        lSimulator.render(true);
        // System.out.println("After rendering, sum intensity is: "
        // + sumImage(lImage));

        for (int i = 1; i < pNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        for (int i = 0; i < pNumberOfIlluminationArms; i++)
          lSimulator.openViewerForLightMap(i);

        // float y = 0;

        while (lCameraImageViewer.isShowing())
        {
          /*lSimulator.setNumberParameter(IlluminationParameter.Height,
                                        0,
                                        0.01f);
          lSimulator.setNumberParameter(IlluminationParameter.Y,
                                        0,
                                        y);
          
          y += 0.01f;
          if (y > 1)
            y = 0;
            /**/
          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }
  /*
  private long sumImage(ClearCLImage lImage)
  {
    long
        size =
        lImage.getSizeInBytes();// * lImage.getWidth() * lImage.getHeight();
  
    OffHeapMemory
        lTransferMemory =
        OffHeapMemory.allocateBytes("temp image", size);
  
    lImage.writeTo(lTransferMemory,
                   new long[] { 0L, 0L, 0L },
                   new long[] { (long) lImage.getWidth(),
                                (long) lImage.getHeight(),
                                (long) lImage.getDepth() },
                   true);
  
    ContiguousBuffer lBuffer = new ContiguousBuffer(lTransferMemory);
  
    long sum = 0;
    while (lBuffer.hasRemainingShort())
    {
      sum += lBuffer.readShort();
    }
    return sum;
  }
  */
}
