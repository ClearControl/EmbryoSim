package simbryo.synthoscopy.microscope.lightsheet.gui;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

import com.sun.javafx.application.PlatformImpl;

import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;

/**
 * Utility to control the parameters of a lightsheet microscope simulator.
 *
 * @author royer
 */
@SuppressWarnings("restriction")
public class LightSheetMicroscopeSimulatorViewer
{

  private Stage mStage = null;
  private LightSheetMicroscopeSimulatorPanel mSimulatorControlPane;
  private Slider mZSlider;

  /**
   * Opens a window showing the controls and views of the simulated microscope.
   * 
   * @param pSimulator
   *          simulator to view
   * @param pWindowTitle
   *          window title
   * @return image viewer
   */
  public static LightSheetMicroscopeSimulatorViewer view(LightSheetMicroscopeSimulator pSimulator,
                                                         String pWindowTitle)
  {
    LightSheetMicroscopeSimulatorViewer lViewImage =
                                                   new LightSheetMicroscopeSimulatorViewer(pSimulator,
                                                                                           pWindowTitle);
    return lViewImage;
  }

  /**
   * Creates a lightsheet microscope simulator view for a given microscope,
   * window title.
   * 
   * @param pSimulator
   *          simulator to view
   * @param pWindowTitle
   *          window title
   */
  public LightSheetMicroscopeSimulatorViewer(LightSheetMicroscopeSimulator pSimulator,
                                             String pWindowTitle)
  {
    this(pSimulator, pWindowTitle, 512, 512);
  }

  /**
   * Creates a view for a given image, window title, and window dimensions.
   * 
   * @param pSimulator
   *          simulator to view
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   */

  public LightSheetMicroscopeSimulatorViewer(LightSheetMicroscopeSimulator pSimulator,
                                             String pWindowTitle,
                                             int pWindowWidth,
                                             int pWindowHeight)
  {
    super();

    PlatformImpl.startup(() -> {
    });
    final CountDownLatch lCountDownLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      mStage = new Stage();
      mStage.setTitle(pWindowTitle);

      mSimulatorControlPane =
                            new LightSheetMicroscopeSimulatorPanel(pSimulator);

      Scene lScene = new Scene(mSimulatorControlPane,
                               pWindowWidth,
                               pWindowHeight);

      mStage.setScene(lScene);
      mStage.show();
      lCountDownLatch.countDown();
    });
    try
    {
      lCountDownLatch.await();
    }
    catch (InterruptedException e)
    {
    }

  }

  /**
   * Waits (blocking call) while window is showing.
   */
  public void waitWhileShowing()
  {
    while (isShowing())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  /**
   * Returns true if image view is showing.
   * 
   * @return true if showing
   */
  public boolean isShowing()
  {
    return mStage.isShowing();
  }

  /**
   * Returns this viewer's z slider
   * 
   * @return z slider
   */
  public Slider getZSlider()
  {
    return mZSlider;
  }

}
