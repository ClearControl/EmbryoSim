package simbryo.textures.noise.demo;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.textures.noise.BSplineNoise;
import simbryo.textures.noise.FractalNoise;
import simbryo.textures.noise.SimplexNoise;

/**
 * Fractal noise texture demos
 *
 * @author royer
 */
public class FractalNoiseDemos
{

  /**
   * Simplex 2D fractal noise demo
   */
  @Test
  public void demoWithSimplexNoise2D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      SimplexNoise lSimplexNoise = new SimplexNoise(2);
      FractalNoise lFractalNoise =
                                 new FractalNoise(lSimplexNoise,
                                                  0.1f,
                                                  0.05f,
                                                  0.025f,
                                                  0.0125f,
                                                  0.00625f);
      lFractalNoise.setAllScales(10);

      float[] lTexture = lFractalNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

  /**
   * Simplex 3D fractal noise demo
   */
  @Test
  public void demoWithSimplexNoise3D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      SimplexNoise lSimplexNoise = new SimplexNoise(3);
      FractalNoise lFractalNoise =
                                 new FractalNoise(lSimplexNoise,
                                                  0.1f,
                                                  0.05f,
                                                  0.025f,
                                                  0.0125f,
                                                  0.00625f);
      lFractalNoise.setAllScales(10);

      float[] lTexture = lFractalNoise.generateTexture(128, 128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

  /**
   * B-Spline 2D fractal noise demo
   */
  @Test
  public void demoWithBSplineNoise2D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      BSplineNoise lBSplineNoise = new BSplineNoise(3);
      FractalNoise lFractalNoise = new FractalNoise(lBSplineNoise,
                                                    1f,
                                                    0.5f,
                                                    0.25f,
                                                    0.125f,
                                                    0.0625f);

      float[] lTexture = lFractalNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

  /**
   * B-Spline 3D fractal noise demo
   */
  @Test
  public void demoWithBSplineNoise3D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      BSplineNoise lBSplineNoise = new BSplineNoise(2);
      FractalNoise lFractalNoise = new FractalNoise(lBSplineNoise,
                                                    1f,
                                                    0.5f,
                                                    0.25f,
                                                    0.125f,
                                                    0.0625f);

      float[] lTexture = lFractalNoise.generateTexture(128, 128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

}
