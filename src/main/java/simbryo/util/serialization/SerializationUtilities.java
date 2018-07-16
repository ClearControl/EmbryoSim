package simbryo.util.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

/**
 * utility class to save and load objects using Java serialization.
 *
 * @author royer
 */
public class SerializationUtilities
{
  /**
   * Saves the given object into a file (via Java serialization)
   * 
   * @param pObject
   *          object to save
   * @param pFile
   *          file to save to
   * @throws IOException
   *           thrown if problem occurs while writing file
   */
  public static <O extends Serializable> void saveToFile(O pObject,
                                                         File pFile) throws IOException
  {
    pFile.getParentFile().mkdirs();
    try (
        FileOutputStream lFileOutputStream =
                                           new FileOutputStream(pFile))
    {
      SerializationUtils.serialize(pObject, lFileOutputStream);
    }
  }

  /**
   * Retrieves embryo dynamics saved to a file. if the file does not exist this
   * method returns null.
   * 
   * @param pClass
   *          class of object retrieved.
   * @param pFile
   *          file
   * @return embryo dynamics
   * @throws IOException
   *           thrown if problem occurs while reading file
   */
  @SuppressWarnings("unchecked")
  public static <O extends Serializable> O loadFromFile(Class<O> pClass,
                                                        File pFile) throws IOException
  {
    if (!pFile.exists())
      return null;
    try (
        FileInputStream lFileInputStream = new FileInputStream(pFile))
    {
      return ((O) SerializationUtils.deserialize(lFileInputStream));
    }
    catch (SerializationException e)
    {
      return null;
    }
    catch (FileNotFoundException e)
    {
      // Should not happen as we just checked
      e.printStackTrace();
    }
    return null;
  }
}
