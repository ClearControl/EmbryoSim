package simbryo.synthoscopy.phantom.fluo.impl.drosophila.demo;

import java.io.File;
import java.util.Arrays;
import javafx.scene.control.Slider;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.io.PhantomRawWriter;
import simbryo.util.timing.Timming;

/**
 * Histone fluorescence drosophila demo
 *
 * @author royer
 */
public class HistoneFluorescenceDrosophilaDemo
{

  private static final boolean cWriteStack = false;
  private static final boolean cRenderFullStack = true;
  private static final int cWidth = 320;
  private static final int cHeight = cWidth;
  private static final int cDepth = cWidth;
  private static final int cStartSimulationStep = 0;
  private static final int cDisplayPeriod = 10;

  /**
   * Demo
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void demo() throws Exception
  {

    PhantomRawWriter lPhantomRawWriter =
                                       new PhantomRawWriter(NativeTypeEnum.Float,
                                                            1f,
                                                            0f);

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getDeviceByName("HD");

        ClearCLContext lContext = lFastestGPUDevice.createContext())
    {

      Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(14);

      System.out.println("grid size:"
                         + Arrays.toString(lDrosophila.getGridDimensions()));
      // lDrosophila.open3DViewer();
      // lDrosophila.getViewer().setDisplayRadius(false);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                               new DrosophilaHistoneFluorescence(lContext,
                                                                                 lDrosophila,
                                                                                 cWidth,
                                                                                 cHeight,
                                                                                 cDepth);

      ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();
      Slider lZSlider = lOpenViewer.getZSlider();

      lDrosophila.simulationSteps(cStartSimulationStep);

      Timming lTimming = new Timming();

      int i = 0;
      boolean lAbort = false;
      while (lOpenViewer.isShowing() && !lAbort)
      {
        // System.out.println("i=" + i);
        lTimming.syncAtPeriod(5);

        ElapsedTime.measure(i % cDisplayPeriod == 0,
                            "dynamics",
                            () -> lDrosophila.simulationSteps(1));

        if (i % cDisplayPeriod == 0)
        {
          lDrosoFluo.clear(true);

          if (cRenderFullStack)
            lDrosoFluo.render(true);
          else
            lDrosoFluo.render((int) lZSlider.valueProperty().get()
                              - 1,
                              (int) lZSlider.valueProperty().get()
                                   + 1,
                              true);

          lAbort = checkMaxGridCellOccupancy(lDrosophila, lAbort);

          if (cWriteStack)
            lPhantomRawWriter.write(lDrosoFluo,
                                    new File("/Users/royer/Temp/temp.raw"));
          // lAbort=true;
        }

        i++;
      }

      lDrosoFluo.close();

      lPhantomRawWriter.close();

    }

  }

  private boolean checkMaxGridCellOccupancy(Drosophila lDrosophila,
                                            boolean lAbort)
  {
    float lMaximalCellOccupancy =
                                lDrosophila.getNeighborhoodGrid()
                                           .getMaximalCellOccupancy();

    System.out.println("lMaximalCellOccupancy="
                       + lMaximalCellOccupancy);

    if (lMaximalCellOccupancy >= 0.99f)
    {
      System.err.println("MAXIMAL OCCUPANCY REACHED!!!");
      lAbort = true;
    }
    return lAbort;
  }

}
