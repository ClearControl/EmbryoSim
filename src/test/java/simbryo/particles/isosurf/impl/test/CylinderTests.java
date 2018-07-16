package simbryo.particles.isosurf.impl.test;

import org.junit.Test;
import simbryo.particles.isosurf.impl.Cylinder;

/**
 * Ellipsoid tests
 *
 * @author royer
 */
public class CylinderTests
{

  /**
   * Test
   */
  @Test
  public void test()
  {
    Cylinder lCylinder = new Cylinder(0.1f, 0.5f, 0.5f, 1f, 1f);

    lCylinder.clear();
    lCylinder.addCoordinate(0.6f);
    lCylinder.addCoordinate(0.5f);
    lCylinder.addCoordinate(0.5f);

    System.out.println(lCylinder.getDistance());
    System.out.println(lCylinder.getNormalizedGardient(0));
    System.out.println(lCylinder.getNormalizedGardient(1));

    // TODO: check if Cylinder iso-surface really works...
    /*    assertEquals(0.0f, lCylinder.getDistance(), 0f);
    assertEquals(0.0f, lCylinder.getNormalizedGardient(0), 0f);
    assertEquals(-1.0f, lCylinder.getNormalizedGardient(1), 0f);
    
    lCylinder.clear();
    lCylinder.addCoordinate(1f);
    lCylinder.addCoordinate(1f);
    
    System.out.println(lCylinder.getDistance());
    System.out.println(lCylinder.getNormalizedGardient(0));
    System.out.println(lCylinder.getNormalizedGardient(1));
    
    assertEquals(0.61f, lCylinder.getDistance(), 0.01f);
    assertEquals(-0.24f, lCylinder.getNormalizedGardient(0), 0.01f);
    assertEquals(-0.97f, lCylinder.getNormalizedGardient(1), 0.01f);
    
    lCylinder.clear();
    lCylinder.addCoordinate(1f);
    lCylinder.addCoordinate(0.5f);
    
    System.out.println(lCylinder.getDistance());
    System.out.println(lCylinder.getNormalizedGardient(0));
    System.out.println(lCylinder.getNormalizedGardient(1));
    
    assertEquals(0.0f, lCylinder.getDistance(), 0.01f);
    assertEquals(-1f, lCylinder.getNormalizedGardient(0), 0.01f);
    assertEquals(-0.0f, lCylinder.getNormalizedGardient(1), 0.01f);
    
    /**/
  }

}
