package simbryo.particles.viewer.two;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import clearcl.viewer.jfx.PanZoomScene;
import simbryo.particles.ParticleSystem;
import simbryo.particles.viewer.ParticleViewerInterface;
import simbryo.util.jfx.JavaFXUtil;

/**
 * 2D Particle system viewer.
 *
 * @author royer
 */
public class ParticleViewer2D extends Stage
                              implements ParticleViewerInterface
{

  private ParticleViewerCanvas mViewParticles2D;
  private ParticleSystem mParticleSystem;
  private PanZoomScene mPanZoomScene;

  /**
   * Opens up a particle viewer, taking care that JavaFX is initialized.
   * 
   * @param pParticleSystem
   *          particle system
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   * @return viewer.
   */
  public static ParticleViewer2D view(ParticleSystem pParticleSystem,
                                      String pWindowTitle,
                                      int pWindowWidth,
                                      int pWindowHeight)
  {
    return JavaFXUtil.runAndWait(() -> {
      return new ParticleViewer2D(pParticleSystem,
                                  pWindowTitle,
                                  pWindowWidth,
                                  pWindowHeight);
    });
  }

  /**
   * Creates a view for a given particle system
   * 
   * @param pParticleSystem
   *          particle system
   */
  public ParticleViewer2D(ParticleSystem pParticleSystem)
  {
    this(pParticleSystem, "Particle System Viewer", 512, 512);
  }

  /**
   * Creates a view for a given particle system. Window title, and window
   * dimensions can be specified.
   * 
   * @param pParticleSystem
   *          particle system
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   */
  public ParticleViewer2D(ParticleSystem pParticleSystem,
                          String pWindowTitle,
                          int pWindowWidth,
                          int pWindowHeight)
  {
    super();
    mParticleSystem = pParticleSystem;

    setTitle(pWindowTitle);

    mViewParticles2D = new ParticleViewerCanvas(pWindowWidth,
                                                pWindowHeight);

    StackPane lStackPane = new StackPane();

    lStackPane.getChildren().addAll(mViewParticles2D);

    mPanZoomScene = new PanZoomScene(this,
                                     lStackPane,
                                     mViewParticles2D,
                                     pWindowWidth,
                                     pWindowHeight,
                                     Color.BLACK);
    mPanZoomScene.setFill(Color.BLACK);

    setScene(mPanZoomScene);
    show();
  }

  /**
   * Triggers an update of the view. Must be called after the particle system
   * has been updated.
   * 
   * @param pBlocking
   *          if true, the viewer waits for the previous rendering to finish.
   */
  @Override
  public void updateDisplay(boolean pBlocking)
  {
    mViewParticles2D.updateDisplay(mParticleSystem, pBlocking);
  }

  /**
   * Waits (blocking call) while window is showing.
   */
  @Override
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
   * Returns the mouse's x coordinate
   * 
   * @return mouse x
   */
  public double getMouseX()
  {
    return mPanZoomScene.getMouseX();
  }

  /**
   * Returns the mouse's y coordinate
   * 
   * @return mouse y
   */
  public double getMouseY()
  {
    return mPanZoomScene.getMouseY();
  }

  /**
   * Returns the scene's width
   * 
   * @return width
   */
  public double getSceneWidth()
  {
    return mPanZoomScene.getWidth();
  }

  /**
   * Returns the scene's height
   * 
   * @return height
   */
  public double getSceneHeight()
  {
    return mPanZoomScene.getHeight();
  }

  /**
   * Sets the display grid flag
   * 
   * @param pDisplayGrid
   *          true -> grid displayed, false otherwise
   */
  public void setDisplayGrid(boolean pDisplayGrid)
  {
    mViewParticles2D.setDisplayGrid(pDisplayGrid);
  }

  /**
   * Sets the display elapsed time flag
   * 
   * @param pDisplayElapsedTime
   *          true -> elapsed time displayed, false otherwise
   */
  public void setDisplayElapsedTime(boolean pDisplayElapsedTime)
  {
    mViewParticles2D.setDisplayElapsedTime(pDisplayElapsedTime);
  }

  @Override
  public void setDisplayRadius(boolean pDisplayRadius)
  {
    mViewParticles2D.setDisplayRadius(pDisplayRadius);
  }

}
