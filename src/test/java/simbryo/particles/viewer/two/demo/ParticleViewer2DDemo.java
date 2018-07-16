package simbryo.particles.viewer.two.demo;

import org.junit.Test;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.impl.CollisionForceField;
import simbryo.particles.viewer.two.ParticleViewer2D;
import simbryo.util.timing.Timming;

/**
 * Particle system 2D viewer demo
 *
 * @author royer
 */
public class ParticleViewer2DDemo
{

  private int cNumberOfParticles = 5000;
  private float cInitialVelocity = 0.0000001f;
  private float cParticleRadius =
                                (float) (0.4
                                         / Math.sqrt(cNumberOfParticles));
  private float cParticleMinimalRadius = 0.001f;
  private float cDragCoefficient = 0.99f;
  private float cCollisionVelocityLoss = 0.9f;
  private float cCollisionForce = 0.00001f;
  private float cGravityForce = 0.0000001f;

  /**
   * Demo
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo2D() throws InterruptedException
  {

    CollisionForceField lCollisionForceField =
                                             new CollisionForceField(cCollisionForce,
                                                                     cDragCoefficient,
                                                                     true);

    ParticleSystem lParticleSystem =
                                   new ParticleSystem(2,
                                                      cNumberOfParticles,
                                                      cParticleMinimalRadius,
                                                      cParticleMinimalRadius + 0.5f
                                                                               * cParticleRadius);

    for (int i = 0; i < cNumberOfParticles; i++)
    {
      float x = (float) Math.random();
      float y = (float) Math.random();

      int lId = lParticleSystem.addParticle(x, y);
      lParticleSystem.setVelocity(lId,
                                  (float) (cInitialVelocity
                                           * (Math.random() - 0.5f)),
                                  (float) (cInitialVelocity
                                           * (Math.random() - 0.5f)));
      lParticleSystem.setRadius(lId,
                                cParticleMinimalRadius
                                     + (cParticleRadius));// Math.random() *
    }

    lParticleSystem.setRadius(0, 0.06f);
    // lParticleSystem.setRadius(1, 0.06f);
    // lParticleSystem.setPosition(1, 0.55f, 0.45f);

    lParticleSystem.updateNeighborhoodGrid();

    // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

    ParticleViewer2D lParticleViewer2D =
                                       ParticleViewer2D.view(lParticleSystem,
                                                             "Particles Are Fun",
                                                             768,
                                                             768);

    Timming lTimming = new Timming();

    while (lParticleViewer2D.isShowing())
    {
      lTimming.syncAtPeriod(3);

      // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
      lParticleSystem.applyForceField(lCollisionForceField);
      if (cGravityForce > 0)
        lParticleSystem.applyForce(0f, cGravityForce);
      lParticleSystem.intergrateEuler();
      lParticleSystem.enforceBounds(cCollisionVelocityLoss);
      lParticleSystem.updateNeighborhoodGrid();

      float lMouseX = (float) (lParticleViewer2D.getMouseX());
      float lMouseY = (float) (lParticleViewer2D.getMouseY());

      lParticleSystem.setPosition(0, lMouseX, lMouseY);
      lParticleViewer2D.updateDisplay(true);

    }

    lParticleViewer2D.waitWhileShowing();
  }

}
