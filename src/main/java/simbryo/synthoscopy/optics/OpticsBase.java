package simbryo.synthoscopy.optics;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.util.MatrixUtils;
import simbryo.synthoscopy.ClearCLSynthoscopyBase;
import simbryo.synthoscopy.SynthoscopyInterface;

/**
 * Optics base class providing common firlds and methods for optics related
 * classes
 *
 * @author royer
 */
public abstract class OpticsBase extends ClearCLSynthoscopyBase
                                 implements
                                 SynthoscopyInterface<ClearCLImage>,
                                 OpticsInterface
{

  private static final float cDefaultWavelengthInNormUnits = 0.0005f;
  private float mWavelengthInNormUnits;

  private Matrix4f mPhantomTransformMatrix = new Matrix4f();
  private ClearCLBuffer mTransformMatrixBuffer;

  /**
   * Instanciates a optics base class with basic optics related fields.
   * 
   * @param pContext
   *          ClearCL context
   * 
   * @param pImageDimensions
   *          image dimensions
   */
  public OpticsBase(final ClearCLContext pContext,
                    long... pImageDimensions)
  {
    super(pContext,
          false,
          ImageChannelDataType.Float,
          pImageDimensions);
    mWavelengthInNormUnits = cDefaultWavelengthInNormUnits;
    mPhantomTransformMatrix.setIdentity();
  }

  protected ClearCLBuffer getPhantomTransformMatrixBuffer()
  {
    mTransformMatrixBuffer =
                           MatrixUtils.matrixToBuffer(mContext,
                                                      mTransformMatrixBuffer,
                                                      getPhantomTransformMatrix());
    return mTransformMatrixBuffer;
  }

  @Override
  public float getLightWavelength()
  {
    return mWavelengthInNormUnits;
  }

  @Override
  public void setLightWavelength(float pWavelengthInNormUnits)
  {
    if (mWavelengthInNormUnits != pWavelengthInNormUnits)
    {
      mWavelengthInNormUnits = pWavelengthInNormUnits;
      requestUpdate();
    }
  }

  @Override
  public Matrix4f getPhantomTransformMatrix()
  {
    return new Matrix4f(mPhantomTransformMatrix);
  }

  @Override
  public Matrix4f getInversePhantomTransformMatrix()
  {
    Matrix4f lInverseTransformMatrix = getPhantomTransformMatrix();
    lInverseTransformMatrix.invert();
    return lInverseTransformMatrix;
  }

  @Override
  public void setPhantomTransformMatrix(Matrix4f pTransformMatrix)
  {
    if (mPhantomTransformMatrix == null
        || !mPhantomTransformMatrix.equals(pTransformMatrix))
    {
      mPhantomTransformMatrix = pTransformMatrix;
      requestUpdate();
    }
  }

  @Override
  public void setTranslation(Vector3f pTranslationVector)
  {
    Matrix4f lNewMatrix = new Matrix4f();
    lNewMatrix.set(getPhantomTransformMatrix());
    lNewMatrix.setTranslation(pTranslationVector);
    setPhantomTransformMatrix(lNewMatrix);
  }

}
