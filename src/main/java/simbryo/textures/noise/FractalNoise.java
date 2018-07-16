package simbryo.textures.noise;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * Fractal noise generator. Given a fratal noise generator it generates a
 * multi-scale version of it.
 *
 * @author royer
 */
public class FractalNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{
  private TextureGeneratorInterface mTextureGenerator;
  private float[] mFractalScales;

  private TextureGeneratorInterface[] mTextureGenerators;

  /**
   * 
   * @param pTextureGenerator
   *          texture generator to be used for each scale
   * @param pFractalScales
   *          scales to use for each 'octave' of the fractal noise.
   */
  public FractalNoise(TextureGeneratorInterface pTextureGenerator,
                      float... pFractalScales)
  {
    super(pTextureGenerator.getDimension());
    mTextureGenerator = pTextureGenerator;
    mFractalScales = pFractalScales;

    int lNumberOfScales = pFractalScales.length;
    mTextureGenerators =
                       new TextureGeneratorInterface[lNumberOfScales];
    reset();
  }

  private void reset()
  {
    for (int i = 0; i < mFractalScales.length; i++)
      mTextureGenerators[i] = mTextureGenerator.clone();
    for (int i = 0; i < mFractalScales.length; i++)
      for (int d = 0; d < getDimension(); d++)
        mTextureGenerators[i].setScale(d,
                                       getScale(d)
                                          * mFractalScales[i]);
  }

  @Override
  public TextureGeneratorInterface clone()
  {
    return new FractalNoise(mTextureGenerator, mFractalScales);
  }

  @Override
  public void setAllScales(float pScale)
  {
    super.setAllScales(pScale);
    reset();
  }

  @Override
  public float sampleTexture(int... pCoordinate)
  {
    int lNumberOfScales = mFractalScales.length;
    float lValue = 0;
    for (int i = 0; i < lNumberOfScales; i++)
    {
      lValue += mTextureGenerators[i].sampleTexture(pCoordinate);
    }
    return lValue;
  }

}
