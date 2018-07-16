package simbryo.particles.test;

import org.junit.Test;
import simbryo.particles.ParticleSystem;

/**
 * Particles system test
 *
 * @author royer
 */
public class ParticleSystemTests
{

  /**
   * Basic tests
   */
  @Test
  public void test()
  {
    ParticleSystem lParticleSystem =
                                   new ParticleSystem(2, 4, 50, 4000);

    lParticleSystem.addParticle(0f, 0f);
    lParticleSystem.addParticle(1f, 1f);

    lParticleSystem.setVelocity(0, 0.001f, 0.001f);
    lParticleSystem.setVelocity(1, -0.001f, -0.001f);

    lParticleSystem.setRadius(0, 0.1f);
    lParticleSystem.setRadius(1, 0.1f);

    lParticleSystem.updateNeighborhoodGrid();

  }

}
