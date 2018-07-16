package simbryo.synthoscopy.phantom.io.demo;

import java.io.File;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.PhantomRendererUtils;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.io.PhantomRawWriter;

/**
 * Phantom raw writer demo
 *
 * @author royer
 */
public class PhantomRawWriterDemo
{

  /**
   * Demo
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void demo() throws Exception
  {
    /*String lUserHome = System.getProperty("user.home");
    File lDownloadFolder = new File(lUserHome + "/Downloads/");
    File lDataFolder = new File(lDownloadFolder, "DrosoStacks");/**/

    File lDataFolder =
                     new File("/Volumes/green-carpet/Simbryo/stacks");

    int lWidth = 512;
    int lHeight = 512;
    int lDepth = 512;

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();
        ClearCLContext lContext = lFastestGPUDevice.createContext())
    {

      int[] lGridDimensions =
                            PhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                          lWidth,
                                                                          lHeight,
                                                                          lDepth);

      Drosophila lDrosophila = new Drosophila(16, lGridDimensions);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                               new DrosophilaHistoneFluorescence(lContext,
                                                                                 lDrosophila,
                                                                                 lWidth,
                                                                                 lHeight,
                                                                                 lDepth);

      PhantomRawWriter lPhantomRawWriter =
                                         new PhantomRawWriter(NativeTypeEnum.Byte,
                                                              100,
                                                              0);
      lPhantomRawWriter.setOverwrite(false);

      int lStart = 8500;
      int lEnd = 15000;

      // move forward in simulation:
      lDrosophila.simulationSteps(lStart);

      // ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();

      int lPeriod = 10;

      while (lDrosophila.getTimeStepIndex() < lEnd)
      {
        lDrosophila.simulationSteps(lPeriod);
        long lTimeIndex = lDrosophila.getTimeStepIndex();

        lDrosoFluo.clear(true);
        lDrosoFluo.render(true);

        File lFile =
                   new File(lDataFolder,
                            String.format("stack.%d.%d.%d.%d.%s.raw",
                                          lWidth,
                                          lHeight,
                                          lDepth,
                                          lTimeIndex,
                                          lPhantomRawWriter.getDataType()));

        if (lPhantomRawWriter.write(lDrosoFluo, lFile))
        {
          System.out.println("Writting file: " + lFile);
        }
      }

      lPhantomRawWriter.close();
      lDrosoFluo.close();

    }

  }

}
