package simbryo.textures;

/**
 * Texture generators are used to, well, generate textures...
 *
 * @author royer
 */
public interface TextureGeneratorInterface extends Cloneable
{

  /**
   * Returns dimension of texture
   * 
   * @return texture dimension
   */
  int getDimension();

  /**
   * Returns scale at a given dimension index
   * 
   * @param pIndex
   *          dimension index
   * @return scale
   */
  float getScale(int pIndex);

  /**
   * Sets scale at given index.
   * 
   * @param pIndex
   *          dimension index
   * @param pScale
   *          scale
   */
  void setScale(int pIndex, float pScale);

  /**
   * Sets all scales i.e. for all dimensions to a given value.
   * 
   * @param pScale
   */
  void setAllScales(float pScale);

  /**
   * generates texture as an array of floats.
   * 
   * @param pDimensions
   *          dimensions of textures
   * @return texture as array of floats
   */
  float[] generateTexture(long... pDimensions);

  /**
   * Samples textures ata given coordinate
   * 
   * @param pCoordinate
   *          texture coordinate
   * @return texture value
   */
  float sampleTexture(int... pCoordinate);

  /**
   * Clones texture
   * 
   * @return returns new cloned texture.
   */
  TextureGeneratorInterface clone();

}
