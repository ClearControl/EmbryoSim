package simbryo.synthoscopy.phantom.io;

import java.io.File;
import java.io.IOException;

import clearcl.ClearCLImage;
import clearcl.io.RawWriter;
import coremem.enums.NativeTypeEnum;
import simbryo.synthoscopy.phantom.PhantomRendererInterface;

/**
 * Phantom raw writer
 *
 * @author royer
 */
public class PhantomRawWriter extends RawWriter implements
                              PhantomWriterInterface<ClearCLImage>
{

  /**
   * Instanciates a Phantom raw writer. The voxel values produced by the phantom
   * are scaled accoding to y = a*x+b.
   * 
   * @param pNativeTypeEnum
   *          nativetype to write file with
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public PhantomRawWriter(NativeTypeEnum pNativeTypeEnum,
                          float pScaling,
                          float pOffset)
  {
    super(pNativeTypeEnum, pScaling, pOffset);
  }

  @Override
  public boolean write(PhantomRendererInterface<ClearCLImage> pPhantomRenderer,
                       File pFile) throws IOException
  {
    ClearCLImage lPhantomImage = pPhantomRenderer.getImage();
    return write(lPhantomImage, pFile);
  }

}
