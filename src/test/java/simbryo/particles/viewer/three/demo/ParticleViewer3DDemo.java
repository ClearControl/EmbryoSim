package simbryo.particles.viewer.three.demo;

import javafx.scene.paint.Color;

import org.junit.Test;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.impl.CollisionForceField;
import simbryo.particles.viewer.three.ParticleViewer3D;
import simbryo.util.timing.Timming;

/**
 * Particle 3D viewer demo
 *
 * @author royer
 */
public class ParticleViewer3DDemo
{

  private int cNumberOfParticles = 2000;
  private float cInitialVelocity = 0.0001f;
  private float cParticlesRadius = (float) (0.25
                                            / Math.pow(cNumberOfParticles,
                                                       0.33f));
  private float cParticlesMinRadius = 0.01f;
  private float cDragCoefficient = 0.99f;
  private float cCollisionVelocityLoss = 0.9f;
  private float cCollisionForce = 0.0001f;
  private float cGravityForce = 0.000001f;

  /**
   * Demo
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo3D() throws InterruptedException
  {

    CollisionForceField lCollisionForceField =
                                             new CollisionForceField(cCollisionForce,
                                                                     cDragCoefficient,
                                                                     true);

    ParticleSystem lParticleSystem =
                                   new ParticleSystem(3,
                                                      cNumberOfParticles,
                                                      cParticlesMinRadius,
                                                      cParticlesMinRadius + 0.5f
                                                                            * cParticlesRadius);

    for (int i = 0; i < cNumberOfParticles; i++)
    {
      float x = (float) Math.random();
      float y = (float) Math.random();
      float z = (float) Math.random();

      int lId = lParticleSystem.addParticle(x, y, z);
      lParticleSystem.setVelocity(lId,
                                  (float) (cInitialVelocity
                                           * (Math.random() - 0.5f)),
                                  (float) (cInitialVelocity
                                           * (Math.random() - 0.5f)),
                                  (float) (cInitialVelocity
                                           * (Math.random() - 0.5f)));
      lParticleSystem.setRadius(lId,
                                (float) (cParticlesMinRadius
                                         + (cParticlesRadius)
                                         + 0.01 * Math.random())); //
    }

    lParticleSystem.setRadius(0, 0.06f);
    // lParticleSystem.setRadius(1, 0.06f);
    // lParticleSystem.setPosition(1, 0.55f, 0.45f);

    lParticleSystem.updateNeighborhoodGrid();

    // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

    ParticleViewer3D lParticleViewer3D =
                                       ParticleViewer3D.view(lParticleSystem,
                                                             "Particles Are Fun",
                                                             768,
                                                             768);

    lParticleViewer3D.setColorClosure((id) -> {
      return Color.hsb(id % 360, 0.6, 0.6);
    });

    Timming lTimming = new Timming();

    while (lParticleViewer3D.isShowing())
    {
      lTimming.syncAtPeriod(3);

      // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
      lParticleSystem.applyForceField(lCollisionForceField);
      if (cGravityForce > 0)
        lParticleSystem.applyForce(0f, cGravityForce, 0f);
      lParticleSystem.intergrateEuler();
      lParticleSystem.enforceBounds(cCollisionVelocityLoss);
      lParticleSystem.updateNeighborhoodGrid();

      /*float lMouseX = (float) (lParticleViewer3D.getMouseX());
      float lMouseY = (float) (lParticleViewer3D.getMouseY());/**/

      // lParticleSystem.setPosition(0, lMouseX, lMouseY);

      lParticleViewer3D.updateDisplay(true);
    }

    lParticleViewer3D.waitWhileShowing();
  }

}
