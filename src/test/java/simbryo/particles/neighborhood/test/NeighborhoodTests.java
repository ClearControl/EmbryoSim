package simbryo.particles.neighborhood.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import simbryo.particles.neighborhood.NeighborhoodGrid;

/**
 * Neighborhood data structure tests
 *
 * @author royer
 */
public class NeighborhoodTests
{

  /**
   * Test neighboorhood queries without radius
   */
  @Test
  public void testNoRadius()
  {
    NeighborhoodGrid lNeighborhood = new NeighborhoodGrid(10, 4, 4);

    lNeighborhood.clear();

    float[] lPositions = new float[]
    { 0f, 0f, 0.3f, 0.3f, 0.9f, 0.2f, 1f, 1f };
    float[] lRadius = new float[]
    { 0f, 0f, 0f, 0.0f };

    lNeighborhood.updateCells(lPositions, lRadius);

    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        if (lNeighborhood.getCellContents(i, j)[0] != -1)
          System.out.println(lNeighborhood.getCellInfoAt(i, j));

    System.out.println(Arrays.toString(lNeighborhood.getCellContents(0,
                                                                     0)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContents(1,
                                                                     1)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContents(3,
                                                                     0)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContents(3,
                                                                     3)));

    assertEquals(0, lNeighborhood.getCellContents(0, 0)[0]);
    assertEquals(1, lNeighborhood.getCellContents(1, 1)[0]);
    assertEquals(2, lNeighborhood.getCellContents(3, 0)[0]);
    assertEquals(3, lNeighborhood.getCellContents(3, 3)[0]);

    assertEquals(-1, lNeighborhood.getCellContents(0, 0)[1]);
    assertEquals(-1, lNeighborhood.getCellContents(1, 1)[1]);
    assertEquals(-1, lNeighborhood.getCellContents(3, 0)[1]);
    assertEquals(-1, lNeighborhood.getCellContents(3, 3)[2]);

    System.out.println(Arrays.toString(lNeighborhood.getCellContentsAt(0f,
                                                                       0f)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContentsAt(0.29f,
                                                                       0.29f)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContentsAt(0.85f,
                                                                       0.85f)));
    System.out.println(Arrays.toString(lNeighborhood.getCellContentsAt(0.95f,
                                                                       0.21f)));

    assertEquals(0, lNeighborhood.getCellContentsAt(0.0f, 0.0f)[0]);
    assertEquals(1, lNeighborhood.getCellContentsAt(0.29f, 0.29f)[0]);
    assertEquals(3, lNeighborhood.getCellContentsAt(0.85f, 0.85f)[0]);
    assertEquals(3, lNeighborhood.getCellContentsAt(0.85f, 0.85f)[0]);
    assertEquals(3, lNeighborhood.getCellContentsAt(0.95f, 0.95f)[0]);
    assertEquals(2, lNeighborhood.getCellContentsAt(0.95f, 0.19f)[0]);
  }

  /**
   * Test neighborhood without radius
   */
  @Test
  public void testWithRadius()
  {
    NeighborhoodGrid lNeighborhood = new NeighborhoodGrid(10, 4, 4);

    lNeighborhood.clear();

    float[] lPositions = new float[]
    { 0f, 0f, 0.30f, 0.30f, 0.25f, 0.76f, 1f, 1f };
    float[] lRadius = new float[]
    { 0.12f, 0.12f, 0.12f, 0.12f };

    lNeighborhood.updateCells(lPositions, lRadius);

    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        if (lNeighborhood.getCellContents(i, j)[0] != -1)
          System.out.println(lNeighborhood.getCellInfoAt(i, j));

    assertEquals(0, lNeighborhood.getCellContents(0, 0)[0]);
    assertEquals(1, lNeighborhood.getCellContents(0, 0)[1]);
    assertEquals(2, lNeighborhood.getCellContents(0, 2)[0]);
    assertEquals(2, lNeighborhood.getCellContents(0, 3)[0]);
    assertEquals(1, lNeighborhood.getCellContents(1, 0)[0]);
    assertEquals(1, lNeighborhood.getCellContents(1, 1)[0]);

    assertEquals(1, lNeighborhood.getCellContents(1, 1)[0]);
    assertEquals(2, lNeighborhood.getCellContents(1, 2)[0]);
    assertEquals(2, lNeighborhood.getCellContents(1, 3)[0]);
    assertEquals(3, lNeighborhood.getCellContents(3, 3)[0]);

  }

  /**
   * Test neighborhood queries with radius
   */
  @Test
  public void testWithRadiusQuery()
  {
    NeighborhoodGrid lNeighborhood = new NeighborhoodGrid(10, 4, 4);

    lNeighborhood.clear();

    float[] lPositions = new float[]
    { 0f, 0f, 0.30f, 0.30f, 0.25f, 0.76f, 1f, 1f };
    float[] lRadius = new float[]
    { 0.12f, 0.12f, 0.12f, 0.12f };

    lNeighborhood.updateCells(lPositions, lRadius);

    int[] lNeighboors = new int[4 * 4 * 10];
    int[] lNeighboorsTemp = new int[lNeighboors.length];

    int lNumberOfNeighboors =
                            lNeighborhood.getAllNeighborsForParticle(lNeighboors,
                                                                     lNeighboorsTemp,
                                                                     lPositions,
                                                                     0,
                                                                     0.5f);

    System.out.println(lNumberOfNeighboors);
    System.out.println(Arrays.toString(lNeighboors));

    assertEquals(3, lNumberOfNeighboors);
    assertEquals(0, lNeighboors[0]);
    assertEquals(1, lNeighboors[1]);
    assertEquals(2, lNeighboors[2]);
    assertEquals(-1, lNeighboors[3]);

    lNumberOfNeighboors =
                        lNeighborhood.getAllNeighborsForParticle(lNeighboors,
                                                                 lNeighboorsTemp,
                                                                 lPositions,
                                                                 2,
                                                                 0.25f);

    System.out.println(lNumberOfNeighboors);
    System.out.println(Arrays.toString(lNeighboors));

    assertEquals(1, lNumberOfNeighboors);
    assertEquals(2, lNeighboors[0]);
    assertEquals(-1, lNeighboors[1]);

  }

}
