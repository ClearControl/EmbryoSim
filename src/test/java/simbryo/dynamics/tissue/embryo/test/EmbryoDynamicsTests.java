package simbryo.dynamics.tissue.embryo.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.util.serialization.SerializationUtilities;

/**
 * Tests for embryo dynamics
 *
 * @author royer
 */
public class EmbryoDynamicsTests
{

  /**
   * This test writes an embryo after about 7 divisions into a file and reloads
   * it from the file.
   * 
   * @throws IOException
   *           NA
   */
  @Test
  public void testSerialization() throws IOException
  {
    Drosophila lDrosophila = new Drosophila(64, 16, 16, 16);

    int lNumberOfSteps = 7000;

    lDrosophila.simulationSteps(7000);

    File lTempFile = File.createTempFile("EmbryoDynamicsTests",
                                         "testSerialization");
    System.out.println(lTempFile);

    SerializationUtilities.saveToFile(lDrosophila, lTempFile);

    Drosophila lDrosophilaLoaded =
                                 SerializationUtilities.loadFromFile(Drosophila.class,
                                                                     lTempFile);

    assertTrue(lDrosophilaLoaded.getTimeStepIndex() == lNumberOfSteps);

    // uncomment below to check visually:
    // ParticleViewer3D lOpen3dViewer = lDrosophilaLoaded.open3DViewer();
    // lOpen3dViewer.updateDisplay(true);

    // lDrosophilaLoaded.getViewer().waitWhileShowing();
  }

}
