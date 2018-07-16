package simbryo.util.vectorinc.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import simbryo.util.vectorinc.VectorInc;

/**
 * Vector incrementation tests
 *
 * @author royer
 */
public class VectorIncTests
{

  /**
   * Test zero min
   */
  @Test
  public void testZeroMin()
  {
    int[] lMin = new int[]
    { 0, 0, 0 };

    int[] lMax = new int[]
    { 4, 3, 2 };

    int[] lCurrent = new int[]
    { 0, 0, 0 };

    int i = 0;
    do
    {
      assertEquals(i % 4, lCurrent[0]);
      assertEquals((i / 4) % 3, lCurrent[1]);
      assertEquals((i / 12) % 2, lCurrent[2]);
      System.out.println(Arrays.toString(lCurrent));
      i++;
    }
    while (VectorInc.increment(lMin, lMax, lCurrent));

  }

  /**
   * Test non-xero min
   */
  @Test
  public void testNonZeroMin()
  {
    int[] lMin = new int[]
    { 1, 1, 1 };

    int[] lMax = new int[]
    { 4, 3, 2 };

    int[] lCurrent = new int[]
    { 1, 1, 1 };

    int i = 0;
    do
    {
      assertEquals(1 + i % 3, lCurrent[0]);
      assertEquals(1 + (i / 3) % 2, lCurrent[1]);
      assertEquals(1, lCurrent[2]);
      System.out.println(Arrays.toString(lCurrent));
      i++;
    }
    while (VectorInc.increment(lMin, lMax, lCurrent));

  }

}
