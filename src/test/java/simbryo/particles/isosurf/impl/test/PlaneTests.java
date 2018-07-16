package simbryo.particles.isosurf.impl.test;

import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import simbryo.particles.isosurf.impl.Plane;

/**
 * Ellipsoid tests
 *
 * @author royer
 */
public class PlaneTests
{

  /**
   * Test
   */
  @Test
  public void test()
  {
    Plane lPlane = new Plane(1f, 1f, 1f);
    lPlane.setPoint(0.5f, 0.5f, 0.5f);

    lPlane.clear();
    lPlane.addCoordinate(0.5f);
    lPlane.addCoordinate(0.5f);
    lPlane.addCoordinate(0.5f);

    assertEquals(0.0f, lPlane.getDistance(), 0f);
    assertEquals(0.5773f, lPlane.getNormalizedGardient(0), 0.001f);

    lPlane.clear();
    lPlane.addCoordinate(1.0f);
    lPlane.addCoordinate(1.0f);
    lPlane.addCoordinate(1.0f);

    assertEquals(sqrt(3 * 0.5 * 0.5), lPlane.getDistance(), 0.001f);
    assertEquals(0.5773f, lPlane.getNormalizedGardient(0), 0.001f);
  }

}
