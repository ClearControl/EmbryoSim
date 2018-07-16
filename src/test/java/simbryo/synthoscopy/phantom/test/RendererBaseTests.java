package simbryo.synthoscopy.phantom.test;

import static org.junit.Assert.assertEquals;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import coremem.ContiguousMemoryInterface;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.PhantomRendererBase;

/**
 * Basic tests for Renderer base class
 *
 * @author royer
 */
public class RendererBaseTests
{

  /**
   * Basic test checking the smart cache-aware rendering.
   */
  @Test
  public void testSmartness()
  {

    Drosophila lDrosophila = new Drosophila(16, 64, 64, 64);

    PhantomRendererBase lRenderer;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();
        ClearCLContext lContext = lFastestGPUDevice.createContext())
    {

      lRenderer = new PhantomRendererBase(lContext,
                                          16,
                                          lDrosophila,
                                          512,
                                          512,
                                          128)
      {
        @Override
        public void renderInternal(int pZPlaneIndexBegin,
                                   int pZPlaneIndexEnd,
                                   boolean pBlockingCall)
        {
          // do nothing
        }

        @Override
        public void render(boolean pBlockingCall)
        {
          // TODO Auto-generated method stub

        }

        @Override
        public void copyTo(ContiguousMemoryInterface pMemory,
                           boolean pBlocking)
        {
          // TODO Auto-generated method stub

        }

        @Override
        public ClearCLImage getImage()
        {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public void clear(boolean pBlockingCall)
        {
          // TODO Auto-generated method stub

        }
      };
    }

    assertEquals(75, lRenderer.renderAndCount(0, 75, true));
    assertEquals(25, lRenderer.renderAndCount(25, 100, true));

    lRenderer.close();
  }

}
