package simbryo.dynamics.tissue.embryo.zoo.demo;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.dynamics.tissue.embryo.zoo.Organoid;
import simbryo.dynamics.tissue.embryo.zoo.Spheroid;
import simbryo.util.timing.Timming;

/**
 * Basic demos for Embryo dynamics
 *
 * @author royer
 */
public class EmbryoDemo
{

  /**
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demoOrganoid() throws InterruptedException
  {

    Organoid lOrganoid = new Organoid(16, 16, 16);

    lOrganoid.open3DViewer();

    Timming lTimming = new Timming();

    while (lOrganoid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);
      lOrganoid.simulationSteps(1);
    }

    lOrganoid.getViewer().waitWhileShowing();
  }

  /**
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demoSpheroid() throws InterruptedException
  {
    Spheroid lSpheroid = new Spheroid(16, 16, 16);

    lSpheroid.open3DViewer();

    Timming lTimming = new Timming();

    while (lSpheroid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);
      lSpheroid.simulationSteps(1);
    }

    lSpheroid.getViewer().waitWhileShowing();
  }

  /**
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demoDrosophila() throws InterruptedException
  {
    Drosophila lDrosophila = new Drosophila(64, 16, 16, 16);

    lDrosophila.open3DViewer();

    lDrosophila.getViewer().setDisplayRadius(true);

    Timming lTimming = new Timming();

    while (lDrosophila.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(1);
      lDrosophila.simulationSteps(1);
    }

    lDrosophila.getViewer().waitWhileShowing();
  }

}
