package simbryo.util;

import java.io.Serializable;

/**
 * Instances of this class implement a double-buffering scheme for float arrays.
 * Two arrays are maintained: a read and write array, a method is provided to
 * swap the two arrays, as well as other methods for performing other typical
 * operations.
 *
 * @author royer
 */
public class DoubleBufferingFloatArray implements Serializable
{
  private static final long serialVersionUID = 1L;

  private float[] mReadArray, mWriteArray;

  /**
   * Initialize the two arrays with a given fixed size.
   * 
   * @param pLength
   *          length of array
   */
  public DoubleBufferingFloatArray(int pLength)
  {
    super();
    allocateArrays(pLength);
  }

  /**
   * Allocates the two arrays with a (new) length.
   * 
   * @param pLength
   *          length of array
   */
  public void allocateArrays(int pLength)
  {
    mReadArray = new float[pLength];
    mWriteArray = new float[pLength];
  }

  /**
   * Returns the read array.
   * 
   * @return read array.
   */
  public float[] getReadArray()
  {
    return mReadArray;
  }

  /**
   * Returns the write array.
   * 
   * @return write array.
   */
  public float[] getWriteArray()
  {
    return mWriteArray;
  }

  /**
   * Returns the current array.
   * 
   * @return current array.
   */
  public float[] getCurrentArray()
  {
    return mReadArray;
  }

  /**
   * Returns the previous array.
   * 
   * @return previous array
   */
  public float[] getPreviousArray()
  {
    return mWriteArray;
  }

  /**
   * Clears array with zeros.
   * 
   * @param pBeginIndex
   *          begin index (inclusive)
   * @param pEndIndex
   *          end index (exclusive)
   */
  public void clear(int pBeginIndex, int pEndIndex)
  {
    set(pBeginIndex, pEndIndex, 0f);
  }

  /**
   * Sets array with gievn value.
   * 
   * @param pBeginIndex
   *          begin index (inclusive)
   * @param pEndIndex
   *          end index (exclusive)
   * @param pValue
   *          value to set.
   */
  public void set(int pBeginIndex, int pEndIndex, float pValue)
  {
    for (int i = pBeginIndex; i < pEndIndex; i++)
      mWriteArray[i] = pValue;
  }

  /**
   * Copies the values from the read array to the write array. This is useful if
   * you know that only a few values will be changed.
   */
  public void copyDefault()
  {
    copyDefault(0, mWriteArray.length);
  }

  /**
   * Copies the values from the read array to the write array. This is useful if
   * you know that only a few values will be changed.
   * 
   * @param pBeginIndex
   *          begin index (inclusive)
   * @param pEndIndex
   *          end index (exclusive)
   */
  public void copyDefault(int pBeginIndex, int pEndIndex)
  {
    System.arraycopy(mReadArray,
                     pBeginIndex,
                     mWriteArray,
                     pBeginIndex,
                     pEndIndex - pBeginIndex);
  }

  /**
   * Copies all values from the read array to the write array after multiplying
   * these values with a constant factor.
   * 
   * @param pValue
   *          value
   */
  public void copyAndMult(float pValue)
  {
    copyAndMult(0, mWriteArray.length, pValue);
  }

  /**
   * Copies all values from the read array to the write array after multiplying
   * these values with a constant factor.
   * 
   * @param pBeginIndex
   *          begin index (inclusive)
   * @param pEndIndex
   *          end index (exclusive)
   * @param pValue
   *          value
   */
  public void copyAndMult(int pBeginIndex,
                          int pEndIndex,
                          float pValue)
  {
    for (int i = pBeginIndex; i < pEndIndex; i++)
      mWriteArray[i] = pValue * mReadArray[i];
  }

  /**
   * Swap arrays.
   */
  public void swap()
  {
    float[] lTempRef = mWriteArray;
    mWriteArray = mReadArray;
    mReadArray = lTempRef;
  }

  /**
   * Copies the contents of the read array to another provided array.
   * 
   * @param pArrayCopy
   *          array to copy contents to.
   * @param pLength
   *          number of entries to copy
   */
  public void copyCurrentArrayTo(float[] pArrayCopy, int pLength)
  {
    System.arraycopy(getCurrentArray(), 0, pArrayCopy, 0, pLength);
  }

}
