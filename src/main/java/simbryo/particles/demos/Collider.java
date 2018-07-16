package simbryo.particles.demos;

import java.util.Optional;
import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.impl.CollisionForceField;
import simbryo.particles.viewer.two.ParticleViewer2D;
import simbryo.util.timing.Timming;

/**
 * Collider App - Play with thousands of colliding disks.
 *
 * @author royer
 */
public class Collider extends Application
{

  private int cNumberOfParticles = 20;
  private float cInitialVelocity = 0.0000001f;

  private float cRadius = (float) (0.395
                                   / Math.sqrt(cNumberOfParticles));
  private float cDragCoeficient = 0.99f;
  private float cBouncingVelocityLoss = 0.9f;
  private float cCollisionForce = 0.0001f;
  private float cGravityForce = 0.000001f;

  private Thread mThread;

  @Override
  public void start(Stage primaryStage)
  {
    primaryStage.close();

    try
    {

      TextInputDialog dialog = new TextInputDialog("100");

      dialog.setTitle("Collider");
      dialog.setHeaderText("Parameters");
      dialog.setContentText("Number of particles:");

      // Traditional way to get the response value.
      Optional<String> result = dialog.showAndWait();

      result.ifPresent(name -> {
        try
        {
          cNumberOfParticles = Integer.parseInt(name);
          cRadius = (float) (0.395 / Math.sqrt(cNumberOfParticles));

        }
        catch (Throwable e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });

      CollisionForceField lCollisionForceField =
                                               new CollisionForceField(cCollisionForce,
                                                                       cDragCoeficient,
                                                                       true);

      ParticleSystem lParticleSystem = new ParticleSystem(2,
                                                          cNumberOfParticles,
                                                          0.5f * cRadius,
                                                          cRadius);

      ParticleViewer2D lParticleViewer =
                                       new ParticleViewer2D(lParticleSystem,
                                                            "Particles Are Fun",
                                                            768,
                                                            768);
      lParticleViewer.setDisplayGrid(false);
      lParticleViewer.setDisplayElapsedTime(true);

      Runnable lRunnable = () -> {

        for (int i = 0; i < cNumberOfParticles; i++)
        {
          addParticle(lParticleSystem);
        }
        lParticleSystem.enforceBounds(0);

        lParticleSystem.setRadius(0, 0.06f);
        // lParticleSystem.setRadius(1, 0.06f);
        // lParticleSystem.setPosition(1, 0.55f, 0.45f);

        lParticleSystem.updateNeighborhoodGrid();

        // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

        Timming lTimming = new Timming();

        while (lParticleViewer.isShowing())
        {
          lTimming.syncAtPeriod(1);

          // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
          lParticleSystem.updateNeighborhoodGrid();
          lParticleSystem.applyForceField(lCollisionForceField);
          if (cGravityForce > 0)
            lParticleSystem.applyForce(0f, cGravityForce);
          lParticleSystem.intergrateEuler();
          lParticleSystem.enforceBounds(cBouncingVelocityLoss);

          float lMouseX = (float) (lParticleViewer.getMouseX());
          float lMouseY = (float) (lParticleViewer.getMouseY());

          lParticleSystem.setPosition(0, lMouseX, lMouseY);
          lParticleViewer.updateDisplay(true);

          // addParticle(lParticleSystem);
        }

        lParticleViewer.waitWhileShowing();
      };

      mThread = new Thread(lRunnable);
      mThread.setDaemon(true);
      mThread.start();
    }
    catch (Throwable e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void addParticle(ParticleSystem lParticleSystem)
  {
    if (lParticleSystem.getNumberOfParticles() >= lParticleSystem.getMaxNumberOfParticles())
      return;

    float x = (float) ((Math.random()));
    float y = (float) ((Math.random()));

    int lId = lParticleSystem.addParticle(x, y);

    // System.out.format("(%d,%d) -> (%g,%g) \n", i, lId, x, y);

    lParticleSystem.setVelocity(lId,
                                (float) (cInitialVelocity
                                         * (Math.random() - 0.5f)),
                                (float) (cInitialVelocity
                                         * (Math.random() - 0.5f)));
    lParticleSystem.setRadius(lId,
                              (float) (1e-9 + (1 * cRadius)
                                       + 0.0001f
                                         * (Math.random() - 0.5f))); //
  }

  /**
   * Main function
   * 
   * @param args
   *          arguments
   * @throws InterruptedException
   *           NA
   */
  public static void main(String[] args) throws InterruptedException
  {
    launch(args);

  }

}
