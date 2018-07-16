package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class SynthoscopySandBox
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

      int lNumberOfDetectionArms = 1;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      boolean lWriteFile = false;

      RawWriter lRawWriter = new RawWriter();
      lRawWriter.setOverwrite(true);
      File lDesktopFolder = new File(System.getProperty("user.home")
                                     + "/Temp/data");
      lDesktopFolder.mkdirs();

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
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

        /*
         * ClearCLImageViewer lFluoPhantomViewer =
         * lDrosophilaFluorescencePhantom.openViewer();/
         **/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*
         * ClearCLImageViewer lScatterPhantomViewer =
         * lDrosophilaScatteringPhantom.openViewer();/
         **/

        LightSheetMicroscopeSimulatorOrtho lSimulator =
                                                      new LightSheetMicroscopeSimulatorOrtho(lContext,
                                                                                             lNumberOfDetectionArms,
                                                                                             lNumberOfIlluminationArms,
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
                                              lSimulator.openViewerForCameraImage(0);
        for (int i = 1; i < lNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        // for (int i = 0; i < lNumberOfIlluminationArms; i++)
        // lSimulator.openViewerForLightMap(i);

        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      0,
                                      1f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      0.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      0,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      0,
                                      0f);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1,
                                      20f);

        int i = 0;

        for (float z =
                     -0.0f; z < 0.3
                            && lCameraImageViewer.isShowing(); z +=
                                                                 0.001)
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile =
                          new File(lDesktopFolder,
                                   String.format("file%d.raw", i++)); // lDrosophila.getTimeStepIndex()

            System.out.println("Writting: " + lRawFile);
            lRawWriter.write(lSimulator.getCameraImage(0), lRawFile);
          }
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }

      lRawWriter.close();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  private void simulate_single_beams(final int side,
                                     final float angle,
                                     final int n_planes,
                                     final String outdir) throws IOException,
                                                          InterruptedException
  {

    try
    {

      int lNumberOfDetectionArms = 1;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      final float z_max = 0.25f;

      boolean lWriteFile = (outdir != null);

      System.out.format("side =  %d, angle = %f, outdir = %s",
                        side,
                        angle,
                        outdir);

      RawWriter lRawWriter = new RawWriter();
      lRawWriter.setOverwrite(true);
      File lDesktopFolder = null;
      if (lWriteFile)
      {
        lDesktopFolder = new File(outdir);
        lDesktopFolder.mkdirs();
      }

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
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

        /*
         * ClearCLImageViewer lFluoPhantomViewer =
         * lDrosophilaFluorescencePhantom.openViewer();/
         **/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*
         * ClearCLImageViewer lScatterPhantomViewer =
         * lDrosophilaScatteringPhantom.openViewer();/
         **/

        LightSheetMicroscopeSimulatorOrtho lSimulator =
                                                      new LightSheetMicroscopeSimulatorOrtho(lContext,
                                                                                             lNumberOfDetectionArms,
                                                                                             lNumberOfIlluminationArms,
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
                                              lSimulator.openViewerForCameraImage(0);
        for (int i = 1; i < lNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        // for (int i = 0; i < lNumberOfIlluminationArms; i++)
        // lSimulator.openViewerForLightMap(i);

        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      0,
                                      1.2f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      1.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      side,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1 - side,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      side,
                                      angle);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1 - side,
                                      0f);

        int counter = 0;

        for (float z =
                     -z_max; z <= z_max
                             && lCameraImageViewer.isShowing(); z +=
                                                                  2.f * z_max
                                                                     / (n_planes
                                                                        - 1.f))
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile =
                          new File(lDesktopFolder,
                                   String.format("file_%01d_%01d_%04d.raw",
                                                 side,
                                                 (angle < 0) ? 0 : 1,
                                                 counter++)); // lDrosophila.getTimeStepIndex()

            // System.out.println("Writting: " + lRawFile);
            lRawWriter.write(lSimulator.getCameraImage(0), lRawFile);
          }
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }

      lRawWriter.close();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  /**
   * Demo single arms
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo_single_arms() throws IOException,
                                 InterruptedException
  {

    // simulate_single_beams(1, 20.f, 51, null);

    for (int side = 0; side < 2; side++)
      for (int angle_mode = 0; angle_mode < 2; angle_mode++)
        simulate_single_beams(side,
                              ((angle_mode < 1) ? -1.f : 1.f) * 20.f,
                              101,
                              System.getProperty("user.home")
                                   + "/Tmp/xscope");
  }
}
