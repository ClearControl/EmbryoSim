package simbryo.util.vectorinc;

/**
 * This static method provide functionality to enumerate all cells within a
 * given box. The enumeration is done by using a lexicographical ordering. Cells
 * are represented as vectors that start from a 'min' vector and end at the
 * 'max' vector.
 *
 * @author royer
 */
public class VectorInc
{

  /**
   * Increments the vector lowest significant coordinate, and propagates the
   * carry if needed.
   * 
   * @param pMin
   *          min vector
   * @param pMax
   *          max vector
   * @param pCurrent
   *          current vector to increment.
   * @return false if reached the highest vector, true otherwise.
   */
  public static boolean increment(int[] pMin,
                                  int[] pMax,
                                  int[] pCurrent)
  {
    pCurrent[0] = pCurrent[0] + 1;

    propagateCarry(pMin, pMax, pCurrent);

    int lLastIndex = pCurrent.length - 1;
    return pCurrent[lLastIndex] < pMax[lLastIndex];
  }

  private static void propagateCarry(int[] pMin,
                                     int[] pMax,
                                     int[] pCurrent)
  {
    int lLength = pMin.length;
    for (int i = 0; i < lLength; i++)
    {
      if (pCurrent[i] >= pMax[i] && i != pCurrent.length - 1)
      {
        pCurrent[i] -= (pMax[i] - pMin[i]);
        pCurrent[i + 1]++;
      }
    }

  }

}
