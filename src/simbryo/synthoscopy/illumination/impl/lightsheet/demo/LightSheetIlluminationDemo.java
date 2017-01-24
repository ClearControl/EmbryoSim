package simbryo.synthoscopy.illumination.impl.lightsheet.demo;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.illumination.impl.lightsheet.LightSheetIllumination;

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
                              ClearCLPhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                                   lPhantomWidth,
                                                                                   lPhantomHeight,
                                                                                   lPhantomDepth);

        Drosophila lDrosophila = new Drosophila(16, lGridDimensions);
        lDrosophila.simulationSteps(1000, 1);

        DrosophilaHistoneFluorescence lDrosoFluo =
                                                 new DrosophilaHistoneFluorescence(lContext,
                                                                                   lDrosophila,
                                                                                   lPhantomWidth,
                                                                                   lPhantomHeight,
                                                                                   lPhantomDepth);
        lDrosoFluo.render();

        LightSheetIllumination lLightSheetIllumination =
                                                       new LightSheetIllumination(lContext,
                                                                                  lPhantomWidth/4,
                                                                                  lPhantomHeight/4,
                                                                                  31L);

        lLightSheetIllumination.setLightSheetHeigth(0.5f);
        lLightSheetIllumination.setLightSheetPosition(0.5f,
                                                      0.5f,
                                                      0.5f);
        
        //lLightSheetIllumination.setLightSheetNormalVector(0.0f, 0.0f, 1.0f);
        //lLightSheetIllumination.setLightSheetAxisVector(1.0f, 0.3f, 0.0f);
        
        lLightSheetIllumination.setOrientationWithAnglesInDegrees(0, 0, 0);
        lLightSheetIllumination.setLightSheetThetaInDeg(3.0f);
        
        
        System.out.println("axis   vector:"+lLightSheetIllumination.getLightSheetAxisVector()  );
        System.out.println("normal vector:"+lLightSheetIllumination.getLightSheetNormalVector());
        


        //lDrosoFluo.getPhantomImage().fillZero(true);

        ElapsedTime.measure("renderlightsheet",
                            () -> lLightSheetIllumination.render(lDrosoFluo.getPhantomImage(),
                                                                 0.5f));

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