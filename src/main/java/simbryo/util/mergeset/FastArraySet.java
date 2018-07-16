package simbryo.util.mergeset;

/**
 * Fast union of sets represented as arrays
 *
 * @author royer
 */
public class FastArraySet
{

  /**
   * Merges two sets.
   * 
   * @param pSetA
   *          set A
   * @param pSetB
   *          set B
   * @param pSetAuB
   *          result: union of A and B
   * @return size of resulting set
   */
  public static final int merge(final int[] pSetA,
                                final int[] pSetB,
                                final int[] pSetAuB)
  {
    return merge(pSetA,
                 0,
                 pSetA.length,
                 pSetB,
                 0,
                 pSetB.length,
                 pSetAuB,
                 0);
  }

  /**
   * Merges two sets A and B, excluding elements at the beginning of the two
   * arrays, and storing the result starting at some offset in the destination
   * array.
   * 
   * @param pSetA
   *          set A
   * @param pStartA
   *          start offset in A
   * @param pSetB
   *          set B
   * @param pStartB
   *          start ofset in B
   * @param pSetAuB
   *          result: union of A and B (taking into consideration the ofsets)
   * @param pStartAuB
   *          starting position for A union B
   * @return size of resulting set
   */
  public static final int merge(final int[] pSetA,
                                final int pStartA,
                                final int[] pSetB,
                                final int pStartB,
                                final int[] pSetAuB,
                                final int pStartAuB)
  {
    return merge(pSetA,
                 pStartA,
                 Integer.MAX_VALUE / 2,
                 pSetB,
                 pStartB,
                 Integer.MAX_VALUE / 2,
                 pSetAuB,
                 pStartAuB);
  }

  /**
   * Merges two sets A and B, excluding elements at the beginning of the two
   * arrays, and storing the result starting at some offset in the destination
   * array.
   * 
   * @param pSetA
   *          set A
   * @param pStartA
   *          start offset in A
   * @param pLengthA
   *          length of A starting from offset
   * @param pSetB
   *          set B
   * @param pStartB
   *          start ofset in B
   * @param pLengthB
   *          length of B starting from offset
   * @param pSetAuB
   *          result: union of A and B (taking into consideration the ofsets)
   * @param pStartAuB
   *          starting position for A union B
   * @return size of resulting set
   */
  public static final int merge(final int[] pSetA,
                                final int pStartA,
                                final int pLengthA,
                                final int[] pSetB,
                                final int pStartB,
                                final int pLengthB,
                                final int[] pSetAuB,
                                final int pStartAuB)
  {

    int lIndexA = pStartA;
    int lIndexB = pStartB;
    int lIndexAuB = pStartAuB;

    for (; lIndexA < pStartA + pLengthA
           && lIndexB < pStartB + pLengthB;)
    {
      int lValueA = pSetA[lIndexA];
      int lValueB = pSetB[lIndexB];

      if (lValueA == -1 || lValueB == -1)
        break;

      if (lValueA == lValueB)
      {
        pSetAuB[lIndexAuB] = lValueA;
        lIndexA++;
        lIndexB++;
        lIndexAuB++;
      }
      else
      {
        if (lValueA < lValueB)
        {
          pSetAuB[lIndexAuB] = lValueA;
          lIndexA++;
          lIndexAuB++;
        }
        else
        {
          pSetAuB[lIndexAuB] = lValueB;
          lIndexB++;
          lIndexAuB++;
        }
      }
    }

    while (lIndexA < pStartA + pLengthA)
    {
      int lValueA = pSetA[lIndexA];
      if (lValueA == -1)
        break;
      pSetAuB[lIndexAuB] = lValueA;
      lIndexA++;
      lIndexAuB++;
    }

    while (lIndexB < pStartB + pLengthB)
    {
      int lValueB = pSetB[lIndexB];
      if (lValueB == -1)
        break;
      pSetAuB[lIndexAuB] = lValueB;
      lIndexB++;
      lIndexAuB++;
    }

    return lIndexAuB - pStartAuB;
  }

}
