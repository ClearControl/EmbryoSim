package simbryo.synthoscopy.phantom.io;

import java.io.File;

import simbryo.synthoscopy.phantom.PhantomRendererInterface;

/**
 * Phantom writer interface
 *
 * @author royer
 * @param <I>
 *          image type
 */
public interface PhantomWriterInterface<I>
{

  /**
   * Renders a phantom stack to a file.
   * 
   * @param pPhantomRenderer
   *          phantom renderer
   * @param pFile
   *          file to write to
   * @return true if file written, false otherwise
   * @throws Throwable
   *           if anything goes wrong...
   */
  boolean write(PhantomRendererInterface<I> pPhantomRenderer,
                File pFile) throws Throwable;

}
