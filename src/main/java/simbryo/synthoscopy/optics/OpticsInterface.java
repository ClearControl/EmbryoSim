package simbryo.synthoscopy.optics;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * Optics interface
 *
 * @author royer
 */
public interface OpticsInterface
{
  /**
   * Gets the transform matrix used for accessing phantoms.
   * 
   * @return 4x4 transform matrix (homogenous coordinates)
   */
  Matrix4f getPhantomTransformMatrix();

  /**
   * Gets the inverse transform matrix used for accessing phantoms.
   * 
   * @return 4x4 inverse transform matrix (homogenous coordinates)
   */
  Matrix4f getInversePhantomTransformMatrix();

  /**
   * Sets the transform matrix used for accessing phantoms.
   * 
   * @param pTransformMatrix
   *          4x4 transform matrix (homogenous coordinates)
   */
  void setPhantomTransformMatrix(Matrix4f pTransformMatrix);

  /**
   * Sets the translation component of the transform matrix used for accessing
   * phantoms.
   * 
   * @param pTranslationVector
   *          trabslation vector
   */
  void setTranslation(Vector3f pTranslationVector);

  /**
   * Returns light wavelength
   * 
   * @return wavelength in normalized units (within [0,1])
   */
  float getLightWavelength();

  /**
   * Sets light wavelength in normalized units.
   * 
   * @param pWavelengthInNormUnits
   *          wavelength in normalized units
   */
  void setLightWavelength(float pWavelengthInNormUnits);

}
